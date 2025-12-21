package xtt.cloud.oa.workflow.infrastructure.messaging.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import xtt.cloud.oa.workflow.domain.flow.event.FlowStartedEvent;

/**
 * 流程启动事件处理器
 * 
 * @author xtt
 */
@Component
public class FlowStartedEventHandler {
    
    private static final Logger log = LoggerFactory.getLogger(FlowStartedEventHandler.class);
    
    @EventListener
    @Async
    public void handle(FlowStartedEvent event) {
        log.info("处理流程启动事件，流程实例ID: {}, 文档ID: {}, 流程定义ID: {}", 
                event.getFlowInstanceId(), event.getDocumentId(), event.getFlowDefId());
        
        // TODO: 实现事件处理逻辑
        // 1. 发送通知
        // 2. 记录审计日志
        // 3. 更新统计信息
        // 4. 触发其他业务流程
    }
}

