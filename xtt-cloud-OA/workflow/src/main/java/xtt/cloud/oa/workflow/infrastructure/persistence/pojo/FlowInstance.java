package xtt.cloud.oa.workflow.infrastructure.persistence.pojo;

import java.time.LocalDateTime;

/**
 * 流程实例实体
 */
public class FlowInstance {
    private Long id;
    private Long documentId;
    private Long flowDefId;
    private Integer flowType; // 1:发文,2:收文
    private Integer status; // 0:进行中,1:已完成,2:已终止
    private Long currentNodeId;
    private Long parentFlowInstanceId; // 父流程实例ID（用于子流程）
    private Integer flowMode; // 流程模式：1-固定流,2-自由流,3-混合流
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 流程类型常量 这个应该是自定义的，非流程中的固定变量
    public static final int TYPE_ISSUANCE = 1;  // 发文
    public static final int TYPE_RECEIPT = 2;   // 收文
    
    // 流程模式常量
    public static final int FLOW_MODE_FIXED = 1;    // 固定流
    public static final int FLOW_MODE_FREE = 2;      // 自由流
    public static final int FLOW_MODE_MIXED = 3;     // 混合流（固定流中包含自由流节点）
    
    // 流程状态常量
    public static final int STATUS_PROCESSING = 0;  // 进行中
    public static final int STATUS_COMPLETED = 1;   // 已完成
    public static final int STATUS_TERMINATED = 2;  // 已终止
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    
    public Long getFlowDefId() { return flowDefId; }
    public void setFlowDefId(Long flowDefId) { this.flowDefId = flowDefId; }
    
    public Integer getFlowType() { return flowType; }
    public void setFlowType(Integer flowType) { this.flowType = flowType; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public Long getCurrentNodeId() { return currentNodeId; }
    public void setCurrentNodeId(Long currentNodeId) { this.currentNodeId = currentNodeId; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getParentFlowInstanceId() { return parentFlowInstanceId; }
    public void setParentFlowInstanceId(Long parentFlowInstanceId) { this.parentFlowInstanceId = parentFlowInstanceId; }
    
    public Integer getFlowMode() { return flowMode; }
    public void setFlowMode(Integer flowMode) { this.flowMode = flowMode; }
}