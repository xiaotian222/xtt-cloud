package xtt.cloud.oa.auth.controller;

import xtt.cloud.oa.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * Test Controller
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*")
public class TestController {

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Auth service is running!");
    }

    /**
     * Public endpoint
     */
    @GetMapping("/public")
    public Result<String> publicEndpoint() {
        return Result.success("This is a public endpoint");
    }

    /**
     * Protected endpoint
     */
    @GetMapping("/protected")
    public Result<String> protectedEndpoint() {
        return Result.success("This is a protected endpoint");
    }
}
