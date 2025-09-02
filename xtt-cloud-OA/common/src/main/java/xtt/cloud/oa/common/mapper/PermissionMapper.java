package xtt.cloud.oa.common.mapper;

import org.springframework.stereotype.Component;
import xtt.cloud.oa.common.dto.PermissionInfoDto;

/**
 * 权限映射器 - 通用映射器
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Component
public class PermissionMapper {

    public PermissionInfoDto toPermissionInfoDto(Object permission) {
        // 具体实现由各个服务提供
        return null;
    }
}
