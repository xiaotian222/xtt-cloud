package xtt.cloud.oa.workflow.infrastructure.cache.org;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import xtt.cloud.oa.common.dto.UserInfoDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 用户信息缓存服务
 * 使用 Redis 缓存用户信息，减少对 Platform 服务的调用
 * 
 * @author xtt
 */
@Service
public class UserCacheService {

    private static final Logger log = LoggerFactory.getLogger(UserCacheService.class);
    
    private static final String USER_CACHE_PREFIX = "workflow:user:";
    private static final String USER_BATCH_CACHE_PREFIX = "workflow:user:batch:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${workflow.cache.user.expiration:3600}")
    private Long cacheExpiration; // 默认1小时

    public UserCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 从缓存中获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息，如果缓存不存在则返回 empty
     */
    public Optional<UserInfoDto> getUserById(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        
        try {
            String key = USER_CACHE_PREFIX + userId;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                UserInfoDto user = objectMapper.readValue(value, UserInfoDto.class);
                log.debug("User cache hit for userId: {}", userId);
                return Optional.of(user);
            }
            log.debug("User cache miss for userId: {}", userId);
            return Optional.empty();
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize user from cache for userId: {}", userId, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get user from cache for userId: {}", userId, e);
            return Optional.empty();
        }
    }

    /**
     * 批量从缓存中获取用户信息
     * 
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    public List<UserInfoDto> getUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<UserInfoDto> users = new ArrayList<>();
        List<Long> missingIds = new ArrayList<>();
        
        // 先从缓存中获取
        for (Long userId : userIds) {
            Optional<UserInfoDto> userOpt = getUserById(userId);
            if (userOpt.isPresent()) {
                users.add(userOpt.get());
            } else {
                missingIds.add(userId);
            }
        }
        
        // 如果有缺失的，记录日志
        if (!missingIds.isEmpty()) {
            log.debug("Missing users in cache, userIds: {}", missingIds);
        }
        
        return users;
    }

    /**
     * 将用户信息存入缓存
     * 
     * @param user 用户信息
     */
    public void cacheUser(UserInfoDto user) {
        if (user == null || user.getId() == null) {
            return;
        }
        
        try {
            String key = USER_CACHE_PREFIX + user.getId();
            String value = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(key, value, cacheExpiration, TimeUnit.SECONDS);
            log.debug("User cached for userId: {}", user.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize user for cache for userId: {}", user.getId(), e);
        } catch (Exception e) {
            log.error("Failed to cache user for userId: {}", user.getId(), e);
        }
    }

    /**
     * 批量将用户信息存入缓存
     * 
     * @param users 用户信息列表
     */
    public void cacheUsers(List<UserInfoDto> users) {
        if (users == null || users.isEmpty()) {
            return;
        }
        
        for (UserInfoDto user : users) {
            cacheUser(user);
        }
    }

    /**
     * 从缓存中删除用户信息
     * 
     * @param userId 用户ID
     */
    public void evictUser(Long userId) {
        if (userId == null) {
            return;
        }
        
        try {
            String key = USER_CACHE_PREFIX + userId;
            redisTemplate.delete(key);
            log.debug("User cache evicted for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to evict user cache for userId: {}", userId, e);
        }
    }

    /**
     * 批量从缓存中删除用户信息
     * 
     * @param userIds 用户ID列表
     */
    public void evictUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        
        for (Long userId : userIds) {
            evictUser(userId);
        }
    }
}

