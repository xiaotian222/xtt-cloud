package xtt.cloud.oa.workflow.application.flow.command;

import java.util.List;
import java.util.Map;

/**
 * 发送自由流命令
 * 
 * @author xtt
 */
public class SendFreeFlowCommand {
    
    /**
     * 流程实例ID
     */
    private Long flowInstanceId;
    
    /**
     * 节点实例ID
     */
    private Long nodeInstanceId;
    
    /**
     * 动作ID
     */
    private Long actionId;
    
    /**
     * 选择的审批人ID列表
     */
    private List<Long> approverIds;
    
    /**
     * 选择的部门ID列表（可选）
     */
    private List<Long> deptIds;
    
    /**
     * 操作人ID
     */
    private Long operatorId;
    
    /**
     * 备注
     */
    private String comments;
    
    /**
     * 流程变量（可选）
     */
    private Map<String, Object> processVariables;
    
    // Getters and Setters
    public Long getFlowInstanceId() {
        return flowInstanceId;
    }
    
    public void setFlowInstanceId(Long flowInstanceId) {
        this.flowInstanceId = flowInstanceId;
    }
    
    public Long getNodeInstanceId() {
        return nodeInstanceId;
    }
    
    public void setNodeInstanceId(Long nodeInstanceId) {
        this.nodeInstanceId = nodeInstanceId;
    }
    
    public Long getActionId() {
        return actionId;
    }
    
    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }
    
    public List<Long> getApproverIds() {
        return approverIds;
    }
    
    public void setApproverIds(List<Long> approverIds) {
        this.approverIds = approverIds;
    }
    
    public List<Long> getDeptIds() {
        return deptIds;
    }
    
    public void setDeptIds(List<Long> deptIds) {
        this.deptIds = deptIds;
    }
    
    public Long getOperatorId() {
        return operatorId;
    }
    
    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public Map<String, Object> getProcessVariables() {
        return processVariables;
    }
    
    public void setProcessVariables(Map<String, Object> processVariables) {
        this.processVariables = processVariables;
    }
}

