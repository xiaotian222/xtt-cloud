package xtt.cloud.oa.workflow.interfaces.rest.flow.dto;

/**
 * 自由流动作 DTO
 * 
 * @author xtt
 */
public class FreeFlowActionDTO {
    
    private Long id;
    private String actionCode;
    private String actionName;
    private Integer actionType;
    private String description;
    private String icon;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getActionCode() {
        return actionCode;
    }
    
    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }
    
    public String getActionName() {
        return actionName;
    }
    
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
    
    public Integer getActionType() {
        return actionType;
    }
    
    public void setActionType(Integer actionType) {
        this.actionType = actionType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
}

