package xtt.cloud.oa.platform.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HelloController {
    @GetMapping("/healthz")
    public Map<String, Object> health() {
        return Map.of("status", "UP");
    }
}


