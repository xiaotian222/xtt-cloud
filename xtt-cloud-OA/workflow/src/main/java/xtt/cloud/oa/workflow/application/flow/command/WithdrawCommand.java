package xtt.cloud.oa.workflow.application.flow.command;

/**
 * 撤回流程命令
 * 
 * @author xtt
 */
public class WithdrawCommand {
    
    private Long flowInstanceId;
    private Long initiatorId;
    private String reason;
    
    public WithdrawCommand() {
    }
    
    public WithdrawCommand(Long flowInstanceId, Long initiatorId, String reason) {
        this.flowInstanceId = flowInstanceId;
        this.initiatorId = initiatorId;
        this.reason = reason;
    }
    
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

