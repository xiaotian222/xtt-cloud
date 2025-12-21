package xtt.cloud.oa.workflow.infrastructure.persistence.pojo;

import java.time.LocalDateTime;

/**
 * 审批人选择范围实体
 * 定义每个发送动作对应的审批人选择范围
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
public class ApproverScope {
    private Long id;
    private Long actionId;          // 动作ID
    private Integer scopeType;      // 范围类型(1:部门,2:人员,3:部门+人员)
    private String deptIds;         // 可选部门ID列表（JSON数组字符串）
    private String userIds;         // 可选人员ID列表（JSON数组字符串）
    private String roleCodes;       // 可选角色编码（逗号分隔）
    private Integer allowCustom;    // 是否允许自定义选择（0:不允许,1:允许）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 范围类型常量
    public static final int SCOPE_TYPE_DEPT = 1;      // 部门
    public static final int SCOPE_TYPE_USER = 2;      // 人员
    public static final int SCOPE_TYPE_DEPT_USER = 3; // 部门+人员
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getActionId() { return actionId; }
    public void setActionId(Long actionId) { this.actionId = actionId; }
    
    public Integer getScopeType() { return scopeType; }
    public void setScopeType(Integer scopeType) { this.scopeType = scopeType; }
    
    public String getDeptIds() { return deptIds; }
    public void setDeptIds(String deptIds) { this.deptIds = deptIds; }
    
    public String getUserIds() { return userIds; }
    public void setUserIds(String userIds) { this.userIds = userIds; }
    
    public String getRoleCodes() { return roleCodes; }
    public void setRoleCodes(String roleCodes) { this.roleCodes = roleCodes; }
    
    public Integer getAllowCustom() { return allowCustom; }
    public void setAllowCustom(Integer allowCustom) { this.allowCustom = allowCustom; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}






