package xtt.cloud.oa.document.domain.entity.flow.history;

import java.time.LocalDateTime;

/**
 * 流程实例历史记录实体
 * 记录流程实例的完整历史信息
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
public class FlowInstanceHistory {
    
    private Long id;
    private Long flowInstanceId;        // 流程实例ID
    private Long documentId;             // 文档ID
    private Long flowDefId;              // 流程定义ID
    private String flowDefName;          // 流程定义名称
    private Integer flowType;            // 流程类型（1:发文,2:收文）
    private Integer flowMode;            // 流程模式（1:固定流,2:自由流,3:混合流）
    private Integer status;              // 流程状态（0:进行中,1:已完成,2:已终止）
    private Long initiatorId;            // 发起人ID
    private String initiatorName;        // 发起人姓名
    private Long initiatorDeptId;         // 发起人部门ID
    private String initiatorDeptName;    // 发起人部门名称
    private LocalDateTime startTime;     // 开始时间
    private LocalDateTime endTime;       // 结束时间
    private Long duration;               // 持续时间（秒）
    private Integer totalNodes;          // 总节点数
    private Integer completedNodes;      // 已完成节点数
    private String terminationReason;    // 终止原因
    private LocalDateTime createdAt;     // 记录创建时间
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getFlowInstanceId() { return flowInstanceId; }
    public void setFlowInstanceId(Long flowInstanceId) { this.flowInstanceId = flowInstanceId; }
    
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    
    public Long getFlowDefId() { return flowDefId; }
    public void setFlowDefId(Long flowDefId) { this.flowDefId = flowDefId; }
    
    public String getFlowDefName() { return flowDefName; }
    public void setFlowDefName(String flowDefName) { this.flowDefName = flowDefName; }
    
    public Integer getFlowType() { return flowType; }
    public void setFlowType(Integer flowType) { this.flowType = flowType; }
    
    public Integer getFlowMode() { return flowMode; }
    public void setFlowMode(Integer flowMode) { this.flowMode = flowMode; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public Long getInitiatorId() { return initiatorId; }
    public void setInitiatorId(Long initiatorId) { this.initiatorId = initiatorId; }
    
    public String getInitiatorName() { return initiatorName; }
    public void setInitiatorName(String initiatorName) { this.initiatorName = initiatorName; }
    
    public Long getInitiatorDeptId() { return initiatorDeptId; }
    public void setInitiatorDeptId(Long initiatorDeptId) { this.initiatorDeptId = initiatorDeptId; }
    
    public String getInitiatorDeptName() { return initiatorDeptName; }
    public void setInitiatorDeptName(String initiatorDeptName) { this.initiatorDeptName = initiatorDeptName; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }
    
    public Integer getTotalNodes() { return totalNodes; }
    public void setTotalNodes(Integer totalNodes) { this.totalNodes = totalNodes; }
    
    public Integer getCompletedNodes() { return completedNodes; }
    public void setCompletedNodes(Integer completedNodes) { this.completedNodes = completedNodes; }
    
    public String getTerminationReason() { return terminationReason; }
    public void setTerminationReason(String terminationReason) { this.terminationReason = terminationReason; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

