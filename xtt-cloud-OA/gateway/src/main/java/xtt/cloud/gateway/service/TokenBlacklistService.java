package xtt.cloud.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Token 黑名单服务（网关版）
 * 用于检查 Token 是否在黑名单中
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);
    
    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    /**
     * 检查 Token 是否在黑名单中（响应式）
     * 
     * @param token JWT Token
     * @return Mono<Boolean> true 如果在黑名单中，false 如果不在
     */
    public Mono<Boolean> isBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            return redisTemplate.hasKey(key)
                    .onErrorReturn(true) // Redis 异常时，为了安全起见，返回 true（拒绝访问）
                    .doOnError(error -> log.error("Failed to check token blacklist", error));
        } catch (Exception e) {
            log.error("Failed to check token blacklist", e);
            // 异常时返回 true，拒绝访问
            return Mono.just(true);
        }
    }
}

