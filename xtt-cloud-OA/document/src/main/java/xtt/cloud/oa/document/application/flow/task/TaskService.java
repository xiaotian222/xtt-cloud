package xtt.cloud.oa.document.application.flow.task;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.document.domain.entity.flow.definition.Document;
import xtt.cloud.oa.document.domain.entity.flow.task.DoneTask;
import xtt.cloud.oa.document.domain.entity.flow.FlowInstance;
import xtt.cloud.oa.document.domain.entity.flow.FlowNodeInstance;
import xtt.cloud.oa.document.domain.entity.flow.task.TodoTask;
import xtt.cloud.oa.document.application.flow.repository.DoneTaskRepository;
import xtt.cloud.oa.document.application.flow.repository.TodoTaskRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务服务
 * 负责待办任务和已办任务的管理
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Service
public class TaskService {
    
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);
    
    private final TodoTaskRepository todoTaskRepository;
    
    private final DoneTaskRepository doneTaskRepository;

    public TaskService(TodoTaskRepository todoTaskRepository, DoneTaskRepository doneTaskRepository) {
        this.todoTaskRepository = todoTaskRepository;
        this.doneTaskRepository = doneTaskRepository;
    }

    /**
     * 创建待办任务
     */
    @Transactional
    public void createTodoTask(FlowNodeInstance nodeInstance, Long approverId, 
                              FlowInstance flowInstance, Document document) {
        log.debug("创建待办任务，节点实例ID: {}, 审批人ID: {}", nodeInstance.getId(), approverId);
        
        TodoTask todo = new TodoTask();
        todo.setDocumentId(flowInstance.getDocumentId());
        todo.setFlowInstanceId(flowInstance.getId());
        todo.setNodeInstanceId(nodeInstance.getId());
        todo.setAssigneeId(approverId);
        todo.setTitle(document.getTitle());
        todo.setContent(document.getContent());
        todo.setTaskType(TodoTask.TASK_TYPE_USER); // 默认用户任务
        todo.setPriority(document.getUrgencyLevel() != null ? document.getUrgencyLevel() : TodoTask.PRIORITY_NORMAL);
        todo.setStatus(TodoTask.STATUS_PENDING);
        todo.setDueDate(calculateDueDate(nodeInstance));
        todo.setCreatedAt(LocalDateTime.now());
        todo.setUpdatedAt(LocalDateTime.now());
        
        todoTaskRepository.save(todo);
        log.info("待办任务创建成功，ID: {}, 审批人ID: {}, 任务类型: {}", 
                todo.getId(), approverId, todo.getTaskType());
    }
    
    /**
     * 创建已办任务
     */
    @Transactional
    public void createDoneTask(FlowNodeInstance nodeInstance, String action, String comments,
                              FlowInstance flowInstance, Document document) {
        log.debug("创建已办任务，节点实例ID: {}, 操作: {}", nodeInstance.getId(), action);
        
        DoneTask done = new DoneTask();
        done.setDocumentId(flowInstance.getDocumentId());
        done.setFlowInstanceId(flowInstance.getId());
        done.setNodeInstanceId(nodeInstance.getId());
        done.setHandlerId(nodeInstance.getApproverId());
        done.setTitle(document.getTitle());
        done.setTaskType(DoneTask.TASK_TYPE_USER); // 默认用户任务
        done.setAction(action);
        done.setComments(comments);
        done.setHandledAt(LocalDateTime.now());
        done.setCreatedAt(LocalDateTime.now());
        
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
        
        TodoTask todo = todoTaskRepository.findByNodeInstanceIdAndAssigneeId(nodeInstanceId, approverId);
        
        if (todo != null) {
            todo.setStatus(TodoTask.STATUS_HANDLED);
            todo.setHandledAt(LocalDateTime.now());
            todo.setUpdatedAt(LocalDateTime.now());
            todoTaskRepository.update(todo);
            log.info("待办任务已标记为已处理，ID: {}", todo.getId());
        }
    }
    
    /**
     * 取消待办任务
     */
    @Transactional
    public void cancelTodoTask(Long todoTaskId) {
        log.info("取消待办任务，ID: {}", todoTaskId);
        
        TodoTask todo = todoTaskRepository.findById(todoTaskId);
        if (todo != null) {
            todo.setStatus(TodoTask.STATUS_CANCELLED);
            todo.setUpdatedAt(LocalDateTime.now());
            todoTaskRepository.update(todo);
            log.info("待办任务已取消，ID: {}", todoTaskId);
        }
    }
    
    /**
     * 根据审批人ID获取待办任务列表
     */
    public IPage<TodoTask> getTodoTasksByAssignee(Long assigneeId, int pageNum, int pageSize) {
        log.debug("查询待办任务列表，审批人ID: {}, 页码: {}, 大小: {}", assigneeId, pageNum, pageSize);
        
        return todoTaskRepository.findPageByAssigneeId(assigneeId, pageNum, pageSize);
    }
    
    /**
     * 根据流程实例ID获取待办任务列表
     */
    public List<TodoTask> getTodoTasksByFlowInstance(Long flowInstanceId) {
        log.debug("查询流程实例的待办任务列表，流程实例ID: {}", flowInstanceId);
        
        return todoTaskRepository.findByFlowInstanceId(flowInstanceId);
    }
    
    /**
     * 根据处理人ID获取已办任务列表
     */
    public IPage<DoneTask> getDoneTasksByHandler(Long handlerId, int pageNum, int pageSize) {
        log.debug("查询已办任务列表，处理人ID: {}, 页码: {}, 大小: {}", handlerId, pageNum, pageSize);
        
        return doneTaskRepository.findPageByHandlerId(handlerId, pageNum, pageSize);
    }
    
    /**
     * 根据流程实例ID获取已办任务列表
     */
    public List<DoneTask> getDoneTasksByFlowInstance(Long flowInstanceId) {
        log.debug("查询流程实例的已办任务列表，流程实例ID: {}", flowInstanceId);
        
        return doneTaskRepository.findByFlowInstanceId(flowInstanceId);
    }
    
    /**
     * 根据任务类型查询待办任务列表
     */
    public IPage<TodoTask> getTodoTasksByType(Integer taskType, Long assigneeId, int pageNum, int pageSize) {
        log.debug("根据任务类型查询待办任务列表，任务类型: {}, 审批人ID: {}, 页码: {}, 大小: {}", 
                taskType, assigneeId, pageNum, pageSize);
        
        return todoTaskRepository.findPageByTypeAndAssigneeId(taskType, assigneeId, pageNum, pageSize);
    }
    
    /**
     * 根据任务类型查询已办任务列表
     */
    public IPage<DoneTask> getDoneTasksByType(Integer taskType, Long handlerId, int pageNum, int pageSize) {
        log.debug("根据任务类型查询已办任务列表，任务类型: {}, 处理人ID: {}, 页码: {}, 大小: {}", 
                taskType, handlerId, pageNum, pageSize);
        
        return doneTaskRepository.findPageByTypeAndHandlerId(taskType, handlerId, pageNum, pageSize);
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

