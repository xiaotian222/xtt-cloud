package xtt.cloud.oa.document.interfaces.rest.dto;

import java.util.List;

/**
 * 执行发送动作请求DTO
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
public class ExecuteActionRequest {
    private Long actionId;              // 动作ID
    private List<Long> selectedDeptIds;  // 选择的部门ID列表
    private List<Long> selectedUserIds;  // 选择的人员ID列表
    private String comment;             // 备注
    
    public Long getActionId() { return actionId; }
    public void setActionId(Long actionId) { this.actionId = actionId; }
    
    public List<Long> getSelectedDeptIds() { return selectedDeptIds; }
    public void setSelectedDeptIds(List<Long> selectedDeptIds) { this.selectedDeptIds = selectedDeptIds; }
    
    public List<Long> getSelectedUserIds() { return selectedUserIds; }
    public void setSelectedUserIds(List<Long> selectedUserIds) { this.selectedUserIds = selectedUserIds; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}

