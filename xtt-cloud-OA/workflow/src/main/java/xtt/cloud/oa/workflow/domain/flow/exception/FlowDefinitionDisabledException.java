package xtt.cloud.oa.workflow.domain.flow.exception;

/**
 * 流程定义已停用异常
 * 
 * @author xtt
 */
public class FlowDefinitionDisabledException extends FlowException {
    
    public FlowDefinitionDisabledException() {
        super("流程定义已停用，无法启动流程");
    }
    
    public FlowDefinitionDisabledException(Long flowDefId) {
        super(String.format("流程定义已停用，无法启动流程，流程定义ID: %d", flowDefId));
    }
}

