package xtt.cloud.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 认证配置类
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Component
@ConfigurationProperties(prefix = "gateway.auth")
public class AuthConfig {

    /**
     * 不需要认证的路径列表
     */
    private List<String> skipPaths = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register", 
            "/api/auth/refresh",
            "/api/auth/logout",
            "/actuator/health",
            "/actuator/info"
    );

    /**
     * 是否启用认证拦截
     */
    private boolean enabled = true;

    public List<String> getSkipPaths() {
        return skipPaths;
    }

    public void setSkipPaths(List<String> skipPaths) {
        this.skipPaths = skipPaths;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
