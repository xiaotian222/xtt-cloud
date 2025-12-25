/*
 * Copyright 2024 XTT Cloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xtt.cloud.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 自动配置类
 * 
 * 提供以下功能：
 * 1. 配置 StringRedisTemplate（如果不存在）
 * 2. 配置 ObjectMapper（用于 JSON 序列化/反序列化）
 * 3. 打印 Redis 配置信息（用于调试）
 * 
 * 配置说明：
 * - Spring Boot 会自动从配置文件（application.yaml 或 Nacos）读取 spring.redis.* 配置
 * - 自动创建 RedisConnectionFactory（LettuceConnectionFactory 或 JedisConnectionFactory）
 * 
 * @author xtt
 */
@AutoConfiguration
@ConditionalOnClass({RedisConnectionFactory.class, StringRedisTemplate.class})
@EnableConfigurationProperties(RedisProperties.class)
public class RedisAutoConfiguration {
    
    private static final Logger log = LoggerFactory.getLogger(RedisAutoConfiguration.class);

    /**
     * 配置 StringRedisTemplate
     * 
     * RedisConnectionFactory 由 Spring Boot 自动配置，会从 spring.redis.* 读取配置
     */
    @Bean
    @ConditionalOnMissingBean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        
        // 设置序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        
        // 初始化连接
        template.afterPropertiesSet();
        
        log.info("StringRedisTemplate 配置完成，Redis 连接工厂: {}", connectionFactory.getClass().getSimpleName());
        return template;
    }

    /**
     * 配置 ObjectMapper（用于 JSON 序列化/反序列化）
     * 
     * 注意：如果应用中已经存在 ObjectMapper Bean，则不会创建此 Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(
            mapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        log.debug("Redis ObjectMapper 配置完成");
        return mapper;
    }
    
    /**
     * 打印 Redis 配置信息（用于调试）
     * 
     * 注意：这个方法会在应用启动时打印 Redis 配置，帮助确认配置是否正确加载
     * 可以通过配置 xtt.cloud.redis.config-info.enabled=false 来禁用
     */
    @Bean
    @ConditionalOnClass(RedisProperties.class)
    public RedisConfigInfoPrinter redisConfigInfoPrinter(RedisProperties redisProperties) {
        return new RedisConfigInfoPrinter(redisProperties);
    }
    
    /**
     * Redis 配置信息打印器
     */
    public static class RedisConfigInfoPrinter {
        
        private static final Logger log = LoggerFactory.getLogger(RedisConfigInfoPrinter.class);
        
        public RedisConfigInfoPrinter(RedisProperties redisProperties) {
            log.info("=== Redis 配置信息 ===");
            log.info("Redis Host: {}", redisProperties.getHost());
            log.info("Redis Port: {}", redisProperties.getPort());
            log.info("Redis Database: {}", redisProperties.getDatabase());
            log.info("Redis Timeout: {}", redisProperties.getTimeout());
            if (redisProperties.getLettuce() != null && redisProperties.getLettuce().getPool() != null) {
                log.info("Redis Pool Max-Active: {}", redisProperties.getLettuce().getPool().getMaxActive());
                log.info("Redis Pool Max-Idle: {}", redisProperties.getLettuce().getPool().getMaxIdle());
                log.info("Redis Pool Min-Idle: {}", redisProperties.getLettuce().getPool().getMinIdle());
            }
            log.info("====================");
        }
    }
}

