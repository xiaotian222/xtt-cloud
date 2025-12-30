package xtt.cloud.oa.workflow.interfaces.rest.flow.dto;

/**
 * 开启自由流转请求
 * 
 * @author xtt
 */
public class StartFreeFlowRequest {
    
    private Long flowInstanceId;
    private Long nodeInstanceId;
    private Long operatorId;
    private String comments;
    
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
}

