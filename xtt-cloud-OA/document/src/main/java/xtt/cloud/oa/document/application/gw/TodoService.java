package xtt.cloud.oa.document.application.gw;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.document.domain.entity.flow.definition.Document;
import xtt.cloud.oa.document.domain.entity.flow.FlowInstance;
import xtt.cloud.oa.document.domain.entity.flow.FlowNodeInstance;
import xtt.cloud.oa.document.domain.entity.flow.task.DoneTask;
import xtt.cloud.oa.document.domain.entity.flow.task.TodoTask;
import xtt.cloud.oa.document.domain.mapper.flow.DoneTaskMapper;
import xtt.cloud.oa.document.domain.mapper.flow.TodoTaskMapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 待办事项服务
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Service
public class TodoService {
    
    private static final Logger log = LoggerFactory.getLogger(TodoService.class);
    
    private final TodoTaskMapper TodoTaskMapper;
    
    private final DoneTaskMapper DoneTaskMapper;

    public TodoService(TodoTaskMapper TodoTaskMapper, DoneTaskMapper DoneTaskMapper) {
        this.TodoTaskMapper = TodoTaskMapper;
        this.DoneTaskMapper = DoneTaskMapper;
    }


    /**
     * 创建待办事项
     */
    @Transactional
    public void createTodoTask(FlowNodeInstance nodeInstance, Long approverId, 
                              FlowInstance flowInstance, Document document) {
        log.debug("创建待办事项，节点实例ID: {}, 审批人ID: {}", nodeInstance.getId(), approverId);
        
        TodoTask todo = new TodoTask();
        todo.setDocumentId(flowInstance.getDocumentId());
        todo.setFlowInstanceId(flowInstance.getId());
        todo.setNodeInstanceId(nodeInstance.getId());
        todo.setAssigneeId(approverId);
        todo.setTitle("审批公文: " + document.getTitle());
        todo.setContent("请审批公文《" + document.getTitle() + "》");
        todo.setPriority(document.getUrgencyLevel() != null ? document.getUrgencyLevel() : TodoTask.PRIORITY_NORMAL);
        todo.setStatus(TodoTask.STATUS_PENDING);
        todo.setDueDate(calculateDueDate(nodeInstance));
        todo.setCreatedAt(LocalDateTime.now());
        todo.setUpdatedAt(LocalDateTime.now());
        
        TodoTaskMapper.insert(todo);
        log.info("待办事项创建成功，ID: {}, 审批人ID: {}", todo.getId(), approverId);
    }
    
    /**
     * 创建已办事项
     */
    @Transactional
    public void createDoneTask(FlowNodeInstance nodeInstance, String action, String comments,
                              FlowInstance flowInstance, Document document) {
        log.debug("创建已办事项，节点实例ID: {}, 操作: {}", nodeInstance.getId(), action);
        
        DoneTask done = new DoneTask();
        done.setDocumentId(flowInstance.getDocumentId());
        done.setFlowInstanceId(flowInstance.getId());
        done.setNodeInstanceId(nodeInstance.getId());
        done.setHandlerId(nodeInstance.getApproverId());
        done.setTitle("审批公文: " + document.getTitle());
        done.setAction(action);
        done.setComments(comments);
        done.setHandledAt(LocalDateTime.now());
        done.setCreatedAt(LocalDateTime.now());
        
        DoneTaskMapper.insert(done);
        log.info("已办事项创建成功，ID: {}, 操作: {}", done.getId(), action);
    }
    
    /**
     * 标记待办事项为已处理
     */
    @Transactional
    public void markAsHandled(Long nodeInstanceId, Long approverId) {
        log.debug("标记待办事项为已处理，节点实例ID: {}, 审批人ID: {}", nodeInstanceId, approverId);
        
        TodoTask todo = TodoTaskMapper.selectOne(
            new LambdaQueryWrapper<TodoTask>()
                .eq(TodoTask::getNodeInstanceId, nodeInstanceId)
                .eq(TodoTask::getAssigneeId, approverId)
                .eq(TodoTask::getStatus, TodoTask.STATUS_PENDING)
        );
        
        if (todo != null) {
            todo.setStatus(TodoTask.STATUS_HANDLED);
            todo.setHandledAt(LocalDateTime.now());
            todo.setUpdatedAt(LocalDateTime.now());
            TodoTaskMapper.updateById(todo);
            log.info("待办事项已标记为已处理，ID: {}", todo.getId());
        }
    }
    
    /**
     * 取消待办事项
     */
    @Transactional
    public void cancelTodoTask(Long TodoTaskId) {
        log.info("取消待办事项，ID: {}", TodoTaskId);
        
        TodoTask todo = TodoTaskMapper.selectById(TodoTaskId);
        if (todo != null) {
            todo.setStatus(TodoTask.STATUS_CANCELLED);
            todo.setUpdatedAt(LocalDateTime.now());
            TodoTaskMapper.updateById(todo);
            log.info("待办事项已取消，ID: {}", TodoTaskId);
        }
    }
    
    /**
     * 根据审批人ID获取待办事项列表
     */
    public IPage<TodoTask> getTodoTasksByAssignee(Long assigneeId, int pageNum, int pageSize) {
        log.debug("查询待办事项列表，审批人ID: {}, 页码: {}, 大小: {}", assigneeId, pageNum, pageSize);
        
        Page<TodoTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TodoTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TodoTask::getAssigneeId, assigneeId)
                   .eq(TodoTask::getStatus, TodoTask.STATUS_PENDING)
                   .orderByDesc(TodoTask::getCreatedAt);
        
        return TodoTaskMapper.selectPage(page, queryWrapper);
    }
    
    /**
     * 根据流程实例ID获取待办事项列表
     */
    public List<TodoTask> getTodoTasksByFlowInstance(Long flowInstanceId) {
        log.debug("查询流程实例的待办事项列表，流程实例ID: {}", flowInstanceId);
        
        return TodoTaskMapper.selectList(
            new LambdaQueryWrapper<TodoTask>()
                .eq(TodoTask::getFlowInstanceId, flowInstanceId)
                .eq(TodoTask::getStatus, TodoTask.STATUS_PENDING)
                .orderByDesc(TodoTask::getCreatedAt)
        );
    }
    
    /**
     * 根据审批人ID获取已办事项列表
     */
    public IPage<DoneTask> getDoneTasksByHandler(Long handlerId, int pageNum, int pageSize) {
        log.debug("查询已办事项列表，处理人ID: {}, 页码: {}, 大小: {}", handlerId, pageNum, pageSize);
        
        Page<DoneTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DoneTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DoneTask::getHandlerId, handlerId)
                   .orderByDesc(DoneTask::getHandledAt);
        
        return DoneTaskMapper.selectPage(page, queryWrapper);
    }
    
    /**
     * 根据流程实例ID获取已办事项列表
     */
    public List<DoneTask> getDoneTasksByFlowInstance(Long flowInstanceId) {
        log.debug("查询流程实例的已办事项列表，流程实例ID: {}", flowInstanceId);
        
        return DoneTaskMapper.selectList(
            new LambdaQueryWrapper<DoneTask>()
                .eq(DoneTask::getFlowInstanceId, flowInstanceId)
                .orderByDesc(DoneTask::getHandledAt)
        );
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

