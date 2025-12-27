package xtt.cloud.oa.workflow.application.flow.task;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.model.entity.task.TodoTask;
import xtt.cloud.oa.workflow.domain.flow.model.entity.task.DoneTask;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskPriority;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskAction;
import xtt.cloud.oa.workflow.domain.flow.repository.TodoTaskRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.DoneTaskRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.DocumentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 任务应用服务
 * 
 * 负责待办任务和已办任务的管理
 * 
 * @author xtt
 */
@Service
public class TaskService {
    
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);
    
    private final TodoTaskRepository todoTaskRepository;
    private final DoneTaskRepository doneTaskRepository;
    private final DocumentRepository documentRepository;
    
    public TaskService(
            TodoTaskRepository todoTaskRepository,
            DoneTaskRepository doneTaskRepository,
            DocumentRepository documentRepository) {
        this.todoTaskRepository = todoTaskRepository;
        this.doneTaskRepository = doneTaskRepository;
        this.documentRepository = documentRepository;
    }
    
    /**
     * 创建待办任务
     */
    @Transactional
    public void createTodoTask(
            FlowNodeInstance nodeInstance,
            Long approverId,
            FlowInstance flowInstance) {
        
        log.debug("创建待办任务，节点实例ID: {}, 审批人ID: {}", 
                nodeInstance.getId(), approverId);
        
        // 1. 获取文档信息
        String title = "待办任务";
        String content = null;
        TaskPriority priority = TaskPriority.NORMAL;
        
        if (flowInstance.getDocumentId() != null) {
            Optional<xtt.cloud.oa.workflow.domain.flow.model.entity.Document> documentOpt = 
                    documentRepository.findById(flowInstance.getDocumentId());
            if (documentOpt.isPresent()) {
                xtt.cloud.oa.workflow.domain.flow.model.entity.Document document = documentOpt.get();
                title = document.getTitle() != null ? document.getTitle() : title;
                // content 从 PO 获取
                if (document.getPO() != null) {
                    content = document.getPO().getContent();
                }
                // TODO: 从文档获取紧急程度，转换为优先级
            }
        }
        
        // 2. 计算截止时间（默认3个工作日）
        LocalDateTime dueDate = calculateDueDate(nodeInstance);
        
        // 3. 创建待办任务
        TodoTask todo = TodoTask.create(
                flowInstance.getDocumentId(),
                flowInstance.getId() != null ? flowInstance.getId().getValue() : null,
                nodeInstance.getId(),
                approverId,
                title,
                content,
                TaskType.USER,
                priority,
                dueDate);
        
        // 4. 保存待办任务
        todoTaskRepository.save(todo);
        
        log.info("待办任务创建成功，ID: {}, 审批人ID: {}, 任务类型: {}", 
                todo.getId(), approverId, todo.getTaskType());
    }
    
    /**
     * 创建已办任务
     */
    @Transactional
    public void createDoneTask(
            FlowNodeInstance nodeInstance,
            TaskAction action,
            String comments,
            FlowInstance flowInstance) {
        
        log.debug("创建已办任务，节点实例ID: {}, 操作: {}", nodeInstance.getId(), action);
        
        // 1. 获取文档信息
        String title = "已办任务";
        if (flowInstance.getDocumentId() != null) {
            Optional<xtt.cloud.oa.workflow.domain.flow.model.entity.Document> documentOpt = 
                    documentRepository.findById(flowInstance.getDocumentId());
            if (documentOpt.isPresent()) {
                title = documentOpt.get().getTitle() != null ? documentOpt.get().getTitle() : title;
            }
        }
        
        // 2. 获取处理人ID
        Long handlerId = nodeInstance.getApprover() != null 
                ? nodeInstance.getApprover().getUserId() 
                : null;
        
        if (handlerId == null) {
            log.warn("节点实例没有审批人，无法创建已办任务，节点实例ID: {}", nodeInstance.getId());
            return;
        }
        
        // 3. 创建已办任务
        DoneTask done = DoneTask.create(
                flowInstance.getDocumentId(),
                flowInstance.getId() != null ? flowInstance.getId().getValue() : null,
                nodeInstance.getId(),
                handlerId,
                title,
                TaskType.USER,
                action,
                comments);
        
        // 4. 保存已办任务
        doneTaskRepository.save(done);
        
        log.info("已办任务创建成功，ID: {}, 操作: {}, 任务类型: {}", 
                done.getId(), action, done.getTaskType());
    }
    
    /**
     * 标记待办任务为已处理
     */
    @Transactional
    public void markAsHandled(Long nodeInstanceId, Long approverId) {
        log.debug("标记待办任务为已处理，节点实例ID: {}, 审批人ID: {}", nodeInstanceId, approverId);
        
        Optional<TodoTask> todoOpt = todoTaskRepository.findByNodeInstanceIdAndAssigneeId(
                nodeInstanceId, approverId);
        
        if (todoOpt.isPresent()) {
            TodoTask todo = todoOpt.get();
            todo.markAsHandled();
            todoTaskRepository.update(todo);
            log.info("待办任务已标记为已处理，ID: {}", todo.getId());
        } else {
            log.warn("待办任务不存在，节点实例ID: {}, 审批人ID: {}", nodeInstanceId, approverId);
        }
    }
    
    /**
     * 取消待办任务
     */
    @Transactional
    public void cancelTodoTask(Long todoTaskId) {
        log.info("取消待办任务，ID: {}", todoTaskId);
        
        Optional<TodoTask> todoOpt = todoTaskRepository.findById(todoTaskId);
        if (todoOpt.isPresent()) {
            TodoTask todo = todoOpt.get();
            todo.cancel();
            todoTaskRepository.update(todo);
            log.info("待办任务已取消，ID: {}", todoTaskId);
        } else {
            log.warn("待办任务不存在，ID: {}", todoTaskId);
        }
    }

    /**
     * 计算截止时间
     * TODO: 根据节点配置的处理时限计算
     */
    private LocalDateTime calculateDueDate(FlowNodeInstance nodeInstance) {
        // 默认3个工作日
        return LocalDateTime.now().plusDays(3);
    }
}
