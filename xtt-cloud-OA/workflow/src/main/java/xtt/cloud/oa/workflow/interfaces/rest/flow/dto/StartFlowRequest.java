package xtt.cloud.oa.workflow.interfaces.rest.flow.dto;

import java.util.Map;

/**
 * 启动流程请求
 * 
 * @author xtt
 */
public class StartFlowRequest {
    
    private Long documentId;
    private Long flowDefId;
    private Integer flowType;
    private Integer flowMode;
    private Long initiatorId;
    private Map<String, Object> processVariables;
    
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
    
    public Integer getFlowType() {
        return flowType;
    }
    
    public void setFlowType(Integer flowType) {
        this.flowType = flowType;
    }
    
    public Integer getFlowMode() {
        return flowMode;
    }
    
    public void setFlowMode(Integer flowMode) {
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
}

