package xtt.cloud.oa.workflow.domain.flow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.model.entity.task.DoneTask;
import xtt.cloud.oa.workflow.domain.flow.model.entity.task.TodoTask;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskAction;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskPriority;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskType;
import xtt.cloud.oa.workflow.domain.flow.repository.DoneTaskRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.TodoTaskRepository;
import xtt.cloud.oa.workflow.domain.flow.service.TaskService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 作者: HuTianRui
 * 日期: 2025/12/29
 * 描述: 这里是对代码的描述
 */
@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TodoTaskRepository todoTaskRepository;
    private final DoneTaskRepository doneTaskRepository;

    public TaskServiceImpl(TodoTaskRepository todoTaskRepository, DoneTaskRepository doneTaskRepository) {
        this.todoTaskRepository = todoTaskRepository;
        this.doneTaskRepository = doneTaskRepository;
    }


    @Override
    public void createTodoTask(FlowNodeInstance nodeInstance, Long approverId, FlowInstance flowInstance) {
        log.debug("创建待办任务，节点实例ID: {}, 审批人ID: {}",
                nodeInstance.getId(), approverId);

        String title = "待办任务";
        String content = "";
        // 1. 获取文档信息 todo
        TaskPriority priority = TaskPriority.NORMAL;
        Object titleObject = flowInstance.getProcessVariables().getVariable("title");
        Object contentObject = flowInstance.getProcessVariables().getVariable("content");
        if(titleObject != null) {
            title = (String) titleObject;
        }
        if(contentObject != null) {
            content = (String) contentObject;
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

    @Override
    public void createDoneTask(FlowInstance flowInstance, FlowNodeInstance nodeInstance, TaskAction action, String comments) {

        log.debug("创建已办任务，节点实例ID: {}, 操作: {}", nodeInstance.getId(), action);

        // 1. 获取文档信息
        String title = "已办任务";
        Object titleObject = flowInstance.getProcessVariables().getVariable("title");
        if(titleObject != null) {
            title = (String) titleObject;
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

    @Override
    public void markAsHandled(FlowNodeInstance nodeInstance,
                              TaskAction action,
                              String comments,
                              FlowInstance flowInstance) {
        log.debug("标记待办任务为已处理，节点实例ID: {}, 审批人ID: {}", nodeInstance.getId(), nodeInstance.getApprover());

        // 2. 获取处理人ID
        Long handlerId = nodeInstance.getApprover() != null
                ? nodeInstance.getApprover().getUserId()
                : null;

        if (handlerId == null) {
            log.warn("节点实例没有审批人，无法创建已办任务，节点实例ID: {}", nodeInstance.getId());
            return;
        }

        Optional<TodoTask> todoOpt = todoTaskRepository.findByNodeInstanceIdAndAssigneeId(
                nodeInstance.getId(), handlerId);

        if (todoOpt.isPresent()) {
            TodoTask todo = todoOpt.get();
            todo.markAsHandled();
            todoTaskRepository.save(todo);
            createDoneTask(flowInstance, nodeInstance, action, comments);

            log.info("待办任务已标记为已处理，ID: {}", todo.getId());
        } else {
            log.warn("待办任务不存在，节点实例ID: {}, 审批人ID: {}", nodeInstance.getId(), nodeInstance.getApprover());
        }
    }

    @Override
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

    @Override
    public void cancelTodoTasks(List<Long> todoTaskIds) {{
        log.info("取消待办任务，IDs: {}", todoTaskIds.toString());
        todoTaskIds.forEach(this::cancelTodoTask);
//        todoTaskRepository.
        log.info("待办任务已取消，IDs: {}", todoTaskIds);
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
