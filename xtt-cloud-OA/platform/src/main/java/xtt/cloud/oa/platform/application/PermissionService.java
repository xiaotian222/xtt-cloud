package xtt.cloud.oa.platform.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.platform.domain.entity.Permission;
import xtt.cloud.oa.platform.domain.mapper.PermissionMapper;
import xtt.cloud.oa.platform.infrastructure.cache.PermissionCache;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PermissionService {
    private final PermissionMapper permissionMapper;
    private final PermissionCache permissionCache;
    
    public PermissionService(PermissionMapper permissionMapper, PermissionCache permissionCache) { 
        this.permissionMapper = permissionMapper;
        this.permissionCache = permissionCache;
    }

    public List<Permission> list() { 
        return permissionMapper.selectAll(); 
    }
    
    public Optional<Permission> get(Long id) { 
        return permissionMapper.findById(id); 
    }

    @Transactional
    public Permission save(Permission permission) { 
        if (permission.getId() == null) {
            // 新增权限
            permission.setCreatedAt(LocalDateTime.now());
            permission.setUpdatedAt(LocalDateTime.now());
            permissionMapper.insert(permission);
        } else {
            // 更新权限
            permission.setUpdatedAt(LocalDateTime.now());
            permissionMapper.update(permission);
        }
        return permission;
    }

    @Transactional
    public void delete(Long id) { 
        permissionMapper.deleteById(id); 
    }

    // 对外服务方法
    public Optional<Permission> findByCode(String code) {
        return permissionMapper.findByCode(code);
    }

    public List<Permission> findByIds(List<Long> permissionIds) {
        return permissionMapper.selectByIds(permissionIds);
    }

    public List<Permission> findByCodes(List<String> codes) {
        return permissionMapper.selectByCodes(codes);
    }

    public List<Permission> findByType(String type) {
        return permissionMapper.selectByType(type);
    }

    public boolean userHasPermission(String username, String permissionCode) {
        return permissionCache.loadUserPermissions(username).contains(permissionCode);
    }

    public boolean userHasAnyPermission(String username, List<String> permissionCodes) {
        Set<String> userPermissions = permissionCache.loadUserPermissions(username);
        return permissionCodes.stream().anyMatch(userPermissions::contains);
    }

    public boolean userHasAllPermissions(String username, List<String> permissionCodes) {
        Set<String> userPermissions = permissionCache.loadUserPermissions(username);
        return userPermissions.containsAll(permissionCodes);
    }

    // 从UserService移过来的权限相关方法
    public Set<String> getUserPermissions(Long userId) {
        return permissionCache.loadUserPermissions(userId);
    }

    public Set<String> getUserPermissionsByUsername(String username) {
        return permissionCache.loadUserPermissions(username);
    }
}


