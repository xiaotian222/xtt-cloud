package xtt.cloud.oa.workflow.application.flow;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.workflow.application.flow.task.TaskApplicationService;
import xtt.cloud.oa.workflow.domain.flow.model.entity.task.DoneTask;
import xtt.cloud.oa.workflow.domain.flow.model.entity.task.TodoTask;
import xtt.cloud.oa.workflow.domain.flow.repository.DoneTaskRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.TodoTaskRepository;

import java.util.List;

/**
 * 作者: HuTianRui
 * 日期: 2025/12/26
 * 描述: 这里是对代码的描述
 */

@Service
public class TaskQueryService {
    private static final Logger log = LoggerFactory.getLogger(TaskQueryService.class);

    private final TodoTaskRepository todoTaskRepository;
    private final DoneTaskRepository doneTaskRepository;

    public TaskQueryService(TodoTaskRepository todoTaskRepository, DoneTaskRepository doneTaskRepository) {
        this.todoTaskRepository = todoTaskRepository;
        this.doneTaskRepository = doneTaskRepository;
    }

    /**
     * 根据审批人ID获取待办任务列表
     */
    @Transactional(readOnly = true)
    public IPage<TodoTask> getTodoTasksByAssignee(Long assigneeId, int pageNum, int pageSize) {
        log.debug("查询待办任务列表，审批人ID: {}, 页码: {}, 大小: {}", assigneeId, pageNum, pageSize);
        return todoTaskRepository.findPageByAssigneeId(assigneeId, pageNum, pageSize);
    }

    /**
     * 根据流程实例ID获取待办任务列表
     */
    @Transactional(readOnly = true)
    public List<TodoTask> getTodoTasksByFlowInstance(Long flowInstanceId) {
        log.debug("查询流程实例的待办任务列表，流程实例ID: {}", flowInstanceId);

        return todoTaskRepository.findByFlowInstanceId(flowInstanceId);
    }

    /**
     * 根据处理人ID获取已办任务列表
     */
    @Transactional(readOnly = true)
    public IPage<DoneTask> getDoneTasksByHandler(Long handlerId, int pageNum, int pageSize) {
        log.debug("查询已办任务列表，处理人ID: {}, 页码: {}, 大小: {}", handlerId, pageNum, pageSize);

        return doneTaskRepository.findPageByHandlerId(handlerId, pageNum, pageSize);
    }

    /**
     * 根据流程实例ID获取已办任务列表
     */
    @Transactional(readOnly = true)
    public List<DoneTask> getDoneTasksByFlowInstance(Long flowInstanceId) {
        log.debug("查询流程实例的已办任务列表，流程实例ID: {}", flowInstanceId);

        return doneTaskRepository.findByFlowInstanceId(flowInstanceId);
    }

    /**
     * 根据任务类型查询待办任务列表
     */
    @Transactional(readOnly = true)
    public IPage<TodoTask> getTodoTasksByType(Integer taskType, Long assigneeId, int pageNum, int pageSize) {
        log.debug("根据任务类型查询待办任务列表，任务类型: {}, 审批人ID: {}, 页码: {}, 大小: {}",
                taskType, assigneeId, pageNum, pageSize);

        return todoTaskRepository.findPageByTypeAndAssigneeId(taskType, assigneeId, pageNum, pageSize);
    }

    /**
     * 根据任务类型查询已办任务列表
     */
    @Transactional(readOnly = true)
    public IPage<DoneTask> getDoneTasksByType(Integer taskType, Long handlerId, int pageNum, int pageSize) {
        log.debug("根据任务类型查询已办任务列表，任务类型: {}, 处理人ID: {}, 页码: {}, 大小: {}",
                taskType, handlerId, pageNum, pageSize);

        return doneTaskRepository.findPageByTypeAndHandlerId(taskType, handlerId, pageNum, pageSize);
    }

}
