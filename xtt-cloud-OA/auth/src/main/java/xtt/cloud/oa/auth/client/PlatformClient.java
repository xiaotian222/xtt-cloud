package xtt.cloud.oa.auth.client;

import xtt.cloud.oa.common.dto.UserInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Platform 服务客户端
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@FeignClient(name = "platform", configuration = xtt.cloud.oa.auth.config.FeignConfig.class)
public interface PlatformClient {

    /**
     * 根据用户名获取用户信息
     */
    @GetMapping("/api/platform/external/users/username/{username}")
    UserInfoDto getUserByUsername(@PathVariable("username") String username);

    /**
     * 验证用户密码
     */
    @GetMapping("/api/platform/external/users/validate")
    boolean validateUserPassword(@RequestParam("username") String username, 
                                @RequestParam("password") String password);
}