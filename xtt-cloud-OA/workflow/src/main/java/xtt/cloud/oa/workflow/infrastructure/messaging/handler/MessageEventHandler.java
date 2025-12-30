package xtt.cloud.oa.workflow.infrastructure.messaging.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import xtt.cloud.oa.workflow.domain.flow.event.NodeCompletedEvent;
import xtt.cloud.oa.workflow.domain.flow.event.NodeInstanceCreatedEvent;
import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskAction;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowInstanceRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeInstanceRepository;
import xtt.cloud.rabbitmq.RabbitMessageSender;

import java.time.LocalDateTime;

/**
 * 消息事件处理器
 * 
 * 职责：
 * 1. 监听节点实例创建事件，发送待办消息到 RabbitMQ
 * 2. 监听节点完成事件，发送已办消息到 RabbitMQ
 * 
 * 注意：使用 @Async 异步处理，不阻塞主流程
 * 
 * @author xtt
 */
@Component
public class MessageEventHandler {
    
    private static final Logger log = LoggerFactory.getLogger(MessageEventHandler.class);
    
    private final RabbitMessageSender messageSender;
    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowNodeInstanceRepository flowNodeInstanceRepository;
    
    public MessageEventHandler(
            RabbitMessageSender messageSender,
            FlowInstanceRepository flowInstanceRepository,
            FlowNodeInstanceRepository flowNodeInstanceRepository) {
        this.messageSender = messageSender;
        this.flowInstanceRepository = flowInstanceRepository;
        this.flowNodeInstanceRepository = flowNodeInstanceRepository;
    }
    
    /**
     * 处理节点实例创建事件 - 发送待办消息
     * 
     * @param event 节点实例创建事件
     */
    @EventListener
    @Async
    public void handleNodeInstanceCreated(NodeInstanceCreatedEvent event) {
        log.info("发送待办消息，节点实例ID: {}, 审批人ID: {}", 
                event.getNodeInstanceId(), event.getApproverId());
        
        try {
            // 1. 加载流程实例和节点实例
            FlowInstance flowInstance = flowInstanceRepository.findById(event.getFlowInstanceId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "流程实例不存在: " + event.getFlowInstanceId()));
            
            FlowNodeInstance nodeInstance = flowNodeInstanceRepository.findById(event.getNodeInstanceId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "节点实例不存在: " + event.getNodeInstanceId()));
            
            // 2. 构建待办消息
            TodoTaskMessage message = TodoTaskMessage.builder()
                    .taskId(nodeInstance.getId())
                    .flowInstanceId(event.getFlowInstanceId())
                    .nodeId(event.getNodeId())
                    .approverId(event.getApproverId())
                    .documentId(flowInstance.getDocumentId())
                    .title(getTitle(flowInstance))
                    .content(getContent(flowInstance))
                    .createdAt(LocalDateTime.now())
                    .build();
            
            // 3. 发送消息到 RabbitMQ
            messageSender.send("workflow.exchange", "todo.task.created", message);
            
