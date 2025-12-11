package xtt.cloud.oa.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务
 * 用于管理已注销或需要失效的 Token
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);
    
    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${jwt.expiration:86400000}")
    private Long tokenExpiration;

    /**
     * 将 Token 加入黑名单
     * 
     * @param token JWT Token
     */
    public void addToBlacklist(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            // 将 token 存入 Redis，过期时间设置为 token 的剩余有效期
            // 这里简化处理，使用配置的过期时间
            long expirationSeconds = tokenExpiration / 1000;
            redisTemplate.opsForValue().set(key, "1", expirationSeconds, TimeUnit.SECONDS);
            log.info("Token added to blacklist: {}", token.substring(0, Math.min(20, token.length())) + "...");
        } catch (Exception e) {
            log.error("Failed to add token to blacklist", e);
            // 即使 Redis 操作失败，也不影响主流程
        }
    }

    /**
     * 检查 Token 是否在黑名单中
     * 
     * @param token JWT Token
     * @return true 如果在黑名单中，false 如果不在
     */
    public boolean isBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            String value = redisTemplate.opsForValue().get(key);
            return value != null;
        } catch (Exception e) {
            log.error("Failed to check token blacklist", e);
            // Redis 异常时，为了安全起见，返回 true（拒绝访问）
            return true;
        }
    }

    /**
     * 从黑名单中移除 Token（通常不需要，因为会自动过期）
     * 
     * @param token JWT Token
     */
    public void removeFromBlacklist(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.delete(key);
            log.info("Token removed from blacklist: {}", token.substring(0, Math.min(20, token.length())) + "...");
        } catch (Exception e) {
            log.error("Failed to remove token from blacklist", e);
        }
    }

    /**
     * 清空所有黑名单（谨慎使用）
     */
    public void clearBlacklist() {
        try {
            // 这里可以根据实际需求实现批量删除
            log.warn("Blacklist clear requested - this operation should be used with caution");
        } catch (Exception e) {
            log.error("Failed to clear blacklist", e);
        }
    }
}

