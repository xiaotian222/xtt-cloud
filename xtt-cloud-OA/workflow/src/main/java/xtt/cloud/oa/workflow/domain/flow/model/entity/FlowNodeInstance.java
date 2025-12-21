package xtt.cloud.oa.workflow.domain.flow.model.entity;

import xtt.cloud.oa.workflow.domain.flow.model.valueobject.NodeStatus;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 节点实例实体
 * 
 * 实体特点：
 * 1. 有唯一标识（ID）
 * 2. 可变（Mutable）
 * 3. 通过ID相等性比较
 * 4. 属于 FlowInstance 聚合
 * 
 * @author xtt
 */
public class FlowNodeInstance {
    
    // 状态常量（兼容旧代码，推荐使用NodeStatus值对象）
    public static final Integer STATUS_PENDING = 0;
    public static final Integer STATUS_PROCESSING = 1;
    public static final Integer STATUS_COMPLETED = 2;
    public static final Integer STATUS_REJECTED = 3;
    public static final Integer STATUS_SKIPPED = 4;
    
    private Long id;
    private Long flowInstanceId;
    private Long nodeId;
    private Approver approver;
    private NodeStatus status;
    private String comments;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 创建节点实例
     */
    public static FlowNodeInstance create(
            Long flowInstanceId,
            Long nodeId,
            Approver approver) {
        
        FlowNodeInstance instance = new FlowNodeInstance();
        instance.flowInstanceId = flowInstanceId;
        instance.nodeId = nodeId;
        instance.approver = approver;
        instance.status = NodeStatus.PENDING;
        instance.createdAt = LocalDateTime.now();
        instance.updatedAt = LocalDateTime.now();
        
        return instance;
    }
    
    /**
     * 开始处理节点
     */
    public void startProcessing() {
        if (!status.canHandle()) {
            throw new IllegalStateException("Node instance cannot be started. Current status: " + status);
        }
        this.status = NodeStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 完成节点
     */
    public void complete(String comments) {
        if (!status.canHandle()) {
            throw new IllegalStateException("Node instance cannot be completed. Current status: " + status);
        }
        this.status = NodeStatus.COMPLETED;
        this.comments = comments;
        this.handledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 拒绝节点
     */
    public void reject(String comments) {
        if (!status.canHandle()) {
            throw new IllegalStateException("Node instance cannot be rejected. Current status: " + status);
        }
        this.status = NodeStatus.REJECTED;
        this.comments = comments;
        this.handledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 跳过节点
     */
    public void skip(String reason) {
        if (!status.canHandle()) {
            throw new IllegalStateException("Node instance cannot be skipped. Current status: " + status);
        }
        this.status = NodeStatus.SKIPPED;
        this.comments = reason;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 判断节点是否已完成
     */
    public boolean isFinished() {
        return status.isFinished();
    }
    
    /**
     * 判断节点是否被拒绝
     */
    public boolean isRejected() {
        return status.isRejected();
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public Long getFlowInstanceId() {
        return flowInstanceId;
    }
    
    public Long getNodeId() {
        return nodeId;
    }
    
    public Approver getApprover() {
        return approver;
    }
    
    public NodeStatus getStatus() {
        return status;
    }
    
    public String getComments() {
        return comments;
    }
    
    public LocalDateTime getHandledAt() {
        return handledAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    // Setters (仅用于持久化层重建对象)
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setFlowInstanceId(Long flowInstanceId) {
        this.flowInstanceId = flowInstanceId;
    }
    
    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }
    
    public void setApprover(Approver approver) {
        this.approver = approver;
    }
    
    public void setStatus(NodeStatus status) {
        this.status = status;
    }
    
    /**
     * 设置状态（通过整数值，兼容旧代码）
     */
    public void setStatus(Integer statusValue) {
        this.status = NodeStatus.fromValue(statusValue);
    }
    
    /**
     * 获取状态值（整数值，兼容旧代码）
     */
    public Integer getStatusValue() {
        return status != null ? status.getValue() : null;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public void setHandledAt(LocalDateTime handledAt) {
        this.handledAt = handledAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowNodeInstance that = (FlowNodeInstance) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "FlowNodeInstance{" +
                "id=" + id +
                ", flowInstanceId=" + flowInstanceId +
                ", nodeId=" + nodeId +
                ", status=" + status +
                '}';
    }
}

