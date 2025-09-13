package xtt.cloud.oa.auth.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Feign 客户端配置
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Configuration
public class FeignConfig {

    /**
     * Feign 日志级别
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * 重试配置
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(1000, 2000, 3);
    }

    /**
     * 请求选项配置
     */
    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(5000, TimeUnit.MILLISECONDS, 10000, TimeUnit.MILLISECONDS, true);
    }
}
