package xtt.cloud.oa.auth.service;

import xtt.cloud.oa.auth.client.PlatformClient;
import xtt.cloud.oa.common.dto.UserInfoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * User Service - 从 Platform 服务获取用户信息，使用 Redis 缓存
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private PlatformClient platformClient;

    @Autowired
    private UserCacheService userCacheService;

    /**
     * Find user by username from Platform service with Redis cache
     */
    public Optional<UserInfoDto> findByUsername(String username) {
        try {
            // 先检查 Redis 缓存
            Optional<UserInfoDto> cachedUser = userCacheService.getUserFromCache(username);
            if (cachedUser.isPresent()) {
                log.debug("User found in cache: {}", username);
                return cachedUser;
            }
            
            // 从 Platform 服务获取用户信息
            UserInfoDto user = platformClient.getUserByUsername(username);
            if (user != null) {
                // 存入 Redis 缓存
                userCacheService.cacheUser(username, user);
                log.debug("User fetched from Platform service and cached: {}", username);
                return Optional.of(user);
            }
        } catch (Exception e) {
            // 如果 Platform 服务不可用，记录日志但不抛出异常
            log.error("Failed to get user from Platform service for username: {}", username, e);
        }
        return Optional.empty();
    }

    /**
     * Authenticate user using Platform service
     */
    public boolean authenticate(String username, String password) {
        try {
            // 使用 Platform 服务验证用户密码
            return platformClient.validateUserPassword(username, password);
        } catch (Exception e) {
            // 如果 Platform 服务不可用，记录日志并返回 false
            System.err.println("Failed to authenticate user via Platform service for username: " + username + ", error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get user's primary role (for JWT token generation)
     */
    public String getUserPrimaryRole(String username) {
        Optional<UserInfoDto> userOpt = findByUsername(username);
        if (userOpt.isPresent()) {
            UserInfoDto user = userOpt.get();
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                // 返回第一个角色的代码，去掉 ROLE_ 前缀
                return user.getRoles().iterator().next().getCode();
            }
        }
        return "USER"; // 默认角色
    }

    /**
     * Clear user cache (for logout or user update)
     */
    public void clearUserCache(String username) {
        userCacheService.evictUser(username);
        log.debug("User cache cleared for username: {}", username);
    }

    /**
     * Clear all user cache
     */
    public void clearAllUserCache() {
        userCacheService.clearAllCache();
        log.debug("All user cache cleared");
    }
}
