package xtt.cloud.oa.platform.application;

import org.springframework.stereotype.Service;
import xtt.cloud.oa.platform.domain.entity.Permission;
import xtt.cloud.oa.platform.domain.entity.Role;
import xtt.cloud.oa.platform.domain.mapper.RolePermissionMapper;
import xtt.cloud.oa.platform.domain.mapper.UserMapper;
import xtt.cloud.oa.platform.domain.mapper.UserRoleMapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限领域服务
 * 负责权限相关的业务逻辑计算，不涉及缓存
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Service
public class PermissionDomainService {
    
    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserMapper userMapper;
    
    public PermissionDomainService(UserRoleMapper userRoleMapper, 
                                  RolePermissionMapper rolePermissionMapper,
                                  UserMapper userMapper) {
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.userMapper = userMapper;
    }
    
    /**
     * 计算用户权限
     */
    public Set<String> calculateUserPermissions(Long userId) {
        // 获取用户角色
        List<Role> roles = userRoleMapper.selectRolesByUserId(userId);
        
        // 获取角色权限
        return roles.stream()
                .flatMap(role -> {
                    List<Permission> permissions = rolePermissionMapper.selectPermissionsByRoleId(role.getId());
                    return permissions.stream();
                })
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }
    
    /**
     * 根据用户名计算用户权限
     */
    public Set<String> calculateUserPermissionsByUsername(String username) {
        return userMapper.findByUsername(username)
                .map(user -> calculateUserPermissions(user.getId()))
                .orElse(Set.of());
    }
}
