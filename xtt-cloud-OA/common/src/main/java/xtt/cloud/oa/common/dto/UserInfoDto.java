package xtt.cloud.oa.common.dto;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户信息 DTO - 对外服务使用
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Set<RoleInfoDto> getRoles() { return roles; }
    public void setRoles(Set<RoleInfoDto> roles) { this.roles = roles; }

    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> permissions) { this.permissions = permissions; }

    public Set<DeptInfoDto> getDepartments() { return departments; }
    public void setDepartments(Set<DeptInfoDto> departments) { this.departments = departments; }
}
