package xtt.cloud.oa.workflow.infrastructure.persistence.repository.old;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Repository;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.TodoTaskMapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask;

import java.util.List;

/**
 * 待办任务 Repository
 * 封装 TodoTaskMapper 的数据访问操作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Repository
public class TodoTaskRepository {
    
    private final TodoTaskMapper mapper;
    
    public TodoTaskRepository(TodoTaskMapper mapper) {
        this.mapper = mapper;
    }
    
    /**
     * 根据ID查询待办任务
     */
    public TodoTask findById(Long id) {
        return mapper.selectById(id);
    }
    
    /**
     * 根据处理人ID查询待办任务列表
     */
    public List<TodoTask> findByAssigneeId(Long assigneeId) {
        return mapper.selectList(
            new LambdaQueryWrapper<TodoTask>()
                .eq(TodoTask::getAssigneeId, assigneeId)
        );
    }
    
    /**
     * 根据节点实例ID和审批人ID查询待办任务
     */
    public TodoTask findByNodeInstanceIdAndAssigneeId(Long nodeInstanceId, Long assigneeId) {
        return mapper.selectOne(
            new LambdaQueryWrapper<TodoTask>()
                .eq(TodoTask::getNodeInstanceId, nodeInstanceId)
                .eq(TodoTask::getAssigneeId, assigneeId)
                .eq(TodoTask::getStatus, TodoTask.STATUS_PENDING)
        );
    }
    
    /**
     * 根据流程实例ID查询待办任务列表
     */
    public List<TodoTask> findByFlowInstanceId(Long flowInstanceId) {
        return mapper.selectList(
            new LambdaQueryWrapper<TodoTask>()
                .eq(TodoTask::getFlowInstanceId, flowInstanceId)
                .eq(TodoTask::getStatus, TodoTask.STATUS_PENDING)
                .orderByDesc(TodoTask::getCreatedAt)
        );
    }
    
    /**
     * 根据审批人ID分页查询待办任务
     */
    public IPage<TodoTask> findPageByAssigneeId(Long assigneeId, int pageNum, int pageSize) {
        Page<TodoTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TodoTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TodoTask::getAssigneeId, assigneeId)
                   .eq(TodoTask::getStatus, TodoTask.STATUS_PENDING)
                   .orderByDesc(TodoTask::getCreatedAt);
        return mapper.selectPage(page, queryWrapper);
    }
    
    /**
     * 根据任务类型和审批人ID分页查询待办任务
     */
    public IPage<TodoTask> findPageByTypeAndAssigneeId(Integer taskType, Long assigneeId, int pageNum, int pageSize) {
        Page<TodoTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TodoTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TodoTask::getTaskType, taskType)
                   .eq(TodoTask::getAssigneeId, assigneeId)
                   .eq(TodoTask::getStatus, TodoTask.STATUS_PENDING)
                   .orderByDesc(TodoTask::getCreatedAt);
        return mapper.selectPage(page, queryWrapper);
    }
    
    /**
     * 保存待办任务
     */
    public void save(TodoTask task) {
        if (task.getId() == null) {
            mapper.insert(task);
        } else {
            mapper.updateById(task);
        }
    }
    
    /**
     * 更新待办任务
     */
    public void update(TodoTask task) {
        mapper.updateById(task);
    }
    
    /**
     * 删除待办任务
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

