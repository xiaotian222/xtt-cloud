package xtt.cloud.oa.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Auth Service Application
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "xtt.cloud.oa.auth.client")
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
