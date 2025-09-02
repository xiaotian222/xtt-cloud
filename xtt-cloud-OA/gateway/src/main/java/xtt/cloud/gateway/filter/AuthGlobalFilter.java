package xtt.cloud.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import xtt.cloud.gateway.config.AuthConfig;
import xtt.cloud.gateway.util.JwtUtil;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证全局过滤器
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthConfig authConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        log.debug("Gateway Auth Filter - Request path: {}", path);

        // 检查是否启用认证拦截
        if (!authConfig.isEnabled()) {
            log.debug("Auth filter is disabled");
            return chain.filter(exchange);
        }

        // 检查是否为不需要认证的路径
        if (isSkipAuth(path)) {
            log.debug("Skip auth for path: {}", path);
            return chain.filter(exchange);
        }

        // 获取 Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return unauthorizedResponse(exchange, "缺少或无效的认证信息");
        }

        // 提取 token
        String token = authHeader.substring(7);
        
        try {
            // 验证 token
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid token for path: {}", path);
                return unauthorizedResponse(exchange, "无效的认证令牌");
            }

            // 提取用户信息并添加到请求头
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);
            
            // 将用户信息添加到请求头，传递给下游服务
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Name", username)
                    .header("X-User-Role", role != null ? role : "")
                    .build();

            log.debug("User authenticated: {} with role: {}", username, role);
            
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
            
        } catch (Exception e) {
            log.error("Token validation error for path: {}, error: {}", path, e.getMessage());
            return unauthorizedResponse(exchange, "认证令牌验证失败");
        }
    }

    /**
     * 检查路径是否需要跳过认证
     */
    private boolean isSkipAuth(String path) {
        return authConfig.getSkipPaths().stream().anyMatch(path::startsWith);
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", message);
        result.put("data", null);

        try {
            String body = objectMapper.writeValueAsString(result);
            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error writing unauthorized response", e);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        // 设置较高的优先级，确保在路由之前执行
        return -100;
    }
}
