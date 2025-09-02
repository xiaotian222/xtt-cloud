package xtt.cloud.oa.common.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 部门信息 DTO - 对外服务使用
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Data
public class DeptInfoDto {
    
    private Long id;
    private Long parentId;
    private String name;
    private Integer sortNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
