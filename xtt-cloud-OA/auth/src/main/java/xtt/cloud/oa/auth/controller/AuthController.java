package xtt.cloud.oa.auth.controller;

import xtt.cloud.oa.common.Result;
import xtt.cloud.oa.auth.dto.LoginRequest;
import xtt.cloud.oa.auth.dto.LoginResponse;
import xtt.cloud.oa.auth.entity.User;
import xtt.cloud.oa.auth.service.AuthService;
import xtt.cloud.oa.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;

/**
 * Authentication Controller
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    /**
     * Login endpoint
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("Login failed: " + e.getMessage());
        }
    }

    /**
     * Refresh token endpoint
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        try {
            LoginResponse response = authService.refreshToken(refreshToken);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("Token refresh failed: " + e.getMessage());
        }
    }

    /**
     * Validate token endpoint
     */
    @GetMapping("/validate")
    public Result<Boolean> validateToken(@RequestParam String token) {
        try {
            boolean isValid = authService.validateToken(token);
            return Result.success(isValid);
        } catch (Exception e) {
            return Result.error("Token validation failed: " + e.getMessage());
        }
    }

    /**
     * Get user info from token
     */
    @GetMapping("/user")
    public Result<User> getUserInfo(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return Result.error("No token provided");
            }
            
            User user = authService.getUserFromToken(token);
            if (user == null) {
                return Result.error("Invalid token");
            }
            
            // 不返回密码信息
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("Failed to get user info: " + e.getMessage());
        }
    }

    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token != null) {
                authService.logout(token);
            }
            return Result.success("Logout successful");
        } catch (Exception e) {
            return Result.error("Logout failed: " + e.getMessage());
        }
    }

    /**
     * Get all users (for testing purposes)
     */
    @GetMapping("/users")
    public Result<Map<String, User>> getAllUsers() {
        try {
            Map<String, User> users = userService.getAllUsers();
            // 不返回密码信息
            users.values().forEach(user -> user.setPassword(null));
            return Result.success(users);
        } catch (Exception e) {
            return Result.error("Failed to get users: " + e.getMessage());
        }
    }

    /**
     * Add new user (for testing purposes)
     */
    @PostMapping("/users")
    public Result<User> addUser(@RequestParam String username, 
                               @RequestParam String password, 
                               @RequestParam String role, 
                               @RequestParam(required = false) String email) {
        try {
            User user = userService.addUser(username, password, role, email);
            user.setPassword(null); // 不返回密码
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("Failed to add user: " + e.getMessage());
        }
    }

    /**
     * Extract token from request header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
