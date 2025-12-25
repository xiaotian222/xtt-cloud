package xtt.cloud.oa.platform.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.platform.domain.entity.Permission;
import xtt.cloud.oa.platform.domain.entity.Role;
import xtt.cloud.oa.platform.domain.mapper.PermissionMapper;
import xtt.cloud.oa.platform.domain.mapper.RoleMapper;
import xtt.cloud.oa.platform.domain.mapper.RolePermissionMapper;
import xtt.cloud.oa.platform.domain.mapper.UserRoleMapper;
import xtt.cloud.oa.platform.infrastructure.cache.PermissionCache;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RoleService {
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final PermissionCache permissionCache;
    
    public RoleService(RoleMapper roleMapper, PermissionMapper permissionMapper, 
                      RolePermissionMapper rolePermissionMapper, UserRoleMapper userRoleMapper,
                      PermissionCache permissionCache) {
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.permissionCache = permissionCache;
    }

    public List<Role> list() { 
        return roleMapper.selectAll(); 
    }
    
    public Optional<Role> get(Long id) { 
        return roleMapper.findById(id); 
    }

    @Transactional
    public Role save(Role role) { 
        if (role.getId() == null) {
            // 新增角色
            role.setCreatedAt(LocalDateTime.now());
            role.setUpdatedAt(LocalDateTime.now());
            roleMapper.insert(role);
        } else {
            // 更新角色
            role.setUpdatedAt(LocalDateTime.now());
            roleMapper.update(role);
        }
        return role;
    }

    @Transactional
    public void delete(Long id) { 
        roleMapper.deleteById(id); 
    }

    @Transactional
    public Role grantPermissions(Long roleId, List<Long> permIds) {
        Role role = roleMapper.findById(roleId).orElseThrow();
        
        // 删除角色现有权限关联
        rolePermissionMapper.deleteRolePermissions(roleId);
        
        // 添加新权限关联
        if (!permIds.isEmpty()) {
            rolePermissionMapper.insertRolePermissions(roleId, permIds);
        }
        
        // 失效该角色下所有用户的权限缓存
        // 注意：这里需要查询该角色下的所有用户，然后失效缓存
        // 由于MyBatis没有直接的关联查询，这里简化处理
        permissionCache.evictAllUserPerms();
        
        return role;
    }

    // 对外服务方法
    public Optional<Role> findByCode(String code) {
        return roleMapper.findByCode(code);
    }

    public Set<Permission> getRolePermissions(Long roleId) {
        List<Permission> permissions = rolePermissionMapper.selectPermissionsByRoleId(roleId);
        return Set.copyOf(permissions);
    }

    public List<Role> findByIds(List<Long> roleIds) {
        return roleMapper.selectByIds(roleIds);
    }

    public List<Role> findByCodes(List<String> codes) {
        return roleMapper.selectByCodes(codes);
    }

    /**
     * 根据角色ID获取该角色下的用户ID列表
     */
    public List<Long> getUserIdsByRoleId(Long roleId) {
        return userRoleMapper.selectUserIdsByRoleId(roleId);
    }
}


