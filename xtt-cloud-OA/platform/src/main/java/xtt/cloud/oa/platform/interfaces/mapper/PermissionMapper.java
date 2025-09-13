package xtt.cloud.oa.platform.interfaces.mapper;

import org.springframework.stereotype.Component;
import xtt.cloud.oa.common.dto.PermissionInfoDto;
import xtt.cloud.oa.platform.domain.entity.Permission;

/**
 * 权限映射器
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Component("permissionDtoMapper")
public class PermissionMapper {

    public PermissionInfoDto toPermissionInfoDto(Permission permission) {
        if (permission == null) {
            return null;
        }
        
        PermissionInfoDto dto = new PermissionInfoDto();
        dto.setId(permission.getId());
        dto.setCode(permission.getCode());
        dto.setName(permission.getName());
        dto.setType(permission.getType());
        dto.setCreatedAt(permission.getCreatedAt());
        dto.setUpdatedAt(permission.getUpdatedAt());
        
        return dto;
    }
}
