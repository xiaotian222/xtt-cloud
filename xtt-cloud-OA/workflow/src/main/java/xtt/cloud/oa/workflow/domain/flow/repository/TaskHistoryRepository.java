package xtt.cloud.oa.workflow.domain.flow.repository;

import xtt.cloud.oa.workflow.domain.flow.model.entity.history.TaskHistory;

import java.util.List;
import java.util.Optional;

/**
 * 任务历史仓储接口
 * 
 * @author xtt
 */
public interface TaskHistoryRepository {
    
    /**
     * 根据ID查找任务历史
     */
    Optional<TaskHistory> findById(Long id);
    
    /**
     * 根据流程实例ID查找任务历史列表
     */
    List<TaskHistory> findByFlowInstanceId(Long flowInstanceId);
    
    /**
     * 根据节点实例ID查找任务历史
     */
    Optional<TaskHistory> findByNodeInstanceId(Long nodeInstanceId);
    
    /**
     * 根据处理人ID查找任务历史列表
     */
    List<TaskHistory> findByHandlerId(Long handlerId);
    
    /**
     * 保存任务历史
     */
    TaskHistory save(TaskHistory history);
    
    /**
     * 更新任务历史
     */
    void update(TaskHistory history);
    
    /**
     * 删除任务历史
     */
    void delete(Long id);
}

