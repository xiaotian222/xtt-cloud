package xtt.cloud.oa.workflow.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Workflow 异步配置
 * 
 * @author XTT Cloud
 */
@Configuration
@ConditionalOnProperty(prefix = "xtt.workflow", name = "enable-async", havingValue = "true", matchIfMissing = true)
@EnableAsync
public class WorkflowAsyncConfig {
    
    /**
     * 配置异步任务执行器
     */
    @Bean(name = "workflowTaskExecutor")
    public Executor workflowTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("workflow-async-");
        executor.initialize();
        return executor;
    }
}

