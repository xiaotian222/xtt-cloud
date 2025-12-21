package xtt.cloud.oa.workflow.domain.flow.exception;

/**
 * 节点完成异常
 * 
 * @author xtt
 */
public class NodeCompletionException extends FlowException {
    
    public NodeCompletionException(String message) {
        super(message);
    }
    
    public NodeCompletionException(Long nodeInstanceId, String message) {
        super(String.format("节点实例完成失败，节点实例ID: %d, 原因: %s", nodeInstanceId, message));
    }
}

