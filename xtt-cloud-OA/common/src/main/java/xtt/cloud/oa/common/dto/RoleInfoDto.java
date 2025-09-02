package xtt.cloud.oa.common.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 角色信息 DTO - 对外服务使用
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Data
public class RoleInfoDto {
    
    private Long id;
    private String code;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 角色权限信息
    private Set<PermissionInfoDto> permissions;
}
