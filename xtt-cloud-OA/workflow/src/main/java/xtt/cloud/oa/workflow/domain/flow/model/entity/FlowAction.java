package xtt.cloud.oa.workflow.domain.flow.model.entity;

import java.time.LocalDateTime;

/**
 * 发送动作实体
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
public class FlowAction {
    private Long id;
    private String actionCode;      // 动作编码（UNIT_HANDLE, REVIEW, EXTERNAL, RETURN）
    private String actionName;       // 动作名称，提供给前端显示名称。
    private Integer actionType;      // 动作类型
    private String actionTypeName;   // 动作类型名称
    private String description;      // 描述
    private String icon;            // 图标
    private Integer enabled;         // 是否启用（0:停用,1:启用）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 动作类型常量
    public static final int TYPE_APPROVE = 1;
    public static final String TYPE_APPROVE_NAME = "同意";
    public static final int TYPE_UNIT_HANDLE = 2;
    public static final String TYPE_UNIT_HANDLE_NAME = "拒绝";
    public static final int TYPE_RETURN_LAST = 3;         // 返回
    public static final String TYPE_RETURN_LAST_NAME = "退回上一个节点";
    public static final int TYPE_REVIEW = 4;          // 核稿
    public static final String TYPE_REVIEW_NAME = "退回初始节点";

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getActionCode() { return actionCode; }
    public void setActionCode(String actionCode) { this.actionCode = actionCode; }
    
    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }
    
    public Integer getActionType() { return actionType; }
    public void setActionType(Integer actionType) { this.actionType = actionType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    
    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer enabled) { this.enabled = enabled; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getActionTypeName() {
        return actionTypeName;
    }

    public void setActionTypeName(String actionTypeName) {
        this.actionTypeName = actionTypeName;
    }
}






