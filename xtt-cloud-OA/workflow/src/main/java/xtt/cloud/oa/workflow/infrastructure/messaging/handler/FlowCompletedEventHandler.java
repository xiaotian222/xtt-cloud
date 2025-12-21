package xtt.cloud.oa.workflow.infrastructure.messaging.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import xtt.cloud.oa.workflow.domain.flow.event.FlowCompletedEvent;

/**
 * 流程完成事件处理器
 * 
 * @author xtt
 */
@Component
public class FlowCompletedEventHandler {
    
    private static final Logger log = LoggerFactory.getLogger(FlowCompletedEventHandler.class);
    
    @EventListener
    @Async
    public void handle(FlowCompletedEvent event) {
        log.info("处理流程完成事件，流程实例ID: {}, 文档ID: {}, 开始时间: {}, 结束时间: {}", 
                event.getFlowInstanceId(), event.getDocumentId(), 
                event.getStartTime(), event.getEndTime());
        
        // TODO: 实现事件处理逻辑
        // 1. 发送完成通知
        // 2. 更新文档状态
        // 3. 记录完成统计
        // 4. 触发后续业务流程
    }
}

