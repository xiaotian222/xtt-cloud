package xtt.cloud.oa.workflow.infrastructure.messaging.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 领域事件发布器
 * 
 * 负责发布领域事件到 Spring 事件总线
 * 
 * @author xtt
 */
@Component
public class DomainEventPublisher {
    
    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisher.class);
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public DomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    /**
     * 发布单个领域事件
     */
    public void publish(Object event) {
        if (event == null) {
            return;
        }
        
        try {
            log.debug("发布领域事件: {}", event.getClass().getSimpleName());
            applicationEventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("发布领域事件失败: {}", event.getClass().getSimpleName(), e);
        }
    }
    
    /**
     * 批量发布领域事件
     */
    public void publishAll(List<Object> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        
        for (Object event : events) {
            publish(event);
        }
    }
}

