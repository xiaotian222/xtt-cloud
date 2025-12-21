package xtt.cloud.oa.workflow.infrastructure.persistence.repository.old;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.TaskHistoryMapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TaskHistory;

import java.util.List;

/**
 * 任务历史 Repository
 * 封装 TaskHistoryMapper 的数据访问操作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Repository
public class TaskHistoryRepositoryImpl {
    
    private final TaskHistoryMapper mapper;

    public TaskHistoryRepositoryImpl(TaskHistoryMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 根据ID查询任务历史
     */
    public TaskHistory findById(Long id) {
        return mapper.selectById(id);
    }
    
    /**
     * 根据流程实例ID查询任务历史列表
     */
    public List<TaskHistory> findByFlowInstanceId(Long flowInstanceId) {
        return mapper.selectList(
            new LambdaQueryWrapper<TaskHistory>()
                .eq(TaskHistory::getFlowInstanceId, flowInstanceId)
                .orderByAsc(TaskHistory::getStartTime)
        );
    }
    
    /**
     * 根据节点实例ID查询任务历史
     */
    public TaskHistory findByNodeInstanceId(Long nodeInstanceId) {
        return mapper.selectOne(
            new LambdaQueryWrapper<TaskHistory>()
                .eq(TaskHistory::getNodeInstanceId, nodeInstanceId)
        );
    }
    
    /**
     * 根据处理人ID查询任务历史列表
     */
    public List<TaskHistory> findByHandlerId(Long handlerId) {
        return mapper.selectList(
            new LambdaQueryWrapper<TaskHistory>()
                .eq(TaskHistory::getHandlerId, handlerId)
                .orderByDesc(TaskHistory::getEndTime)
        );
    }
    
    /**
     * 保存任务历史
     */
    public void save(TaskHistory history) {
        if (history.getId() == null) {
            mapper.insert(history);
        } else {
            mapper.updateById(history);
        }
    }
    
    /**
     * 更新任务历史
     */
    public void update(TaskHistory history) {
        mapper.updateById(history);
    }
    
    /**
     * 删除任务历史
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

