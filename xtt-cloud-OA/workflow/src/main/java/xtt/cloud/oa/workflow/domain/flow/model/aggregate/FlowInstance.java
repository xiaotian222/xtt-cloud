package xtt.cloud.oa.workflow.domain.flow.model.aggregate;

import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowInstanceId;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowStatus;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowMode;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.ProcessVariables;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.event.FlowStartedEvent;
import xtt.cloud.oa.workflow.domain.flow.event.FlowCompletedEvent;
import xtt.cloud.oa.workflow.domain.flow.event.FlowTerminatedEvent;
import xtt.cloud.oa.workflow.domain.flow.event.NodeInstanceCreatedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 流程实例聚合根
 * 
 * 聚合根特点：
 * 1. 管理聚合内的所有实体和值对象
 * 2. 保证业务不变式（Business Invariants）
 * 3. 通过ID进行唯一标识
 * 4. 控制聚合内对象的访问
 * 
 * @author xtt
 */
public class FlowInstance {
    
    private FlowInstanceId id;
    private Long documentId;
    private Long flowDefId;
    private FlowType flowType;
    private FlowStatus status;
    private FlowMode flowMode;
    private Long currentNodeId;
    private Long parentFlowInstanceId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // 流程变量
    private ProcessVariables processVariables;

    // 聚合内的实体集合
    private List<FlowNodeInstance> nodeInstances;

    // 领域事件列表
    private List<Object> domainEvents;
    
    /**
     * 私有构造函数，通过工厂方法创建
     */
    private FlowInstance() {
        this.nodeInstances = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
        this.processVariables = new ProcessVariables();
    }
    
    /**
     * 用于持久化层重建对象的构造函数
     * 注意：此方法仅用于从数据库重建聚合根，不应在业务逻辑中使用
     */
    public static FlowInstance reconstruct() {
        return new FlowInstance();
    }
    
    /**
     * 创建新的流程实例（工厂方法）
     */
    public static FlowInstance create(
            Long documentId,
            Long flowDefId,
            FlowType flowType,
            FlowMode flowMode,
            ProcessVariables initialVariables) {
        
        FlowInstance instance = new FlowInstance();
        instance.documentId = documentId;
        instance.flowDefId = flowDefId;
        instance.flowType = flowType;
        instance.flowMode = flowMode;
        instance.status = FlowStatus.PROCESSING;
        instance.processVariables = initialVariables != null ? initialVariables : new ProcessVariables();
        instance.startTime = LocalDateTime.now();
        instance.createdAt = LocalDateTime.now();
        instance.updatedAt = LocalDateTime.now();
        
        // 发布领域事件
        instance.addDomainEvent(new FlowStartedEvent(
                instance.getId() != null ? instance.getId().getValue() : null,
                documentId,
                flowDefId));
        
        return instance;
    }
    
    /**
     * 启动流程
     */
    public void start() {
        if (!status.isProcessing()) {
            throw new IllegalStateException("Flow instance cannot be started. Current status: " + status);
        }
        this.startTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 完成流程
     */
    public void complete() {
        if (!status.canProceed()) {
            throw new IllegalStateException("Flow instance cannot be completed. Current status: " + status);
        }
        this.status = FlowStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new FlowCompletedEvent(
                id != null ? id.getValue() : null,
                documentId,
                startTime,
                endTime));
    }
    
