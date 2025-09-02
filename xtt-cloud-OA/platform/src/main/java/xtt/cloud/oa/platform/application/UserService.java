package xtt.cloud.oa.platform.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.platform.domain.entity.Role;
import xtt.cloud.oa.platform.domain.entity.User;
import xtt.cloud.oa.platform.domain.repository.RoleRepository;
import xtt.cloud.oa.platform.domain.repository.UserRepository;
import xtt.cloud.oa.platform.infrastructure.cache.PermissionCache;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionCache permissionCache;
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PermissionCache permissionCache) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionCache = permissionCache;
    }

    public List<User> list() { return userRepository.findAll(); }
    public Optional<User> get(Long id) { return userRepository.findById(id); }

    @Transactional
    public User save(User user) { return userRepository.save(user); }

    @Transactional
    public void delete(Long id) { userRepository.deleteById(id); }

    @Transactional
    public User grantRoles(Long userId, List<Long> roleIds) {
        User user = userRepository.findById(userId).orElseThrow();
        List<Role> roles = roleRepository.findAllById(roleIds);
        user.getRoles().clear();
        user.getRoles().addAll(roles);
        User saved = userRepository.save(user);
        // 失效该用户的权限缓存
        permissionCache.evictUserPerms(userId);
        return saved;
    }

    public Set<String> getUserPermissions(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return permissionCache.loadUserPermissions(user);
    }

    public Set<String> getUserPermissionsByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return permissionCache.loadUserPermissions(user);
    }
}


