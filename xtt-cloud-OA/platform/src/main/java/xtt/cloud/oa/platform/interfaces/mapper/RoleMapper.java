package xtt.cloud.oa.platform.interfaces.mapper;

import org.springframework.stereotype.Component;
import xtt.cloud.oa.common.dto.PermissionInfoDto;
import xtt.cloud.oa.common.dto.RoleInfoDto;
import xtt.cloud.oa.platform.domain.entity.Permission;
import xtt.cloud.oa.platform.domain.entity.Role;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色映射器
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Component("roleDtoMapper")
public class RoleMapper {

    public RoleInfoDto toRoleInfoDto(Role role) {
        if (role == null) {
            return null;
        }
        
        RoleInfoDto dto = new RoleInfoDto();
        dto.setId(role.getId());
        dto.setCode(role.getCode());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        
        return dto;
    }

    public Set<PermissionInfoDto> toPermissionInfoDtos(Set<Permission> permissions) {
        if (permissions == null) {
            return null;
        }
        
        return permissions.stream()
                .map(this::toPermissionInfoDto)
                .collect(Collectors.toSet());
    }

    private PermissionInfoDto toPermissionInfoDto(Permission permission) {
        if (permission == null) {
            return null;
        }
        
        PermissionInfoDto dto = new PermissionInfoDto();
        dto.setId(permission.getId());
        dto.setCode(permission.getCode());
        dto.setName(permission.getName());
        dto.setType(permission.getType());
        dto.setCreatedAt(permission.getCreatedAt());
        
        return dto;
    }
}