    /**
     * 终止流程
     */
    public void terminate() {
        if (!status.canProceed()) {
            throw new IllegalStateException("Flow instance cannot be terminated. Current status: " + status);
        }
        this.status = FlowStatus.TERMINATED;
        this.endTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new FlowTerminatedEvent(
                id != null ? id.getValue() : null,
                documentId,
                status.getValue()));
    }
    
    /**
     * 暂停流程
     */
    public void suspend() {
        if (!status.canProceed()) {
            throw new IllegalStateException("Flow instance cannot be suspended. Current status: " + status);
        }
        this.status = FlowStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 恢复流程
     */
    public void resume() {
        if (!status.isSuspended()) {
            throw new IllegalStateException("Flow instance cannot be resumed. Current status: " + status);
        }
        this.status = FlowStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 取消流程
     */
    public void cancel() {
        if (!status.canProceed()) {
            throw new IllegalStateException("Flow instance cannot be cancelled. Current status: " + status);
        }
        this.status = FlowStatus.CANCELLED;
        this.endTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 移动到下一个节点
     */
    public void moveToNode(Long nodeId) {
        if (!status.canProceed()) {
            throw new IllegalStateException("Cannot move to next node. Flow status: " + status);
        }
        this.currentNodeId = nodeId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 设置流程变量
     */
    public void setProcessVariable(String key, Object value) {
        this.processVariables = processVariables.setVariable(key, value);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 获取流程变量
     */
    public Object getProcessVariable(String key) {
        return processVariables.getVariable(key);
    }
    
    /**
     * 添加节点实例
     */
    public void addNodeInstance(FlowNodeInstance nodeInstance) {
        if (nodeInstance == null) {
            throw new IllegalArgumentException("Node instance cannot be null");
        }
        // 业务规则：节点实例必须属于当前流程实例
        if (this.id != null && nodeInstance.getFlowInstanceId() != null) {
            if (!this.id.getValue().equals(nodeInstance.getFlowInstanceId())) {
                throw new IllegalArgumentException("Node instance does not belong to this flow instance");
            }
        }
        this.nodeInstances.add(nodeInstance);
        
        // 发布节点实例创建事件
        Long approverId = nodeInstance.getApprover() != null 
                ? nodeInstance.getApprover().getUserId() 
                : null;
        addDomainEvent(new NodeInstanceCreatedEvent(
                nodeInstance.getId(),
                this.id != null ? this.id.getValue() : null,
                nodeInstance.getNodeId(),
                approverId));
    }
    
    /**
     * 获取所有节点实例
     */
    public List<FlowNodeInstance> getNodeInstances() {
        return new ArrayList<>(nodeInstances);
    }
    
    /**
     * 添加领域事件
     */
    private void addDomainEvent(Object event) {
        this.domainEvents.add(event);
    }
    
    /**
     * 获取并清空领域事件
     */
    public List<Object> getAndClearDomainEvents() {
        List<Object> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }
    
    // Getters
    public FlowInstanceId getId() {
        return id;
    }
    
    public Long getDocumentId() {
        return documentId;
    }
    
    public Long getFlowDefId() {
        return flowDefId;
    }
    
    public FlowType getFlowType() {
        return flowType;
    }
    
    public FlowStatus getStatus() {
        return status;
    }
    
    public FlowMode getFlowMode() {
        return flowMode;
    }
    
    public Long getCurrentNodeId() {
        return currentNodeId;
    }
    
    public Long getParentFlowInstanceId() {
        return parentFlowInstanceId;
    }
    
    public ProcessVariables getProcessVariables() {
        return processVariables;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    // Setters (仅用于持久化层重建对象)
    public void setId(FlowInstanceId id) {
        this.id = id;
    }
    
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
    
    public void setFlowDefId(Long flowDefId) {
        this.flowDefId = flowDefId;
    }
    
    public void setFlowType(FlowType flowType) {
        this.flowType = flowType;
    }
    
    public void setStatus(FlowStatus status) {
        this.status = status;
    }
    
    public void setFlowMode(FlowMode flowMode) {
        this.flowMode = flowMode;
    }
    
    public void setCurrentNodeId(Long currentNodeId) {
        this.currentNodeId = currentNodeId;
    }
    
    public void setParentFlowInstanceId(Long parentFlowInstanceId) {
        this.parentFlowInstanceId = parentFlowInstanceId;
    }
    
    public void setProcessVariables(ProcessVariables processVariables) {
        this.processVariables = processVariables;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public void setNodeInstances(List<FlowNodeInstance> nodeInstances) {
        this.nodeInstances = nodeInstances != null ? new ArrayList<>(nodeInstances) : new ArrayList<>();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowInstance that = (FlowInstance) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

