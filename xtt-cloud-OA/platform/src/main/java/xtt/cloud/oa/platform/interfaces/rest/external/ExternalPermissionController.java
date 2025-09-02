package xtt.cloud.oa.platform.interfaces.rest.external;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xtt.cloud.oa.platform.application.PermissionService;
import xtt.cloud.oa.common.dto.PermissionInfoDto;
import xtt.cloud.oa.platform.interfaces.mapper.PermissionMapper;

import java.util.List;
import java.util.Set;

/**
 * 对外权限服务接口
 * 供其他微服务调用，提供权限信息查询服务
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@RestController
@RequestMapping("/api/platform/external/permissions")
@RequiredArgsConstructor
public class ExternalPermissionController {

    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;

    /**
     * 根据权限代码获取权限信息
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<PermissionInfoDto> getPermissionByCode(@PathVariable String code) {
        return permissionService.findByCode(code)
                .map(permissionMapper::toPermissionInfoDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据权限ID获取权限信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<PermissionInfoDto> getPermissionById(@PathVariable Long id) {
        return permissionService.get(id)
                .map(permissionMapper::toPermissionInfoDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 验证权限是否存在
     */
    @GetMapping("/code/{code}/exists")
    public ResponseEntity<Boolean> permissionExists(@PathVariable String code) {
        boolean exists = permissionService.findByCode(code).isPresent();
        return ResponseEntity.ok(exists);
    }

    /**
     * 根据权限代码列表批量获取权限信息
     */
    @PostMapping("/batch/codes")
    public ResponseEntity<List<PermissionInfoDto>> getPermissionsByCodes(@RequestBody List<String> codes) {
        List<PermissionInfoDto> permissions = permissionService.findByCodes(codes).stream()
                .map(permissionMapper::toPermissionInfoDto)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(permissions);
    }

    /**
     * 根据权限ID列表批量获取权限信息
     */
    @PostMapping("/batch")
    public ResponseEntity<List<PermissionInfoDto>> getPermissionsByIds(@RequestBody List<Long> permissionIds) {
        List<PermissionInfoDto> permissions = permissionService.findByIds(permissionIds).stream()
                .map(permissionMapper::toPermissionInfoDto)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(permissions);
    }

    /**
     * 根据权限类型获取权限列表
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<PermissionInfoDto>> getPermissionsByType(@PathVariable String type) {
        List<PermissionInfoDto> permissions = permissionService.findByType(type).stream()
                .map(permissionMapper::toPermissionInfoDto)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(permissions);
    }

    /**
     * 获取所有权限列表
     */
    @GetMapping("/all")
    public ResponseEntity<List<PermissionInfoDto>> getAllPermissions() {
        List<PermissionInfoDto> permissions = permissionService.list().stream()
                .map(permissionMapper::toPermissionInfoDto)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(permissions);
    }

    /**
     * 验证用户是否拥有指定权限
     */
    @GetMapping("/user/{username}/has/{permissionCode}")
    public ResponseEntity<Boolean> userHasPermission(@PathVariable String username, @PathVariable String permissionCode) {
        boolean hasPermission = permissionService.userHasPermission(username, permissionCode);
        return ResponseEntity.ok(hasPermission);
    }

    /**
     * 验证用户是否拥有指定权限列表中的任意一个
     */
    @PostMapping("/user/{username}/has-any")
    public ResponseEntity<Boolean> userHasAnyPermission(@PathVariable String username, @RequestBody List<String> permissionCodes) {
        boolean hasAnyPermission = permissionService.userHasAnyPermission(username, permissionCodes);
        return ResponseEntity.ok(hasAnyPermission);
    }

    /**
     * 验证用户是否拥有指定权限列表中的所有权限
     */
    @PostMapping("/user/{username}/has-all")
    public ResponseEntity<Boolean> userHasAllPermissions(@PathVariable String username, @RequestBody List<String> permissionCodes) {
        boolean hasAllPermissions = permissionService.userHasAllPermissions(username, permissionCodes);
        return ResponseEntity.ok(hasAllPermissions);
    }
}
