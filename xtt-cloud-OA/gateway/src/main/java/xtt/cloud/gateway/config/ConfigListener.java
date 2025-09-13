package xtt.cloud.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 配置监听器 - 打印从 Nacos 拉取的配置信息
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Component
public class ConfigListener {

    private static final Logger log = LoggerFactory.getLogger(ConfigListener.class);

    @Autowired
    private Environment environment;

    @Autowired
    private RouteLocator routeLocator;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        printNacosConfig();
        printGatewayRoutes();
    }

    /**
     * 打印 Nacos 配置信息
     */
    private void printNacosConfig() {
        log.info("=== Nacos Configuration Info ===");
        
        // 打印 Nacos 相关配置
        String nacosServer = environment.getProperty("spring.cloud.nacos.config.server-addr");
        String nacosGroup = environment.getProperty("spring.cloud.nacos.config.group");
        String nacosFileExtension = environment.getProperty("spring.cloud.nacos.config.file-extension");
        
        log.info("Nacos Server: {}", nacosServer);
        log.info("Nacos Group: {}", nacosGroup);
        log.info("Nacos File Extension: {}", nacosFileExtension);
        
        // 打印 JWT 配置
        String jwtSecret = environment.getProperty("jwt.secret");
        String jwtExpiration = environment.getProperty("jwt.expiration");
        String jwtRefreshExpiration = environment.getProperty("jwt.refresh-expiration");
        
        log.info("JWT Secret: {}", jwtSecret != null ? "***" + jwtSecret.substring(jwtSecret.length() - 4) : "null");
        log.info("JWT Expiration: {} ms", jwtExpiration);
        log.info("JWT Refresh Expiration: {} ms", jwtRefreshExpiration);
        
        // 打印 Gateway Auth 配置
        String authEnabled = environment.getProperty("gateway.auth.enabled");
        String skipPaths = environment.getProperty("gateway.auth.skip-paths");
        
        log.info("Gateway Auth Enabled: {}", authEnabled);
        log.info("Gateway Auth Skip Paths: {}", skipPaths);
        
        // 打印所有配置属性（过滤敏感信息）
        log.info("=== All Configuration Properties ===");
//        Map<String, String> allProps = (Map<String, String>) environment.getPropertySources()
//                .stream()
//                .filter(propertySource -> propertySource.getName().contains("nacos"))
//                .findFirst()
//                .map(propertySource -> (Map<String, String>) propertySource.getSource())
//                .orElse(null);
//
//        if (allProps != null) {
//            allProps.forEach((key, value) -> {
//                if (key.toLowerCase().contains("secret") || key.toLowerCase().contains("password")) {
//                    log.info("{}: ***", key);
//                } else {
//                    log.info("{}: {}", key, value);
//                }
//            });
//        }
        
        log.info("=== End Nacos Configuration Info ===");
    }

    /**
     * 打印网关路由信息
     */
    private void printGatewayRoutes() {
        log.info("=== Gateway Routes Info ===");
        
        Flux<Route> routes = routeLocator.getRoutes();
        routes.subscribe(route -> {
            log.info("Route ID: {}", route.getId());
            log.info("Route URI: {}", route.getUri());
            log.info("Route Predicates: {}", route.getPredicate().toString());
            log.info("Route Filters: {}", route.getFilters());
            log.info("Route Order: {}", route.getOrder());
            log.info("---");
        });
        
        log.info("=== End Gateway Routes Info ===");
    }
}
