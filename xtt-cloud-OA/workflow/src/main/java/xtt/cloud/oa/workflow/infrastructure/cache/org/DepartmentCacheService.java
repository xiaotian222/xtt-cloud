package xtt.cloud.oa.workflow.infrastructure.cache.org;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import xtt.cloud.oa.common.dto.DeptInfoDto;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 部门信息缓存服务
 * 使用 Redis 缓存部门信息，减少对 Platform 服务的调用
 * 
 * @author xtt
 */
@Service
public class DepartmentCacheService {

    private static final Logger log = LoggerFactory.getLogger(DepartmentCacheService.class);
    
    private static final String DEPT_CACHE_PREFIX = "workflow:dept:";
    private static final String DEPT_USERS_CACHE_PREFIX = "workflow:dept:users:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${workflow.cache.department.expiration:3600}")
    private Long cacheExpiration; // 默认1小时

    public DepartmentCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 从缓存中获取部门信息
     * 
     * @param deptId 部门ID
     * @return 部门信息，如果缓存不存在则返回 empty
     */
    public Optional<DeptInfoDto> getDepartmentById(Long deptId) {
        if (deptId == null) {
            return Optional.empty();
        }
        
        try {
            String key = DEPT_CACHE_PREFIX + deptId;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                DeptInfoDto dept = objectMapper.readValue(value, DeptInfoDto.class);
                log.debug("Department cache hit for deptId: {}", deptId);
                return Optional.of(dept);
            }
            log.debug("Department cache miss for deptId: {}", deptId);
            return Optional.empty();
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize department from cache for deptId: {}", deptId, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get department from cache for deptId: {}", deptId, e);
            return Optional.empty();
        }
    }

    /**
     * 将部门信息存入缓存
     * 
     * @param dept 部门信息
     */
    public void cacheDepartment(DeptInfoDto dept) {
        if (dept == null || dept.getId() == null) {
            return;
        }
        
        try {
            String key = DEPT_CACHE_PREFIX + dept.getId();
            String value = objectMapper.writeValueAsString(dept);
            redisTemplate.opsForValue().set(key, value, cacheExpiration, TimeUnit.SECONDS);
            log.debug("Department cached for deptId: {}", dept.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize department for cache for deptId: {}", dept.getId(), e);
        } catch (Exception e) {
            log.error("Failed to cache department for deptId: {}", dept.getId(), e);
        }
    }

    /**
     * 从缓存中获取部门下的用户ID列表
     * 
     * @param deptId 部门ID
     * @return 用户ID列表，如果缓存不存在则返回 empty
     */
    public Optional<List<Long>> getUserIdsByDepartmentId(Long deptId) {
        if (deptId == null) {
            return Optional.empty();
        }
        
        try {
            String key = DEPT_USERS_CACHE_PREFIX + deptId;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                List<Long> userIds = objectMapper.readValue(value, new TypeReference<List<Long>>() {});
                log.debug("Department users cache hit for deptId: {}", deptId);
                return Optional.of(userIds);
            }
            log.debug("Department users cache miss for deptId: {}", deptId);
            return Optional.empty();
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize department users from cache for deptId: {}", deptId, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get department users from cache for deptId: {}", deptId, e);
            return Optional.empty();
        }
    }

    /**
     * 将部门下的用户ID列表存入缓存
     * 
     * @param deptId 部门ID
     * @param userIds 用户ID列表
     */
    public void cacheUserIdsByDepartmentId(Long deptId, List<Long> userIds) {
        if (deptId == null || userIds == null) {
            return;
        }
        
        try {
            String key = DEPT_USERS_CACHE_PREFIX + deptId;
            String value = objectMapper.writeValueAsString(userIds);
            redisTemplate.opsForValue().set(key, value, cacheExpiration, TimeUnit.SECONDS);
            log.debug("Department users cached for deptId: {}", deptId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize department users for cache for deptId: {}", deptId, e);
        } catch (Exception e) {
            log.error("Failed to cache department users for deptId: {}", deptId, e);
        }
    }

    /**
     * 从缓存中删除部门信息
     * 
     * @param deptId 部门ID
     */
    public void evictDepartment(Long deptId) {
        if (deptId == null) {
            return;
        }
        
        try {
            String deptKey = DEPT_CACHE_PREFIX + deptId;
            String usersKey = DEPT_USERS_CACHE_PREFIX + deptId;
            redisTemplate.delete(deptKey);
            redisTemplate.delete(usersKey);
            log.debug("Department cache evicted for deptId: {}", deptId);
        } catch (Exception e) {
            log.error("Failed to evict department cache for deptId: {}", deptId, e);
        }
    }
}

