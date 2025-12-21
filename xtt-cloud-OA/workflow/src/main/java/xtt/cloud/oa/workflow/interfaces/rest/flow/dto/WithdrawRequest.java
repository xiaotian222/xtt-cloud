package xtt.cloud.oa.workflow.interfaces.rest.flow.dto;

/**
 * 撤回流程请求
 * 
 * @author xtt
 */
public class WithdrawRequest {
    
    private Long flowInstanceId;
    private Long initiatorId;
    private String reason;
    
    public Long getFlowInstanceId() {
        return flowInstanceId;
    }
    
    public void setFlowInstanceId(Long flowInstanceId) {
        this.flowInstanceId = flowInstanceId;
    }
    
    public Long getInitiatorId() {
        return initiatorId;
    }
    
    public void setInitiatorId(Long initiatorId) {
        this.initiatorId = initiatorId;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}

