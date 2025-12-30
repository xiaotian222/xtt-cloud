package xtt.cloud.oa.workflow.domain.flow.event;

import java.time.LocalDateTime;

/**
 * 节点实例创建领域事件
 * 
 * 当创建节点实例并分配审批人时发布此事件
 * 用于触发待办任务创建和消息发送
 * 
 * @author xtt
 */
public class NodeInstanceCreatedEvent implements DomainEvent {
    
    private final Long nodeInstanceId;
    private final Long flowInstanceId;
    private final Long nodeId;
    private final Long approverId;
    private final LocalDateTime occurredOn;
    
    public NodeInstanceCreatedEvent(Long nodeInstanceId, Long flowInstanceId, 
                                   Long nodeId, Long approverId) {
        this.nodeInstanceId = nodeInstanceId;
        this.flowInstanceId = flowInstanceId;
        this.nodeId = nodeId;
        this.approverId = approverId;
        this.occurredOn = LocalDateTime.now();
    }
    
    public Long getNodeInstanceId() {
        return nodeInstanceId;
    }
    
    public Long getFlowInstanceId() {
        return flowInstanceId;
    }
    
    public Long getNodeId() {
        return nodeId;
    }
    
    public Long getApproverId() {
        return approverId;
    }
    
    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}

