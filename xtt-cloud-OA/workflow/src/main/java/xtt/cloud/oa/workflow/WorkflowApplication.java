package xtt.cloud.oa.workflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 作者: HuTianRui
 * 日期: 2025/12/21
 * 描述: 这里是对代码的描述
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("xtt.cloud.oa.workflow.infrastructure.persistence.pojo.*")
public class WorkflowApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkflowApplication.class, args);
    }
}
