package xtt.cloud.oa.workflow.application.flow.command;

/**
 * 审批通过命令
 * 
 * @author xtt
 */
public class ApproveCommand {
    
    private Long flowInstanceId;
    private Long nodeInstanceId;
    private Long approverId;
    private String comments;
    private Boolean forward; // 是否转办
    
    public ApproveCommand() {
    }
    
    public ApproveCommand(Long flowInstanceId, Long nodeInstanceId, 
                        Long approverId, String comments) {
        this.flowInstanceId = flowInstanceId;
        this.nodeInstanceId = nodeInstanceId;
        this.approverId = approverId;
        this.comments = comments;
        this.forward = false;
    }
    
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
    
    public Long getApproverId() {
        return approverId;
    }
    
    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public Boolean getForward() {
        return forward;
    }
    
    public void setForward(Boolean forward) {
        this.forward = forward;
    }
    
    public boolean isForward() {
        return Boolean.TRUE.equals(forward);
    }
}

