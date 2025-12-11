package xtt.cloud.oa.auth.service;

import xtt.cloud.oa.auth.dto.LoginRequest;
import xtt.cloud.oa.auth.dto.LoginResponse;
import xtt.cloud.oa.common.BusinessException;
import xtt.cloud.oa.common.dto.UserInfoDto;
import xtt.cloud.oa.auth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

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
            throw new BusinessException("用户名或密码错误");
        }

        // 获取用户信息
        Optional<UserInfoDto> userOpt = userService.findByUsername(username);
        if (!userOpt.isPresent()) {
            throw new BusinessException("用户不存在");
        }

        UserInfoDto user = userOpt.get();
        String role = userService.getUserPrimaryRole(username);

        // 生成 JWT token（包含用户 ID）
        String token = jwtUtil.generateToken(username, role, user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(username, user.getId());

        log.info("User logged in successfully: {}", username);
        return new LoginResponse(token, refreshToken, username, role, expiration);
    }

    /**
     * Refresh token
     */
    public LoginResponse refreshToken(String refreshToken) {
        try {
            String username = jwtUtil.extractUsername(refreshToken);
            
            // 验证 refresh token
            if (!jwtUtil.validateToken(refreshToken, username)) {
                throw new BusinessException("无效的刷新令牌");
            }

            // 检查 refresh token 是否在黑名单中
            if (tokenBlacklistService.isBlacklisted(refreshToken)) {
                throw new BusinessException("刷新令牌已失效");
            }

            // 获取用户信息
            Optional<UserInfoDto> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                throw new BusinessException("用户不存在");
            }

            UserInfoDto user = userOpt.get();
            String role = userService.getUserPrimaryRole(username);

            // 生成新的 token（包含用户 ID）
            String newToken = jwtUtil.generateToken(username, role, user.getId());
            String newRefreshToken = jwtUtil.generateRefreshToken(username, user.getId());

            log.info("Token refreshed successfully for user: {}", username);
            return new LoginResponse(newToken, newRefreshToken, username, role, expiration);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new BusinessException("刷新令牌失败");
        }
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token) {
        try {
            // 先检查 token 是否在黑名单中
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("Token is blacklisted");
                return false;
            }

            String username = jwtUtil.extractUsername(token);
            return jwtUtil.validateToken(token, username);
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return false;
        }
    }

    /**
     * Extract username from token
     */
    public String extractUsernameFromToken(String token) {
        try {
            String username = jwtUtil.extractUsername(token);
            if (username != null && jwtUtil.validateToken(token, username)) {
                return username;
            }
        } catch (Exception e) {
            // Token validation failed
        }
        return null;
    }

    /**
     * Logout - 将 token 加入黑名单
     */
    public void logout(String token) {
        try {
            // 将 token 加入黑名单
            tokenBlacklistService.addToBlacklist(token);
            
            // 可选：清除用户缓存（如果用户信息发生变化）
            String username = jwtUtil.extractUsername(token);
            if (username != null) {
                userService.clearUserCache(username);
            }
            
            log.info("User logged out: {}", username);
        } catch (Exception e) {
            log.error("Logout failed", e);
            // 即使加入黑名单失败，也记录日志，但不抛出异常
        }
    }
}
