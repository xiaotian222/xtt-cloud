package xtt.cloud.oa.auth.controller;

import xtt.cloud.oa.common.Result;
import xtt.cloud.oa.auth.dto.LoginRequest;
import xtt.cloud.oa.auth.dto.LoginResponse;
import xtt.cloud.oa.common.dto.UserInfoDto;
import xtt.cloud.oa.auth.service.AuthService;
import xtt.cloud.oa.auth.service.UserService;
import xtt.cloud.oa.auth.util.SecurityContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Optional;

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
     * Get current user info from SecurityContext
     */
    @GetMapping("/user")
    public Result<UserInfoDto> getCurrentUserInfo() {
        try {
            if (!SecurityContextUtil.isAuthenticated()) {
                return Result.error("User not authenticated");
            }
            
            String username = SecurityContextUtil.getCurrentUsername();
            if (username == null) {
                return Result.error("Username not found in token");
            }
            
            // 从 Platform 服务获取用户信息
            Optional<UserInfoDto> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                return Result.error("User not found");
            }
            
            return Result.success(userOpt.get());
        } catch (Exception e) {
            return Result.error("Failed to get user info: " + e.getMessage());
        }
    }

    // 根据用户 ID 获取用户信息的功能已移至 Platform 服务
    // 认证服务专注于认证和授权功能

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

    // 用户管理功能已移至 Platform 服务
    // 认证服务专注于认证和授权功能

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
