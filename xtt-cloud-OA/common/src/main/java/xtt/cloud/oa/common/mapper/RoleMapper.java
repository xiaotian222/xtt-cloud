package xtt.cloud.oa.common.mapper;

import org.springframework.stereotype.Component;
import xtt.cloud.oa.common.dto.PermissionInfoDto;
import xtt.cloud.oa.common.dto.RoleInfoDto;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色映射器 - 通用映射器
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Component("commonRoleMapper")
public class RoleMapper {

    public RoleInfoDto toRoleInfoDto(Object role) {
        // 具体实现由各个服务提供
        return null;
    }

    public Set<PermissionInfoDto> toPermissionInfoDtos(Set<?> permissions) {
        if (permissions == null) {
            return null;
        }
        
        return permissions.stream()
                .map(this::toPermissionInfoDto)
                .collect(Collectors.toSet());
    }

    private PermissionInfoDto toPermissionInfoDto(Object permission) {
        // 具体实现由各个服务提供
        return null;
    }
}
