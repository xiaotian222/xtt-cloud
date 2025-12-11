package xtt.cloud.oa.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import xtt.cloud.oa.common.dto.UserInfoDto;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 用户信息缓存服务
 * 使用 Redis 缓存用户信息，减少对 Platform 服务的调用
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Service
public class UserCacheService {

    private static final Logger log = LoggerFactory.getLogger(UserCacheService.class);
    
    private static final String USER_CACHE_PREFIX = "user:cache:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${user.cache.expiration:3600}")
    private Long cacheExpiration;

    /**
     * 从缓存中获取用户信息
     * 
     * @param username 用户名
     * @return 用户信息，如果缓存不存在则返回 empty
     */
    public Optional<UserInfoDto> getUserFromCache(String username) {
        try {
            String key = USER_CACHE_PREFIX + username;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                UserInfoDto user = objectMapper.readValue(value, UserInfoDto.class);
                log.debug("User cache hit for username: {}", username);
                return Optional.of(user);
            }
            log.debug("User cache miss for username: {}", username);
            return Optional.empty();
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize user from cache for username: {}", username, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get user from cache for username: {}", username, e);
            return Optional.empty();
        }
    }

    /**
     * 将用户信息存入缓存
     * 
     * @param username 用户名
     * @param user 用户信息
     */
    public void cacheUser(String username, UserInfoDto user) {
        try {
            String key = USER_CACHE_PREFIX + username;
            String value = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(key, value, cacheExpiration, TimeUnit.SECONDS);
            log.debug("User cached for username: {}", username);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize user for cache for username: {}", username, e);
        } catch (Exception e) {
            log.error("Failed to cache user for username: {}", username, e);
        }
    }

    /**
     * 从缓存中删除用户信息
     * 
     * @param username 用户名
     */
    public void evictUser(String username) {
        try {
            String key = USER_CACHE_PREFIX + username;
            redisTemplate.delete(key);
            log.debug("User cache evicted for username: {}", username);
        } catch (Exception e) {
            log.error("Failed to evict user cache for username: {}", username, e);
        }
    }

    /**
     * 清空所有用户缓存（谨慎使用）
     */
    public void clearAllCache() {
        try {
            // 这里可以根据实际需求实现批量删除
            log.warn("User cache clear requested - this operation should be used with caution");
        } catch (Exception e) {
            log.error("Failed to clear user cache", e);
        }
    }
}

