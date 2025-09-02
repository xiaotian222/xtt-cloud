package xtt.cloud.oa.common.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 权限信息 DTO - 对外服务使用
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Data
public class PermissionInfoDto {
    
    private Long id;
    private String code;
    private String name;
    private String type;
    private LocalDateTime createdAt;
}
