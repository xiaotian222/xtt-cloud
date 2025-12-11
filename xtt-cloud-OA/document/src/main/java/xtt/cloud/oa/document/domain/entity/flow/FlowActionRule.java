package xtt.cloud.oa.document.domain.entity.flow;

import java.time.LocalDateTime;

/**
 * 动作规则实体
 * 定义在什么条件下可以使用某个发送动作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
public class FlowActionRule {
    private Long id;
    private Long actionId;          // 动作ID
    private Integer documentStatus; // 文件状态(0:草稿,1:审核中,2:已发布,3:已归档)
    private String userRole;        // 用户角色（支持多个，逗号分隔，*表示所有角色）
    private Long deptId;           // 部门ID（可选，限制特定部门）
    private Integer priority;      // 优先级（数字越大优先级越高）
    private Integer enabled;        // 是否启用（0:停用,1:启用）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getActionId() { return actionId; }
    public void setActionId(Long actionId) { this.actionId = actionId; }
    
    public Integer getDocumentStatus() { return documentStatus; }
    public void setDocumentStatus(Integer documentStatus) { this.documentStatus = documentStatus; }
    
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    
    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer enabled) { this.enabled = enabled; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}






