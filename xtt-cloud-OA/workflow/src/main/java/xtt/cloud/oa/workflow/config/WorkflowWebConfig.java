package xtt.cloud.oa.workflow.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;

/**
 * Workflow Web 配置
 * 
 * @author XTT Cloud
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "xtt.workflow", name = "enable-web-api", havingValue = "true", matchIfMissing = true)
public class WorkflowWebConfig {
    // Web 相关配置，如拦截器、跨域等
    // REST Controller 会自动被 Spring 扫描并注册
}

