package xtt.cloud.oa.platform.interfaces.rest.external;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xtt.cloud.oa.platform.application.UserService;
import xtt.cloud.oa.common.dto.UserInfoDto;
import xtt.cloud.oa.platform.interfaces.mapper.UserMapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 对外用户服务接口
 * 供其他微服务调用，提供用户信息查询服务
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@RestController
@RequestMapping("/api/platform/external/users")
@RequiredArgsConstructor
public class ExternalUserController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * 根据用户名获取用户信息（包含角色和权限）
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserInfoDto> getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(user -> {
                    UserInfoDto dto = userMapper.toUserInfoDto(user);
                    // 加载用户角色
                    dto.setRoles(userMapper.toRoleInfoDtos(userService.getUserRoles(user.getId())));
                    // 加载用户权限
                    dto.setPermissions(userService.getUserPermissions(user.getId()));
                    // 加载用户部门
                    dto.setDepartments(userMapper.toDeptInfoDtos(userService.getUserDepartments(user.getId())));
                    return dto;
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据用户ID获取用户信息（包含角色和权限）
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserInfoDto> getUserById(@PathVariable Long id) {
        return userService.get(id)
                .map(user -> {
                    UserInfoDto dto = userMapper.toUserInfoDto(user);
                    // 加载用户角色
                    dto.setRoles(userMapper.toRoleInfoDtos(userService.getUserRoles(user.getId())));
                    // 加载用户权限
                    dto.setPermissions(userService.getUserPermissions(user.getId()));
                    // 加载用户部门
                    dto.setDepartments(userMapper.toDeptInfoDtos(userService.getUserDepartments(user.getId())));
                    return dto;
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据用户名获取用户权限列表
     */
    @GetMapping("/username/{username}/permissions")
    public ResponseEntity<Set<String>> getUserPermissionsByUsername(@PathVariable String username) {
        Set<String> permissions = userService.getUserPermissionsByUsername(username);
        return ResponseEntity.ok(permissions);
    }

    /**
     * 根据用户ID获取用户权限列表
     */
    @GetMapping("/{id}/permissions")
    public ResponseEntity<Set<String>> getUserPermissions(@PathVariable Long id) {
        Set<String> permissions = userService.getUserPermissions(id);
        return ResponseEntity.ok(permissions);
    }

    /**
     * 根据用户名获取用户角色列表
     */
    @GetMapping("/username/{username}/roles")
    public ResponseEntity<Set<String>> getUserRolesByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(user -> userService.getUserRoles(user.getId()))
                .map(roles -> roles.stream().map(role -> role.getCode()).collect(java.util.stream.Collectors.toSet()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据用户ID获取用户角色列表
     */
    @GetMapping("/{id}/roles")
    public ResponseEntity<Set<String>> getUserRoles(@PathVariable Long id) {
        Set<String> roleCodes = userService.getUserRoles(id).stream()
                .map(role -> role.getCode())
                .collect(java.util.stream.Collectors.toSet());
        return ResponseEntity.ok(roleCodes);
    }

    /**
     * 验证用户是否存在
     */
    @GetMapping("/username/{username}/exists")
    public ResponseEntity<Boolean> userExists(@PathVariable String username) {
        boolean exists = userService.findByUsername(username).isPresent();
        return ResponseEntity.ok(exists);
    }

    /**
     * 根据用户ID列表批量获取用户信息
     */
    @PostMapping("/batch")
    public ResponseEntity<List<UserInfoDto>> getUsersByIds(@RequestBody List<Long> userIds) {
        List<UserInfoDto> users = userService.findByIds(userIds).stream()
                .map(user -> {
                    UserInfoDto dto = userMapper.toUserInfoDto(user);
                    dto.setRoles(userMapper.toRoleInfoDtos(userService.getUserRoles(user.getId())));
                    dto.setPermissions(userService.getUserPermissions(user.getId()));
                    dto.setDepartments(userMapper.toDeptInfoDtos(userService.getUserDepartments(user.getId())));
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(users);
    }

    /**
     * 根据用户名列表批量获取用户信息
     */
    @PostMapping("/batch/usernames")
    public ResponseEntity<List<UserInfoDto>> getUsersByUsernames(@RequestBody List<String> usernames) {
        List<UserInfoDto> users = userService.findByUsernames(usernames).stream()
                .map(user -> {
                    UserInfoDto dto = userMapper.toUserInfoDto(user);
                    dto.setRoles(userMapper.toRoleInfoDtos(userService.getUserRoles(user.getId())));
                    dto.setPermissions(userService.getUserPermissions(user.getId()));
                    dto.setDepartments(userMapper.toDeptInfoDtos(userService.getUserDepartments(user.getId())));
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(users);
    }
}
