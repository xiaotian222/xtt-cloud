package xtt.cloud.oa.platform.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.platform.domain.entity.Department;
import xtt.cloud.oa.platform.domain.entity.Role;
import xtt.cloud.oa.platform.domain.entity.User;
import xtt.cloud.oa.platform.domain.mapper.DepartmentMapper;
import xtt.cloud.oa.platform.domain.mapper.RoleMapper;
import xtt.cloud.oa.platform.domain.mapper.UserDepartmentMapper;
import xtt.cloud.oa.platform.domain.mapper.UserMapper;
import xtt.cloud.oa.platform.domain.mapper.UserRoleMapper;
import xtt.cloud.oa.platform.infrastructure.cache.PermissionCache;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final DepartmentMapper departmentMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserDepartmentMapper userDepartmentMapper;
    private final PermissionCache permissionCache;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserMapper userMapper, RoleMapper roleMapper, DepartmentMapper departmentMapper,
                      UserRoleMapper userRoleMapper, UserDepartmentMapper userDepartmentMapper,
                      PermissionCache permissionCache, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.departmentMapper = departmentMapper;
        this.userRoleMapper = userRoleMapper;
        this.userDepartmentMapper = userDepartmentMapper;
        this.permissionCache = permissionCache;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> list() { 
        return userMapper.selectAll(); 
    }
    
    public Optional<User> get(Long id) { 
        return userMapper.findById(id); 
    }

    @Transactional
    public User save(User user) { 
        if (user.getId() == null) {
            // 新增用户
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(user);
        } else {
            // 更新用户
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.update(user);
        }
        return user;
    }

    @Transactional
    public void delete(Long id) { 
        userMapper.deleteById(id); 
    }

    @Transactional
    public User grantRoles(Long userId, List<Long> roleIds) {
        User user = userMapper.findById(userId).orElseThrow();
        
        // 删除用户现有角色关联
        userRoleMapper.deleteUserRoles(userId);
        
        // 添加新角色关联
        if (!roleIds.isEmpty()) {
            userRoleMapper.insertUserRoles(userId, roleIds);
        }
        
        // 失效该用户的权限缓存
        permissionCache.evictUserPerms(userId);
        return user;
    }

    public Set<String> getUserPermissions(Long userId) {
        User user = userMapper.findById(userId).orElseThrow();
        return permissionCache.loadUserPermissions(user);
    }

    public Set<String> getUserPermissionsByUsername(String username) {
        User user = userMapper.findByUsername(username).orElseThrow();
        return permissionCache.loadUserPermissions(user);
    }

    // 对外服务方法
    public Optional<User> findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    public Set<Role> getUserRoles(Long userId) {
        List<Role> roles = userRoleMapper.selectRolesByUserId(userId);
        return Set.copyOf(roles);
    }

    public Set<Department> getUserDepartments(Long userId) {
        List<Department> departments = userDepartmentMapper.selectDepartmentsByUserId(userId);
        return Set.copyOf(departments);
    }

    public List<User> findByIds(List<Long> userIds) {
        return userMapper.selectByIds(userIds);
    }

    public List<User> findByUsernames(List<String> usernames) {
        return userMapper.selectByUsernames(usernames);
    }

    /**
     * 验证用户密码
     */
    public boolean validateUserPassword(String username, String password) {
        Optional<User> userOpt = userMapper.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 检查用户状态
            if (user.getStatus() != null && user.getStatus() != 1) {
                return false; // 用户被禁用
            }
            // 验证密码
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }
}


