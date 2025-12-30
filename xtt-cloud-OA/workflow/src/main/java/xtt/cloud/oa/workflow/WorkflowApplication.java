package xtt.cloud.oa.workflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Workflow 独立启动类（可选）
 * 
 * <p>注意：此启动类仅用于 workflow 作为独立服务运行。
 * 如果 workflow 作为 starter 集成到其他应用中，不需要此启动类。
 * 其他应用只需要引入 workflow 依赖，Spring Boot 会自动加载 WorkflowAutoConfiguration。
 * 
 * <p>作为 starter 使用时，应用启动类需要：
 * <ul>
 *   <li>添加 @MapperScan("xtt.cloud.oa.workflow.infrastructure.persistence.mapper")（如果应用没有全局配置）</li>
 *   <li>配置数据源（workflow 会使用应用的主数据源）</li>
 *   <li>在配置文件中设置 xtt.workflow.enabled=true（默认已启用）</li>
 * </ul>
 * 
 * @author HuTianRui
 * @date 2025/12/21
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "xtt.cloud.oa.workflow.infrastructure.external.client")
@EnableAsync
@MapperScan("xtt.cloud.oa.workflow.infrastructure.persistence.mapper")
public class WorkflowApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkflowApplication.class, args);
    }
}
