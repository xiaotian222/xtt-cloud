package xtt.cloud.oa.workflow.domain.flow.model.entity.history;

import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowMode;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowStatus;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 流程实例历史记录实体
 * 
 * 记录流程实例的完整历史信息
 * 
 * @author xtt
 */
public class FlowInstanceHistory {
    
    private Long id;
    private Long flowInstanceId;        // 流程实例ID
    private Long documentId;             // 文档ID
    private Long flowDefId;              // 流程定义ID
    private String flowDefName;          // 流程定义名称
    private FlowType flowType;           // 流程类型
    private FlowMode flowMode;           // 流程模式
    private FlowStatus status;           // 流程状态
    private Long initiatorId;            // 发起人ID
    private String initiatorName;        // 发起人姓名
    private Long initiatorDeptId;        // 发起人部门ID
    private String initiatorDeptName;    // 发起人部门名称
    private LocalDateTime startTime;     // 开始时间
    private LocalDateTime endTime;       // 结束时间
    private Long duration;               // 持续时间（秒）
    private Integer totalNodes;          // 总节点数
    private Integer completedNodes;      // 已完成节点数
    private String terminationReason;    // 终止原因
    private LocalDateTime createdAt;      // 记录创建时间
    
    /**
     * 创建流程实例历史（工厂方法）
     */
    public static FlowInstanceHistory create(
            Long flowInstanceId,
            Long documentId,
            Long flowDefId,
            String flowDefName,
            FlowType flowType,
            FlowMode flowMode,
            FlowStatus status,
            Long initiatorId,
            LocalDateTime startTime) {
        
        if (flowInstanceId == null || flowInstanceId <= 0) {
            throw new IllegalArgumentException("流程实例ID必须大于0");
        }
        
        FlowInstanceHistory history = new FlowInstanceHistory();
        history.flowInstanceId = flowInstanceId;
        history.documentId = documentId;
        history.flowDefId = flowDefId;
        history.flowDefName = flowDefName;
        history.flowType = flowType;
        history.flowMode = flowMode;
        history.status = status;
        history.initiatorId = initiatorId;
        history.startTime = startTime;
        history.createdAt = LocalDateTime.now();
        
        return history;
    }
    
    /**
     * 完成流程历史记录
     */
    public void complete(LocalDateTime endTime, Integer totalNodes, Integer completedNodes) {
        this.endTime = endTime;
        this.totalNodes = totalNodes;
        this.completedNodes = completedNodes;
        
        // 计算持续时间
        if (startTime != null && endTime != null) {
            this.duration = java.time.Duration.between(startTime, endTime).getSeconds();
        }
    }
    
    /**
     * 终止流程历史记录
     */
    public void terminate(LocalDateTime endTime, String reason) {
        this.endTime = endTime;
        this.terminationReason = reason;
        
        // 计算持续时间
        if (startTime != null && endTime != null) {
            this.duration = java.time.Duration.between(startTime, endTime).getSeconds();
        }
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public Long getFlowInstanceId() {
        return flowInstanceId;
    }
    
    public Long getDocumentId() {
        return documentId;
    }
    
    public Long getFlowDefId() {
        return flowDefId;
    }
    
    public String getFlowDefName() {
        return flowDefName;
    }
    
    public FlowType getFlowType() {
        return flowType;
    }
    
    public FlowMode getFlowMode() {
        return flowMode;
    }
    
    public FlowStatus getStatus() {
        return status;
    }
    
    public Long getInitiatorId() {
        return initiatorId;
    }
    
    public String getInitiatorName() {
        return initiatorName;
    }
    
    public Long getInitiatorDeptId() {
        return initiatorDeptId;
    }
    
    public String getInitiatorDeptName() {
        return initiatorDeptName;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public Long getDuration() {
        return duration;
    }
    
    public Integer getTotalNodes() {
        return totalNodes;
    }
    
    public Integer getCompletedNodes() {
        return completedNodes;
    }
    
    public String getTerminationReason() {
        return terminationReason;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    // Setters (仅用于持久化层重建对象)
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setFlowInstanceId(Long flowInstanceId) {
        this.flowInstanceId = flowInstanceId;
    }
    
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
    
    public void setFlowDefId(Long flowDefId) {
        this.flowDefId = flowDefId;
    }
    
    public void setFlowDefName(String flowDefName) {
        this.flowDefName = flowDefName;
    }
    
    public void setFlowType(FlowType flowType) {
        this.flowType = flowType;
    }
    
    public void setFlowMode(FlowMode flowMode) {
        this.flowMode = flowMode;
    }
    
    public void setStatus(FlowStatus status) {
        this.status = status;
    }
    
    public void setInitiatorId(Long initiatorId) {
        this.initiatorId = initiatorId;
    }
    
    public void setInitiatorName(String initiatorName) {
        this.initiatorName = initiatorName;
    }
    
    public void setInitiatorDeptId(Long initiatorDeptId) {
        this.initiatorDeptId = initiatorDeptId;
    }
    
    public void setInitiatorDeptName(String initiatorDeptName) {
        this.initiatorDeptName = initiatorDeptName;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public void setDuration(Long duration) {
        this.duration = duration;
    }
    
    public void setTotalNodes(Integer totalNodes) {
        this.totalNodes = totalNodes;
    }
    
    public void setCompletedNodes(Integer completedNodes) {
        this.completedNodes = completedNodes;
    }
    
    public void setTerminationReason(String terminationReason) {
        this.terminationReason = terminationReason;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowInstanceHistory that = (FlowInstanceHistory) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "FlowInstanceHistory{" +
                "id=" + id +
                ", flowInstanceId=" + flowInstanceId +
                ", flowDefName='" + flowDefName + '\'' +
                ", status=" + status +
                '}';
    }
}

