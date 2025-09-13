package xtt.cloud.gateway.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 作者: HuTianRui
 * 日期: 2025/9/12
 * 描述: 这里是对代码的描述
 */
@Configuration
public class RouteDefinitionPrinter {

    @Bean
    public ApplicationRunner printRouteDefinitions(RouteDefinitionLocator locator) {
        return args -> {
            locator.getRouteDefinitions().subscribe(def -> {
                System.out.println("==== Route Definition from Nacos ====");
                System.out.println("ID: " + def.getId());
                System.out.println("URI: " + def.getUri());
                System.out.println("Predicates: " + def.getPredicates());
                System.out.println("Filters: " + def.getFilters());
            });
        };
    }
}