            log.info("待办消息发送成功，节点实例ID: {}, 审批人ID: {}", 
                    event.getNodeInstanceId(), event.getApproverId());
        } catch (Exception e) {
            log.error("待办消息发送失败，节点实例ID: {}, 审批人ID: {}", 
                    event.getNodeInstanceId(), event.getApproverId(), e);
            // 可以记录到失败队列，后续重试
        }
    }
    
    /**
     * 处理节点完成事件 - 发送已办消息
     * 
     * @param event 节点完成事件
     */
    @EventListener
    @Async
    public void handleNodeCompleted(NodeCompletedEvent event) {
        log.info("发送已办消息，节点实例ID: {}, 审批人ID: {}", 
                event.getNodeInstanceId(), event.getApproverId());
        
        try {
            // 1. 加载流程实例和节点实例
            FlowInstance flowInstance = flowInstanceRepository.findById(event.getFlowInstanceId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "流程实例不存在: " + event.getFlowInstanceId()));
            
            FlowNodeInstance nodeInstance = flowNodeInstanceRepository.findById(event.getNodeInstanceId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "节点实例不存在: " + event.getNodeInstanceId()));
            
            // 2. 构建已办消息
            DoneTaskMessage message = DoneTaskMessage.builder()
                    .taskId(nodeInstance.getId())
                    .flowInstanceId(event.getFlowInstanceId())
                    .nodeId(event.getNodeId())
                    .handlerId(event.getApproverId())
                    .action(TaskAction.APPROVE) // 可以从事件中获取
                    .documentId(flowInstance.getDocumentId())
                    .title(getTitle(flowInstance))
                    .comments(nodeInstance.getComments())
                    .handledAt(LocalDateTime.now())
                    .build();
            
            // 3. 发送消息到 RabbitMQ
            messageSender.send("workflow.exchange", "done.task.created", message);
            
            log.info("已办消息发送成功，节点实例ID: {}, 审批人ID: {}", 
                    event.getNodeInstanceId(), event.getApproverId());
        } catch (Exception e) {
            log.error("已办消息发送失败，节点实例ID: {}, 审批人ID: {}", 
                    event.getNodeInstanceId(), event.getApproverId(), e);
            // 可以记录到失败队列，后续重试
        }
    }
    
    private String getTitle(FlowInstance flowInstance) {
        Object title = flowInstance.getProcessVariables().getVariable("title");
        return title != null ? title.toString() : "待办任务";
    }
    
    private String getContent(FlowInstance flowInstance) {
        Object content = flowInstance.getProcessVariables().getVariable("content");
        return content != null ? content.toString() : "";
    }
    
    /**
     * 待办任务消息 DTO
     */
    public static class TodoTaskMessage {
        private Long taskId;
        private Long flowInstanceId;
        private Long nodeId;
        private Long approverId;
        private Long documentId;
        private String title;
        private String content;
        private LocalDateTime createdAt;
        
        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private TodoTaskMessage message = new TodoTaskMessage();
            
            public Builder taskId(Long taskId) { message.taskId = taskId; return this; }
            public Builder flowInstanceId(Long flowInstanceId) { message.flowInstanceId = flowInstanceId; return this; }
            public Builder nodeId(Long nodeId) { message.nodeId = nodeId; return this; }
            public Builder approverId(Long approverId) { message.approverId = approverId; return this; }
            public Builder documentId(Long documentId) { message.documentId = documentId; return this; }
            public Builder title(String title) { message.title = title; return this; }
            public Builder content(String content) { message.content = content; return this; }
            public Builder createdAt(LocalDateTime createdAt) { message.createdAt = createdAt; return this; }
            public TodoTaskMessage build() { return message; }
        }
        
        // Getters
        public Long getTaskId() { return taskId; }
        public Long getFlowInstanceId() { return flowInstanceId; }
        public Long getNodeId() { return nodeId; }
        public Long getApproverId() { return approverId; }
        public Long getDocumentId() { return documentId; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
    
    /**
     * 已办任务消息 DTO
     */
    public static class DoneTaskMessage {
        private Long taskId;
        private Long flowInstanceId;
        private Long nodeId;
        private Long handlerId;
        private TaskAction action;
        private Long documentId;
        private String title;
        private String comments;
        private LocalDateTime handledAt;
        
        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private DoneTaskMessage message = new DoneTaskMessage();
            
            public Builder taskId(Long taskId) { message.taskId = taskId; return this; }
            public Builder flowInstanceId(Long flowInstanceId) { message.flowInstanceId = flowInstanceId; return this; }
            public Builder nodeId(Long nodeId) { message.nodeId = nodeId; return this; }
            public Builder handlerId(Long handlerId) { message.handlerId = handlerId; return this; }
            public Builder action(TaskAction action) { message.action = action; return this; }
            public Builder documentId(Long documentId) { message.documentId = documentId; return this; }
            public Builder title(String title) { message.title = title; return this; }
            public Builder comments(String comments) { message.comments = comments; return this; }
            public Builder handledAt(LocalDateTime handledAt) { message.handledAt = handledAt; return this; }
            public DoneTaskMessage build() { return message; }
        }
        
        // Getters
        public Long getTaskId() { return taskId; }
        public Long getFlowInstanceId() { return flowInstanceId; }
        public Long getNodeId() { return nodeId; }
        public Long getHandlerId() { return handlerId; }
        public TaskAction getAction() { return action; }
        public Long getDocumentId() { return documentId; }
        public String getTitle() { return title; }
        public String getComments() { return comments; }
        public LocalDateTime getHandledAt() { return handledAt; }
    }
}

