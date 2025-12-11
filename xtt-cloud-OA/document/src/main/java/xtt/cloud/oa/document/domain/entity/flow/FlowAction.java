package xtt.cloud.oa.document.domain.entity.flow;

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
    private String actionName;       // 动作名称
    private Integer actionType;      // 动作类型
    private String description;      // 描述
    private String icon;            // 图标
    private Integer enabled;         // 是否启用（0:停用,1:启用）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 动作类型常量
    public static final int TYPE_UNIT_HANDLE = 1;    // 单位内办理
    public static final int TYPE_REVIEW = 2;          // 核稿
    public static final int TYPE_EXTERNAL = 3;        // 转外单位办理
    public static final int TYPE_RETURN = 4;         // 返回
    
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
}






