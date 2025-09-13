package xtt.cloud.oa.platform.interfaces.rest.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xtt.cloud.oa.platform.application.RoleService;
import xtt.cloud.oa.common.dto.RoleInfoDto;
import xtt.cloud.oa.platform.interfaces.mapper.RoleMapper;

import java.util.List;
import java.util.Set;

/**
 * 对外角色服务接口
 * 供其他微服务调用，提供角色信息查询服务
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@RestController
@RequestMapping("/api/platform/external/roles")
public class ExternalRoleController {

    private RoleService roleService;
    private RoleMapper roleMapper;

    @Autowired
    public ExternalRoleController(RoleService roleService, 
                                 @Qualifier("roleDtoMapper") RoleMapper roleMapper) {
        this.roleService = roleService;
        this.roleMapper = roleMapper;
    }

    /**
     * 根据角色代码获取角色信息
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<RoleInfoDto> getRoleByCode(@PathVariable String code) {
        return roleService.findByCode(code)
                .map(role -> {
                    RoleInfoDto dto = roleMapper.toRoleInfoDto(role);
                    // 加载角色权限
                    dto.setPermissions(roleMapper.toPermissionInfoDtos(roleService.getRolePermissions(role.getId())));
                    return dto;
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据角色ID获取角色信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleInfoDto> getRoleById(@PathVariable Long id) {
        return roleService.get(id)
                .map(role -> {
                    RoleInfoDto dto = roleMapper.toRoleInfoDto(role);
                    // 加载角色权限
                    dto.setPermissions(roleMapper.toPermissionInfoDtos(roleService.getRolePermissions(role.getId())));
                    return dto;
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据角色代码获取角色权限列表
     */
    @GetMapping("/code/{code}/permissions")
    public ResponseEntity<Set<String>> getRolePermissionsByCode(@PathVariable String code) {
        return roleService.findByCode(code)
                .map(role -> roleService.getRolePermissions(role.getId()))
                .map(permissions -> permissions.stream().map(perm -> perm.getCode()).collect(java.util.stream.Collectors.toSet()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据角色ID获取角色权限列表
     */
    @GetMapping("/{id}/permissions")
    public ResponseEntity<Set<String>> getRolePermissions(@PathVariable Long id) {
        Set<String> permissionCodes = roleService.getRolePermissions(id).stream()
                .map(permission -> permission.getCode())
                .collect(java.util.stream.Collectors.toSet());
        return ResponseEntity.ok(permissionCodes);
    }

    /**
     * 验证角色是否存在
     */
    @GetMapping("/code/{code}/exists")
    public ResponseEntity<Boolean> roleExists(@PathVariable String code) {
        boolean exists = roleService.findByCode(code).isPresent();
        return ResponseEntity.ok(exists);
    }

    /**
     * 根据角色代码列表批量获取角色信息
     */
    @PostMapping("/batch/codes")
    public ResponseEntity<List<RoleInfoDto>> getRolesByCodes(@RequestBody List<String> codes) {
        List<RoleInfoDto> roles = roleService.findByCodes(codes).stream()
                .map(role -> {
                    RoleInfoDto dto = roleMapper.toRoleInfoDto(role);
                    dto.setPermissions(roleMapper.toPermissionInfoDtos(roleService.getRolePermissions(role.getId())));
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(roles);
    }

    /**
     * 根据角色ID列表批量获取角色信息
     */
    @PostMapping("/batch")
    public ResponseEntity<List<RoleInfoDto>> getRolesByIds(@RequestBody List<Long> roleIds) {
        List<RoleInfoDto> roles = roleService.findByIds(roleIds).stream()
                .map(role -> {
                    RoleInfoDto dto = roleMapper.toRoleInfoDto(role);
                    dto.setPermissions(roleMapper.toPermissionInfoDtos(roleService.getRolePermissions(role.getId())));
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(roles);
    }

    /**
     * 获取所有角色列表（简化版，只包含基本信息）
     */
    @GetMapping("/all")
    public ResponseEntity<List<RoleInfoDto>> getAllRoles() {
        List<RoleInfoDto> roles = roleService.list().stream()
                .map(roleMapper::toRoleInfoDto)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(roles);
    }
}
