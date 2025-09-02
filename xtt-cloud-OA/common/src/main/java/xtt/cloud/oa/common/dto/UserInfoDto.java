package xtt.cloud.oa.common.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户信息 DTO - 对外服务使用
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Data
public class UserInfoDto {
    
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 用户角色信息
    private Set<RoleInfoDto> roles;
    
    // 用户权限信息
    private Set<String> permissions;
    
    // 用户部门信息
    private Set<DeptInfoDto> departments;
}
