package xtt.cloud.oa.platform.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.platform.domain.entity.Permission;
import xtt.cloud.oa.platform.domain.entity.Role;
import xtt.cloud.oa.platform.domain.repository.RoleRepository;
import xtt.cloud.oa.platform.domain.repository.PermissionRepository;
import xtt.cloud.oa.platform.infrastructure.cache.PermissionCache;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionCache permissionCache;
    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository, PermissionCache permissionCache) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.permissionCache = permissionCache;
    }

    public List<Role> list() { return roleRepository.findAll(); }
    public Optional<Role> get(Long id) { return roleRepository.findById(id); }

    @Transactional
    public Role save(Role role) { return roleRepository.save(role); }

    @Transactional
    public void delete(Long id) { roleRepository.deleteById(id); }

    @Transactional
    public Role grantPermissions(Long roleId, List<Long> permIds) {
        Role role = roleRepository.findById(roleId).orElseThrow();
        var perms = permissionRepository.findAllById(permIds);
        role.getPermissions().clear();
        role.getPermissions().addAll(perms);
        Role saved = roleRepository.save(role);
        // 失效该角色下所有用户的权限缓存
        if (saved.getUsers() != null) {
            saved.getUsers().forEach(u -> permissionCache.evictUserPerms(u.getId()));
        }
        return saved;
    }

    // 对外服务方法
    public Optional<Role> findByCode(String code) {
        return roleRepository.findByCode(code);
    }

    public Set<Permission> getRolePermissions(Long roleId) {
        Role role = roleRepository.findById(roleId).orElseThrow();
        return role.getPermissions();
    }

    public List<Role> findByIds(List<Long> roleIds) {
        return roleRepository.findAllById(roleIds);
    }

    public List<Role> findByCodes(List<String> codes) {
        return roleRepository.findByCodeIn(codes);
    }
}


