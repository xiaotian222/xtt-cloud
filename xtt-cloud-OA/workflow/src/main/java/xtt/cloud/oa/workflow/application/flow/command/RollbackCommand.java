package xtt.cloud.oa.workflow.application.flow.command;

/**
 * 回退流程命令
 * 
 * @author xtt
 */
public class RollbackCommand {
    
    private Long flowInstanceId;
    private Long currentNodeInstanceId;
    private Long targetNodeId;
    private Long approverId;
    private String reason;
    
    public RollbackCommand() {
    }
    
    public RollbackCommand(Long flowInstanceId, Long currentNodeInstanceId, 
                          Long targetNodeId, Long approverId, String reason) {
        this.flowInstanceId = flowInstanceId;
        this.currentNodeInstanceId = currentNodeInstanceId;
        this.targetNodeId = targetNodeId;
        this.approverId = approverId;
        this.reason = reason;
    }
    
    public Long getFlowInstanceId() {
        return flowInstanceId;
    }
    
    public void setFlowInstanceId(Long flowInstanceId) {
        this.flowInstanceId = flowInstanceId;
    }
    
    public Long getCurrentNodeInstanceId() {
        return currentNodeInstanceId;
    }
    
    public void setCurrentNodeInstanceId(Long currentNodeInstanceId) {
        this.currentNodeInstanceId = currentNodeInstanceId;
    }
    
    public Long getTargetNodeId() {
        return targetNodeId;
    }
    
    public void setTargetNodeId(Long targetNodeId) {
        this.targetNodeId = targetNodeId;
    }
    
    public Long getApproverId() {
        return approverId;
    }
    
    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}

