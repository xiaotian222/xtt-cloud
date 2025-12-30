package xtt.cloud.oa.workflow.infrastructure.messaging.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.workflow.domain.flow.event.NodeCompletedEvent;
import xtt.cloud.oa.workflow.domain.flow.event.NodeInstanceCreatedEvent;
import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskAction;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowInstanceRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeInstanceRepository;
import xtt.cloud.oa.workflow.domain.flow.service.TaskService;

/**
 * 待办已办任务事件处理器
 * 
 * 职责：
 * 1. 监听节点实例创建事件，创建待办任务
 * 2. 监听节点完成事件，创建已办任务
 * 
 * 注意：使用 @Async 异步处理，不阻塞主流程
 * 
 * @author xtt
 */
@Component
public class TaskEventHandler {
    
    private static final Logger log = LoggerFactory.getLogger(TaskEventHandler.class);
    
    private final TaskService taskService;
    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowNodeInstanceRepository flowNodeInstanceRepository;
    
    public TaskEventHandler(
            TaskService taskService,
            FlowInstanceRepository flowInstanceRepository,
            FlowNodeInstanceRepository flowNodeInstanceRepository) {
        this.taskService = taskService;
        this.flowInstanceRepository = flowInstanceRepository;
        this.flowNodeInstanceRepository = flowNodeInstanceRepository;
    }
    
    /**
     * 处理节点实例创建事件 - 创建待办任务
     * 
     * @param event 节点实例创建事件
     */
    @EventListener
    @Async
    @Transactional
    public void handleNodeInstanceCreated(NodeInstanceCreatedEvent event) {
        log.info("处理节点实例创建事件，节点实例ID: {}, 审批人ID: {}", 
                event.getNodeInstanceId(), event.getApproverId());
        
        try {
            // 1. 加载流程实例
            FlowInstance flowInstance = flowInstanceRepository.findById(event.getFlowInstanceId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "流程实例不存在: " + event.getFlowInstanceId()));
            
            // 2. 加载节点实例
            FlowNodeInstance nodeInstance = flowNodeInstanceRepository.findById(event.getNodeInstanceId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "节点实例不存在: " + event.getNodeInstanceId()));
            
            // 3. 创建待办任务
            taskService.createTodoTask(nodeInstance, event.getApproverId(), flowInstance);
            
            log.info("待办任务创建成功，节点实例ID: {}, 审批人ID: {}", 
                    event.getNodeInstanceId(), event.getApproverId());
        } catch (Exception e) {
            log.error("待办任务创建失败，节点实例ID: {}, 审批人ID: {}", 
                    event.getNodeInstanceId(), event.getApproverId(), e);
            // 可以记录到失败队列，后续重试
            throw e;
        }
    }
    
    /**
     * 处理节点完成事件 - 创建已办任务
     * 
     * @param event 节点完成事件
     */
    @EventListener
    @Async
    @Transactional
    public void handleNodeCompleted(NodeCompletedEvent event) {
        log.info("处理节点完成事件，节点实例ID: {}, 审批人ID: {}", 
                event.getNodeInstanceId(), event.getApproverId());
        
        try {
            // 1. 加载流程实例
            FlowInstance flowInstance = flowInstanceRepository.findById(event.getFlowInstanceId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "流程实例不存在: " + event.getFlowInstanceId()));
            
            // 2. 加载节点实例
            FlowNodeInstance nodeInstance = flowNodeInstanceRepository.findById(event.getNodeInstanceId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "节点实例不存在: " + event.getNodeInstanceId()));
            
            // 3. 确定操作类型（从事件中获取，如果没有则默认为 APPROVE）
            TaskAction action = TaskAction.APPROVE; // 可以从事件中获取
            
            // 4. 创建已办任务
            taskService.createDoneTask(flowInstance, nodeInstance, action, null);
            
            log.info("已办任务创建成功，节点实例ID: {}, 审批人ID: {}", 
                    event.getNodeInstanceId(), event.getApproverId());
        } catch (Exception e) {
            log.error("已办任务创建失败，节点实例ID: {}, 审批人ID: {}", 
                    event.getNodeInstanceId(), event.getApproverId(), e);
            // 可以记录到失败队列，后续重试
            throw e;
        }
    }
}

