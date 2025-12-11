package xtt.cloud.oa.document;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("xtt.cloud.oa.document.domain.mapper")
public class DocumentApplication {
    public static void main(String[] args) {
        SpringApplication.run(DocumentApplication.class, args);
    }
}
