package xtt.cloud.oa.workflow.infrastructure.cache.org;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import xtt.cloud.oa.common.dto.RoleInfoDto;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 角色信息缓存服务
 * 使用 Redis 缓存角色信息，减少对 Platform 服务的调用
 * 
 * @author xtt
 */
@Service
public class RoleCacheService {

    private static final Logger log = LoggerFactory.getLogger(RoleCacheService.class);
    
    private static final String ROLE_CACHE_PREFIX = "workflow:role:";
    private static final String ROLE_USERS_CACHE_PREFIX = "workflow:role:users:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${workflow.cache.role.expiration:3600}")
    private Long cacheExpiration; // 默认1小时

    public RoleCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 从缓存中获取角色信息
     * 
     * @param roleId 角色ID
     * @return 角色信息，如果缓存不存在则返回 empty
     */
    public Optional<RoleInfoDto> getRoleById(Long roleId) {
        if (roleId == null) {
            return Optional.empty();
        }
        
        try {
            String key = ROLE_CACHE_PREFIX + roleId;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                RoleInfoDto role = objectMapper.readValue(value, RoleInfoDto.class);
                log.debug("Role cache hit for roleId: {}", roleId);
                return Optional.of(role);
            }
            log.debug("Role cache miss for roleId: {}", roleId);
            return Optional.empty();
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize role from cache for roleId: {}", roleId, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get role from cache for roleId: {}", roleId, e);
            return Optional.empty();
        }
    }

    /**
     * 将角色信息存入缓存
     * 
     * @param role 角色信息
     */
    public void cacheRole(RoleInfoDto role) {
        if (role == null || role.getId() == null) {
            return;
        }
        
        try {
            String key = ROLE_CACHE_PREFIX + role.getId();
            String value = objectMapper.writeValueAsString(role);
            redisTemplate.opsForValue().set(key, value, cacheExpiration, TimeUnit.SECONDS);
            log.debug("Role cached for roleId: {}", role.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize role for cache for roleId: {}", role.getId(), e);
        } catch (Exception e) {
            log.error("Failed to cache role for roleId: {}", role.getId(), e);
        }
    }

    /**
     * 从缓存中获取角色下的用户ID列表
     * 
     * @param roleId 角色ID
     * @return 用户ID列表，如果缓存不存在则返回 empty
     */
    public Optional<List<Long>> getUserIdsByRoleId(Long roleId) {
        if (roleId == null) {
            return Optional.empty();
        }
        
        try {
            String key = ROLE_USERS_CACHE_PREFIX + roleId;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                List<Long> userIds = objectMapper.readValue(value, new TypeReference<List<Long>>() {});
                log.debug("Role users cache hit for roleId: {}", roleId);
                return Optional.of(userIds);
            }
            log.debug("Role users cache miss for roleId: {}", roleId);
            return Optional.empty();
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize role users from cache for roleId: {}", roleId, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get role users from cache for roleId: {}", roleId, e);
            return Optional.empty();
        }
    }

    /**
     * 将角色下的用户ID列表存入缓存
     * 
     * @param roleId 角色ID
     * @param userIds 用户ID列表
     */
    public void cacheUserIdsByRoleId(Long roleId, List<Long> userIds) {
        if (roleId == null || userIds == null) {
            return;
        }
        
        try {
            String key = ROLE_USERS_CACHE_PREFIX + roleId;
            String value = objectMapper.writeValueAsString(userIds);
            redisTemplate.opsForValue().set(key, value, cacheExpiration, TimeUnit.SECONDS);
            log.debug("Role users cached for roleId: {}", roleId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize role users for cache for roleId: {}", roleId, e);
        } catch (Exception e) {
            log.error("Failed to cache role users for roleId: {}", roleId, e);
        }
    }

    /**
     * 从缓存中删除角色信息
     * 
     * @param roleId 角色ID
     */
    public void evictRole(Long roleId) {
        if (roleId == null) {
            return;
        }
        
        try {
            String roleKey = ROLE_CACHE_PREFIX + roleId;
            String usersKey = ROLE_USERS_CACHE_PREFIX + roleId;
            redisTemplate.delete(roleKey);
            redisTemplate.delete(usersKey);
            log.debug("Role cache evicted for roleId: {}", roleId);
        } catch (Exception e) {
            log.error("Failed to evict role cache for roleId: {}", roleId, e);
        }
    }
}

