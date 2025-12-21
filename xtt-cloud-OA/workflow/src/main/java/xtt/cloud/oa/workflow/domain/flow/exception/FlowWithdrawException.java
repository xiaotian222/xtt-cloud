package xtt.cloud.oa.workflow.domain.flow.exception;

/**
 * 流程撤回异常
 * 
 * @author xtt
 */
public class FlowWithdrawException extends FlowException {
    
    public FlowWithdrawException(String message) {
        super(message);
    }
    
    public FlowWithdrawException(Long flowInstanceId, String message) {
        super(String.format("流程撤回失败，流程实例ID: %d, 原因: %s", flowInstanceId, message));
    }
}

