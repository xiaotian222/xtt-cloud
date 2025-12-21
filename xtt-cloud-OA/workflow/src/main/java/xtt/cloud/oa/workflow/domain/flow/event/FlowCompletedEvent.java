package xtt.cloud.oa.workflow.domain.flow.event;

import java.time.LocalDateTime;

/**
 * 流程完成领域事件
 * 
 * @author xtt
 */
public class FlowCompletedEvent implements DomainEvent {
    
    private final Long flowInstanceId;
    private final Long documentId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final LocalDateTime occurredOn;
    
    public FlowCompletedEvent(Long flowInstanceId, Long documentId, 
                             LocalDateTime startTime, LocalDateTime endTime) {
        this.flowInstanceId = flowInstanceId;
        this.documentId = documentId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.occurredOn = LocalDateTime.now();
    }
    
    public Long getFlowInstanceId() {
        return flowInstanceId;
    }
    
    public Long getDocumentId() {
        return documentId;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}

