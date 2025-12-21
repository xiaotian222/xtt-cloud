package xtt.cloud.oa.workflow.domain.flow.event;

import java.time.LocalDateTime;

/**
 * 流程终止领域事件
 * 
 * @author xtt
 */
public class FlowTerminatedEvent implements DomainEvent {
    
    private final Long flowInstanceId;
    private final Long documentId;
    private final Integer status;
    private final LocalDateTime occurredOn;
    
    public FlowTerminatedEvent(Long flowInstanceId, Long documentId, Integer status) {
        this.flowInstanceId = flowInstanceId;
        this.documentId = documentId;
        this.status = status;
        this.occurredOn = LocalDateTime.now();
    }
    
    public Long getFlowInstanceId() {
        return flowInstanceId;
    }
    
    public Long getDocumentId() {
        return documentId;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}

