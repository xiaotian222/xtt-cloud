package xtt.cloud.oa.workflow.interfaces.rest.flow.dto;

/**
 * 审批通过请求
 * 
 * @author xtt
 */
public class ApproveRequest {
    
    private Long flowInstanceId;
    private Long nodeInstanceId;
    private Long approverId;
    private String comments;
    private Boolean forward;
    
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
}

