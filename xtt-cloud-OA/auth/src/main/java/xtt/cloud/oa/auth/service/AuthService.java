package xtt.cloud.oa.auth.service;

import xtt.cloud.oa.auth.dto.LoginRequest;
import xtt.cloud.oa.auth.dto.LoginResponse;
import xtt.cloud.oa.auth.entity.User;
import xtt.cloud.oa.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Authentication Service
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Login and generate JWT token
     */
    public LoginResponse login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // 验证用户凭据
        if (!userService.authenticate(username, password)) {
            throw new RuntimeException("Invalid username or password");
        }

        // 获取用户信息
        Optional<User> userOpt = userService.findByUsername(username);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        // 生成 JWT token
        String token = jwtUtil.generateToken(username, user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(username);

        return new LoginResponse(token, refreshToken, username, user.getRole(), expiration);
    }

    /**
     * Refresh token
     */
    public LoginResponse refreshToken(String refreshToken) {
        try {
            String username = jwtUtil.extractUsername(refreshToken);
            
            // 验证 refresh token
            if (!jwtUtil.validateToken(refreshToken, username)) {
                throw new RuntimeException("Invalid refresh token");
            }

            // 获取用户信息
            Optional<User> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                throw new RuntimeException("User not found");
            }

            User user = userOpt.get();

            // 生成新的 token
            String newToken = jwtUtil.generateToken(username, user.getRole());
            String newRefreshToken = jwtUtil.generateRefreshToken(username);

            return new LoginResponse(newToken, newRefreshToken, username, user.getRole(), expiration);
        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token");
        }
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token) {
        try {
            String username = jwtUtil.extractUsername(token);
            return jwtUtil.validateToken(token, username);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get user info from token
     */
    public User getUserFromToken(String token) {
        try {
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            return userOpt.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Logout (in JWT, logout is typically handled on the client side)
     * This method can be used to invalidate tokens if needed
     */
    public void logout(String token) {
        // 在实际项目中，可以将 token 加入黑名单
        // 这里只是简单的实现
        System.out.println("User logged out: " + jwtUtil.extractUsername(token));
    }
}
