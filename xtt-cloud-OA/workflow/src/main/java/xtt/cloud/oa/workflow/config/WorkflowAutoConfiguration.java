package xtt.cloud.oa.workflow.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import xtt.cloud.oa.workflow.application.flow.FlowApplicationService;

import javax.sql.DataSource;

/**
 * Workflow 自动配置类
 * 
 * @author XTT Cloud
 */
@Configuration
@ConditionalOnProperty(prefix = "xtt.workflow", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({FlowApplicationService.class, DataSource.class})
@EnableConfigurationProperties(WorkflowProperties.class)
@Import({
    WorkflowMyBatisConfig.class,
    WorkflowWebConfig.class,
    WorkflowAsyncConfig.class
})
@ComponentScan(basePackages = {
    "xtt.cloud.oa.workflow.domain",
    "xtt.cloud.oa.workflow.application",
    "xtt.cloud.oa.workflow.infrastructure",
    "xtt.cloud.oa.workflow.interfaces"
})
@EnableFeignClients(basePackages = "xtt.cloud.oa.workflow.infrastructure.external.client")
public class WorkflowAutoConfiguration {
    // 自动配置逻辑已通过 @Import 和 @ComponentScan 完成
}

