package xtt.cloud.oa.platform.infrastructure.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import xtt.cloud.oa.platform.application.PermissionDomainService;
import xtt.cloud.oa.platform.domain.entity.User;

import java.util.Set;

@Component
public class PermissionCache {
    private static final Logger logger = LoggerFactory.getLogger(PermissionCache.class);
    public static final String USER_PERMS_CACHE = "userPerms";
    
    private final PermissionDomainService permissionDomainService;
    
    @Autowired
    public PermissionCache(PermissionDomainService permissionDomainService) {
        this.permissionDomainService = permissionDomainService;
    }

    @Cacheable(cacheNames = USER_PERMS_CACHE, key = "#user.id")
    public Set<String> loadUserPermissions(User user) {
        if (user.getId() == null) return java.util.Set.of();
        return permissionDomainService.calculateUserPermissions(user.getId());
    }

    @CacheEvict(cacheNames = USER_PERMS_CACHE, key = "#userId")
    public void evictUserPerms(Long userId) {
        logger.info("Evicting permission cache for user ID: {}", userId);
    }

    @CacheEvict(cacheNames = USER_PERMS_CACHE, allEntries = true)
    public void evictAllUserPerms() {
        logger.info("Evicting all permission cache entries");
        
        // 可以在这里添加额外的清理逻辑
        // 比如清理相关的其他缓存、发送缓存清理事件等
        logger.debug("All permission cache entries have been evicted");
    }
    
    /**
     * 根据用户名清理权限缓存
     * 这是一个辅助方法，用于清理按用户名缓存的权限
     */
    @CacheEvict(cacheNames = USER_PERMS_CACHE, key = "#username")
    public void evictUserPermsByUsername(String username) {
        logger.debug("Evicting permission cache for username: {}", username);
    }

    @Cacheable(cacheNames = USER_PERMS_CACHE, key = "#username")
    public Set<String> loadUserPermissions(String username) {
        return permissionDomainService.calculateUserPermissionsByUsername(username);
    }

    @Cacheable(cacheNames = USER_PERMS_CACHE, key = "#userId")
    public Set<String> loadUserPermissions(Long userId) {
        return permissionDomainService.calculateUserPermissions(userId);
    }
}


