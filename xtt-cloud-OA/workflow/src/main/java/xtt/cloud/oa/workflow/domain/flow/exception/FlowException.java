package xtt.cloud.oa.workflow.domain.flow.exception;

/**
 * 流程领域异常基类
 * 
 * @author xtt
 */
public class FlowException extends RuntimeException {
    
    public FlowException(String message) {
        super(message);
    }
    
    public FlowException(String message, Throwable cause) {
        super(message, cause);
    }
}

