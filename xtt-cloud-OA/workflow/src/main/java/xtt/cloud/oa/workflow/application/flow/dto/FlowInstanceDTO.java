package xtt.cloud.oa.workflow.application.flow.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 流程实例DTO
 * 
 * @author xtt
 */
public class FlowInstanceDTO {
    
    private Long id;
    private Long documentId;
    private Long flowDefId;
    private Integer flowType;
    private String flowTypeDesc;
    private Integer status;
    private String statusDesc;
    private Integer flowMode;
    private String flowModeDesc;
    private Long currentNodeId;
    private Long parentFlowInstanceId;
    private Map<String, Object> processVariables;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FlowNodeInstanceDTO> nodeInstances;
    
    public FlowInstanceDTO() {
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public Integer getFlowType() {
        return flowType;
    }
    
    public void setFlowType(Integer flowType) {
        this.flowType = flowType;
    }
    
    public String getFlowTypeDesc() {
        return flowTypeDesc;
    }
    
    public void setFlowTypeDesc(String flowTypeDesc) {
        this.flowTypeDesc = flowTypeDesc;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getStatusDesc() {
        return statusDesc;
    }
    
    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }
    
    public Integer getFlowMode() {
        return flowMode;
    }
    
    public void setFlowMode(Integer flowMode) {
        this.flowMode = flowMode;
    }
    
    public String getFlowModeDesc() {
        return flowModeDesc;
    }
    
    public void setFlowModeDesc(String flowModeDesc) {
        this.flowModeDesc = flowModeDesc;
    }
    
    public Long getCurrentNodeId() {
        return currentNodeId;
    }
    
    public void setCurrentNodeId(Long currentNodeId) {
        this.currentNodeId = currentNodeId;
    }
    
    public Long getParentFlowInstanceId() {
        return parentFlowInstanceId;
    }
    
    public void setParentFlowInstanceId(Long parentFlowInstanceId) {
        this.parentFlowInstanceId = parentFlowInstanceId;
    }
    
    public Map<String, Object> getProcessVariables() {
        return processVariables;
    }
    
    public void setProcessVariables(Map<String, Object> processVariables) {
        this.processVariables = processVariables;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<FlowNodeInstanceDTO> getNodeInstances() {
        return nodeInstances;
    }
    
    public void setNodeInstances(List<FlowNodeInstanceDTO> nodeInstances) {
        this.nodeInstances = nodeInstances;
    }
}

