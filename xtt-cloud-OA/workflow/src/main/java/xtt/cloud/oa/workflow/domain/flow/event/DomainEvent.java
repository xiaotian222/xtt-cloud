package xtt.cloud.oa.workflow.domain.flow.event;

import java.time.LocalDateTime;

/**
 * 领域事件接口
 * 
 * 所有领域事件都应该实现此接口
 * 
 * @author xtt
 */
public interface DomainEvent {
    
    /**
     * 获取事件发生时间
     */
    LocalDateTime getOccurredOn();
}

