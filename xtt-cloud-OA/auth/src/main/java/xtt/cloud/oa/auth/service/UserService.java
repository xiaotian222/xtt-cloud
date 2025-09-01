package xtt.cloud.oa.auth.service;

import xtt.cloud.oa.auth.entity.User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * User Service
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Service
public class UserService {

    // 模拟用户数据，实际项目中应该从数据库获取
    private final Map<String, User> users = new HashMap<>();

    public UserService() {
        // 初始化模拟用户数据
        initUsers();
    }

    private void initUsers() {
        users.put("admin", new User("admin", "password", "ROLE_ADMIN", "admin@xtt.com"));
        users.put("user", new User("user", "password", "ROLE_USER", "user@xtt.com"));
        users.put("manager", new User("manager", "password", "ROLE_MANAGER", "manager@xtt.com"));
    }

    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    /**
     * Authenticate user
     */
    public boolean authenticate(String username, String password) {
        Optional<User> userOpt = findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 这里使用简单的密码比较，实际项目中应该使用 BCrypt
            return user.getPassword().equals(password) && user.getEnabled();
        }
        return false;
    }

    /**
     * Get all users (for testing purposes)
     */
    public Map<String, User> getAllUsers() {
        return new HashMap<>(users);
    }

    /**
     * Add new user
     */
    public User addUser(String username, String password, String role, String email) {
        User user = new User(username, password, role, email);
        users.put(username, user);
        return user;
    }

    /**
     * Update user
     */
    public User updateUser(String username, String role, String email, Boolean enabled) {
        User user = users.get(username);
        if (user != null) {
            user.setRole(role);
            user.setEmail(email);
            user.setEnabled(enabled);
        }
        return user;
    }

    /**
     * Delete user
     */
    public boolean deleteUser(String username) {
        return users.remove(username) != null;
    }
}
