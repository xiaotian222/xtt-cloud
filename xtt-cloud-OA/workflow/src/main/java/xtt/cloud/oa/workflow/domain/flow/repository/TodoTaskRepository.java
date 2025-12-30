package xtt.cloud.oa.workflow.domain.flow.repository;

import xtt.cloud.oa.workflow.domain.flow.model.entity.task.TodoTask;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Optional;

/**
 * 待办任务仓储接口
 * 
 * @author xtt
 */
public interface TodoTaskRepository {
    
    /**
     * 根据ID查找待办任务
     */
    Optional<TodoTask> findById(Long id);
    
    /**
     * 根据处理人ID查找待办任务列表
     */
    List<TodoTask> findByAssigneeId(Long assigneeId);
    
    /**
     * 根据节点实例ID和处理人ID查找待办任务
     */
    Optional<TodoTask> findByNodeInstanceIdAndAssigneeId(Long nodeInstanceId, Long assigneeId);
    
    /**
     * 根据流程实例ID查找待办任务列表
     */
    List<TodoTask> findByFlowInstanceId(Long flowInstanceId);
    
    /**
     * 根据处理人ID分页查询待办任务
     */
    IPage<TodoTask> findPageByAssigneeId(Long assigneeId, int pageNum, int pageSize);
    
    /**
     * 根据任务类型和处理人ID分页查询待办任务
     */
    IPage<TodoTask> findPageByTypeAndAssigneeId(Integer taskType, Long assigneeId, int pageNum, int pageSize);
    
    /**
     * 保存待办任务
     */
    TodoTask save(TodoTask task);
    
    /**
     * 更新待办任务
     */
    void update(TodoTask task);
    
    /**
     * 删除待办任务
     */
    void delete(Long id);
}

