package xtt.cloud.oa.workflow.interfaces.rest.flow.dto;

/**
 * 回退流程请求
 * 
 * @author xtt
 */
public class RollbackRequest {
    
    private Long flowInstanceId;
    private Long currentNodeInstanceId;
    private Long targetNodeId;
    private Long approverId;
    private String reason;
    
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

