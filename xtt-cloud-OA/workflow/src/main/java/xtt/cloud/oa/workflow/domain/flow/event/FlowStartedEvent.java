package xtt.cloud.oa.workflow.domain.flow.event;

import java.time.LocalDateTime;

/**
 * 流程启动领域事件
 * 
 * @author xtt
 */
public class FlowStartedEvent implements DomainEvent {
    
    private final Long flowInstanceId;
    private final Long documentId;
    private final Long flowDefId;
    private final LocalDateTime occurredOn;
    
    public FlowStartedEvent(Long flowInstanceId, Long documentId, Long flowDefId) {
        this.flowInstanceId = flowInstanceId;
        this.documentId = documentId;
        this.flowDefId = flowDefId;
        this.occurredOn = LocalDateTime.now();
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
    
    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}

