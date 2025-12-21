package xtt.cloud.oa.workflow.application.flow.command;

import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowMode;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.ProcessVariables;

import java.util.Map;

/**
 * 启动流程命令
 * 
 * @author xtt
 */
public class StartFlowCommand {
    
    private Long documentId;
    private Long flowDefId;
    private FlowType flowType;
    private FlowMode flowMode;
    private Long initiatorId;
    private Map<String, Object> processVariables;
    
    public StartFlowCommand() {
    }
    
    public StartFlowCommand(Long documentId, Long flowDefId, FlowType flowType, 
                           FlowMode flowMode, Long initiatorId, Map<String, Object> processVariables) {
        this.documentId = documentId;
        this.flowDefId = flowDefId;
        this.flowType = flowType;
        this.flowMode = flowMode;
        this.initiatorId = initiatorId;
        this.processVariables = processVariables;
    }
    
    public Long getDocumentId() {
        return documentId;
    }
    
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
    
    public Long getFlowDefId() {
        return flowDefId;
    }
    
    public void setFlowDefId(Long flowDefId) {
        this.flowDefId = flowDefId;
    }
    
    public FlowType getFlowType() {
        return flowType;
    }
    
    public void setFlowType(FlowType flowType) {
        this.flowType = flowType;
    }
    
    public FlowMode getFlowMode() {
        return flowMode;
    }
    
    public void setFlowMode(FlowMode flowMode) {
        this.flowMode = flowMode;
    }
    
    public Long getInitiatorId() {
        return initiatorId;
    }
    
    public void setInitiatorId(Long initiatorId) {
        this.initiatorId = initiatorId;
    }
    
    public Map<String, Object> getProcessVariables() {
        return processVariables;
    }
    
    public void setProcessVariables(Map<String, Object> processVariables) {
        this.processVariables = processVariables;
    }
    
    /**
     * 转换为 ProcessVariables 值对象
     */
    public ProcessVariables toProcessVariables() {
        if (processVariables == null || processVariables.isEmpty()) {
            return new ProcessVariables();
        }
        return new ProcessVariables(processVariables);
    }
}

