package xtt.cloud.oa.auth.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xtt.cloud.oa.common.dto.UserInfoDto;

/**
 * Platform 服务客户端降级处理
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Component
public class PlatformClientFallback implements PlatformClient {
    
    private static final Logger logger = LoggerFactory.getLogger(PlatformClientFallback.class);

    @Override
    public UserInfoDto getUserByUsername(String username) {
        logger.warn("Platform service is unavailable, fallback triggered for username: {}", username);
        return null;
    }

    @Override
    public boolean validateUserPassword(String username, String password) {
        logger.warn("Platform service is unavailable, fallback triggered for username: {}", username);
        return false;
    }
}
