package xtt.cloud.oa.workflow.domain.flow.repository;

import xtt.cloud.oa.workflow.domain.flow.model.entity.task.DoneTask;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Optional;

/**
 * 已办任务仓储接口
 * 
 * @author xtt
 */
public interface DoneTaskRepository {
    
    /**
     * 根据ID查找已办任务
     */
    Optional<DoneTask> findById(Long id);
    
    /**
     * 根据处理人ID查找已办任务列表
     */
    List<DoneTask> findByHandlerId(Long handlerId);
    
    /**
     * 根据流程实例ID查找已办任务列表
     */
    List<DoneTask> findByFlowInstanceId(Long flowInstanceId);
    
    /**
     * 根据处理人ID分页查询已办任务
     */
    IPage<DoneTask> findPageByHandlerId(Long handlerId, int pageNum, int pageSize);
    
    /**
     * 根据任务类型和处理人ID分页查询已办任务
     */
    IPage<DoneTask> findPageByTypeAndHandlerId(Integer taskType, Long handlerId, int pageNum, int pageSize);
    
    /**
     * 保存已办任务
     */
    DoneTask save(DoneTask task);
    
    /**
     * 更新已办任务
     */
    void update(DoneTask task);
    
    /**
     * 删除已办任务
     */
    void delete(Long id);
}

