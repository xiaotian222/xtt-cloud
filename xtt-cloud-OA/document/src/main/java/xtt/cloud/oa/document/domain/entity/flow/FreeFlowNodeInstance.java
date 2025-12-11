package xtt.cloud.oa.document.domain.entity.flow;

import java.time.LocalDateTime;

/**
 * 自由流节点实例扩展实体
 * 记录自由流节点的额外信息（发送动作、选择的审批人等）
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
public class FreeFlowNodeInstance {
    private Long id;
    private Long nodeInstanceId;    // 节点实例ID
    private Long actionId;          // 发送动作ID
    private String actionName;      // 动作名称
    private String selectedDeptIds; // 选择的部门ID列表（JSON数组字符串）
    private String selectedUserIds;  // 选择的人员ID列表（JSON数组字符串）
    private String customComment;   // 自定义备注
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getNodeInstanceId() { return nodeInstanceId; }
    public void setNodeInstanceId(Long nodeInstanceId) { this.nodeInstanceId = nodeInstanceId; }
    
    public Long getActionId() { return actionId; }
    public void setActionId(Long actionId) { this.actionId = actionId; }
    
    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }
    
    public String getSelectedDeptIds() { return selectedDeptIds; }
    public void setSelectedDeptIds(String selectedDeptIds) { this.selectedDeptIds = selectedDeptIds; }
    
    public String getSelectedUserIds() { return selectedUserIds; }
    public void setSelectedUserIds(String selectedUserIds) { this.selectedUserIds = selectedUserIds; }
    
    public String getCustomComment() { return customComment; }
    public void setCustomComment(String customComment) { this.customComment = customComment; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}






