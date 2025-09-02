package xtt.cloud.oa.platform.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.platform.domain.entity.Permission;
import xtt.cloud.oa.platform.domain.repository.PermissionRepository;
import xtt.cloud.oa.platform.infrastructure.cache.PermissionCache;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;
    private final PermissionCache permissionCache;
    
    public PermissionService(PermissionRepository permissionRepository, PermissionCache permissionCache) { 
        this.permissionRepository = permissionRepository;
        this.permissionCache = permissionCache;
    }

    public List<Permission> list() { return permissionRepository.findAll(); }
    public Optional<Permission> get(Long id) { return permissionRepository.findById(id); }

    @Transactional
    public Permission save(Permission permission) { return permissionRepository.save(permission); }

    @Transactional
    public void delete(Long id) { permissionRepository.deleteById(id); }

    // 对外服务方法
    public Optional<Permission> findByCode(String code) {
        return permissionRepository.findByCode(code);
    }

    public List<Permission> findByIds(List<Long> permissionIds) {
        return permissionRepository.findAllById(permissionIds);
    }

    public List<Permission> findByCodes(List<String> codes) {
        return permissionRepository.findByCodeIn(codes);
    }

    public List<Permission> findByType(String type) {
        return permissionRepository.findByType(type);
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
}


