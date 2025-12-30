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

package xtt.cloud.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * RabbitMQ 自动配置类
 * 
 * 提供以下功能：
 * 1. 配置 RabbitTemplate（JSON 消息转换器）
 * 2. 配置消息转换器（Jackson2JsonMessageConverter）
 * 3. 配置 RabbitListenerContainerFactory（支持 JSON 消息）
 * 4. 配置消息发送器（RabbitMessageSender）
 * 5. 打印 RabbitMQ 配置信息（用于调试）
 * 
 * 配置说明：
 * - Spring Boot 会自动从配置文件（application.yaml 或 Nacos）读取 spring.rabbitmq.* 配置
 * - 自动创建 ConnectionFactory
 * 
 * @author xtt
 */
@AutoConfiguration
@ConditionalOnClass({RabbitTemplate.class, ConnectionFactory.class})
@EnableConfigurationProperties(RabbitProperties.class)
public class RabbitMQAutoConfiguration {
    
    private static final Logger log = LoggerFactory.getLogger(RabbitMQAutoConfiguration.class);

    /**
     * 配置消息转换器（Jackson2JsonMessageConverter）
     * 
     * 用于将消息对象序列化为 JSON，或将 JSON 反序列化为对象
     */
    @Bean
    @ConditionalOnMissingBean
    @Primary
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        log.info("RabbitMQ 消息转换器配置完成（Jackson2JsonMessageConverter）");
        return converter;
    }

    /**
     * 配置 RabbitTemplate
     * 
     * ConnectionFactory 由 Spring Boot 自动配置，会从 spring.rabbitmq.* 读取配置
     */
    @Bean
    @ConditionalOnMissingBean
    @Primary
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        
        // 启用消息确认
        template.setMandatory(true);
        
        // 设置消息返回回调
        template.setReturnsCallback((returned) -> {
            log.warn("消息返回：Exchange: {}, RoutingKey: {}, ReplyCode: {}, ReplyText: {}, Message: {}",
                    returned.getExchange(),
                    returned.getRoutingKey(),
                    returned.getReplyCode(),
                    returned.getReplyText(),
                    returned.getMessage());
        });
        
        // 设置消息确认回调
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("消息发送成功，CorrelationData: {}", correlationData);
            } else {
                log.error("消息发送失败，CorrelationData: {}, Cause: {}", correlationData, cause);
            }
        });
        
        log.info("RabbitTemplate 配置完成，连接工厂: {}", connectionFactory.getClass().getSimpleName());
        return template;
    }

    /**
     * 配置 RabbitListenerContainerFactory
     * 
     * 用于 @RabbitListener 注解的消息监听器
     */
    @Bean
    @ConditionalOnMissingBean(name = "rabbitListenerContainerFactory")
    @Primary
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        
        // 设置并发消费者数量
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(10);
        
        // 设置预取数量
        factory.setPrefetchCount(10);
        
        // 设置自动确认
        factory.setAutoStartup(true);
        
        log.info("RabbitListenerContainerFactory 配置完成");
        return factory;
    }

    /**
     * 配置消息发送器
     * 
     * 提供便捷的消息发送方法
     */
    @Bean
    @ConditionalOnMissingBean
    public RabbitMessageSender rabbitMessageSender(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        return new RabbitMessageSender(rabbitTemplate, objectMapper);
    }

    /**
     * 打印 RabbitMQ 配置信息（用于调试）
     */
    @Bean
    @ConditionalOnClass(RabbitProperties.class)
    public RabbitMQConfigInfoPrinter rabbitMQConfigInfoPrinter(RabbitProperties rabbitProperties) {
        return new RabbitMQConfigInfoPrinter(rabbitProperties);
    }
    
    /**
     * RabbitMQ 配置信息打印器
     */
    public static class RabbitMQConfigInfoPrinter {
        
        private static final Logger log = LoggerFactory.getLogger(RabbitMQConfigInfoPrinter.class);
        
        public RabbitMQConfigInfoPrinter(RabbitProperties rabbitProperties) {
            log.info("=== RabbitMQ 配置信息 ===");
            log.info("RabbitMQ Host: {}", rabbitProperties.getHost());
            log.info("RabbitMQ Port: {}", rabbitProperties.getPort());
            log.info("RabbitMQ Username: {}", rabbitProperties.getUsername());
            log.info("RabbitMQ Virtual Host: {}", rabbitProperties.getVirtualHost());
            if (rabbitProperties.getListener() != null) {
                log.info("Listener Type: {}", rabbitProperties.getListener().getType());
                if (rabbitProperties.getListener().getSimple() != null) {
                    log.info("Simple Listener Concurrency: {}", rabbitProperties.getListener().getSimple().getConcurrency());
                    log.info("Simple Listener Max Concurrency: {}", rabbitProperties.getListener().getSimple().getMaxConcurrency());
                    log.info("Simple Listener Prefetch: {}", rabbitProperties.getListener().getSimple().getPrefetch());
                }
            }
            log.info("========================");
        }
    }
}

