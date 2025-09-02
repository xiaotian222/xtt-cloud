package xtt.cloud.oa.platform.infrastructure.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import xtt.cloud.oa.platform.domain.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PermissionCache {
    public static final String USER_PERMS_CACHE = "userPerms";

    @Cacheable(cacheNames = USER_PERMS_CACHE, key = "#user.id")
    public Set<String> loadUserPermissions(User user) {
        if (user.getRoles() == null) return java.util.Set.of();
        return user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getCode())
                .collect(Collectors.toSet());
    }

    @CacheEvict(cacheNames = USER_PERMS_CACHE, key = "#userId")
    public void evictUserPerms(Long userId) { }

    @Cacheable(cacheNames = USER_PERMS_CACHE, key = "#username")
    public Set<String> loadUserPermissions(String username) {
        // 这个方法需要从 UserService 中调用，这里先返回空集合
        // 实际实现中应该通过 UserService 获取用户信息
        return java.util.Set.of();
    }
}


