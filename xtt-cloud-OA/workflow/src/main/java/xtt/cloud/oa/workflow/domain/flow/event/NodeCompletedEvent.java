package xtt.cloud.oa.workflow.domain.flow.event;

import java.time.LocalDateTime;

/**
 * 节点完成领域事件
 * 
 * @author xtt
 */
public class NodeCompletedEvent implements DomainEvent {
    
    private final Long nodeInstanceId;
    private final Long flowInstanceId;
    private final Long nodeId;
    private final Long approverId;
    private final LocalDateTime occurredOn;
    
    public NodeCompletedEvent(Long nodeInstanceId, Long flowInstanceId, 
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
    
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}

