package xtt.cloud.oa.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import xtt.cloud.oa.common.dto.UserInfoDto;

import java.util.List;
import java.util.Set;

/**
 * Platform 服务客户端
 * 用于 Auth 服务调用 Platform 服务的对外接口
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@FeignClient(name = "platform", path = "/api/platform/external")
public interface PlatformClient {

    /**
     * 根据用户名获取用户信息
     */
    @GetMapping("/users/username/{username}")
    UserInfoDto getUserByUsername(@PathVariable("username") String username);

    /**
     * 根据用户ID获取用户信息
     */
    @GetMapping("/users/{id}")
    UserInfoDto getUserById(@PathVariable("id") Long id);

    /**
     * 根据用户名获取用户权限
     */
    @GetMapping("/users/username/{username}/permissions")
    Set<String> getUserPermissionsByUsername(@PathVariable("username") String username);

    /**
     * 根据用户ID获取用户权限
     */
    @GetMapping("/users/{id}/permissions")
    Set<String> getUserPermissions(@PathVariable("id") Long id);

    /**
     * 根据用户名获取用户角色
     */
    @GetMapping("/users/username/{username}/roles")
    Set<String> getUserRolesByUsername(@PathVariable("username") String username);

    /**
     * 验证用户是否存在
     */
    @GetMapping("/users/username/{username}/exists")
    Boolean userExists(@PathVariable("username") String username);

    /**
     * 批量获取用户信息（根据用户名列表）
     */
    @PostMapping("/users/batch/usernames")
    List<UserInfoDto> getUsersByUsernames(@RequestBody List<String> usernames);

    /**
     * 验证用户是否拥有指定权限
     */
    @GetMapping("/permissions/user/{username}/has/{permissionCode}")
    Boolean userHasPermission(@PathVariable("username") String username, 
                             @PathVariable("permissionCode") String permissionCode);

    /**
     * 验证用户是否拥有指定权限列表中的任意一个
     */
    @PostMapping("/permissions/user/{username}/has-any")
    Boolean userHasAnyPermission(@PathVariable("username") String username, 
                                @RequestBody List<String> permissionCodes);
}
