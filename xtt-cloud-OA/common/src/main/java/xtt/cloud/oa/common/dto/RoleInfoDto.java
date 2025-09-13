package xtt.cloud.oa.common.dto;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 角色信息 DTO - 对外服务使用
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
public class RoleInfoDto {
    
    private Long id;
    private String code;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 角色权限信息
    private Set<PermissionInfoDto> permissions;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Set<PermissionInfoDto> getPermissions() { return permissions; }
    public void setPermissions(Set<PermissionInfoDto> permissions) { this.permissions = permissions; }
}
