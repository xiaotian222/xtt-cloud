package xtt.cloud.oa.workflow.infrastructure.persistence.repository.old;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.ActivityHistoryMapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.pojo.ActivityHistory;

import java.util.List;

/**
 * 活动历史 Repository
 * 封装 ActivityHistoryMapper 的数据访问操作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Repository
public class ActivityHistoryRepositoryImpl {
    
    private final ActivityHistoryMapper mapper;

    public ActivityHistoryRepositoryImpl(ActivityHistoryMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 根据ID查询活动历史
     */
    public ActivityHistory findById(Long id) {
        return mapper.selectById(id);
    }
    
    /**
     * 根据流程实例ID查询活动历史列表
     */
    public List<ActivityHistory> findByFlowInstanceId(Long flowInstanceId) {
        return mapper.selectList(
            new LambdaQueryWrapper<ActivityHistory>()
                .eq(ActivityHistory::getFlowInstanceId, flowInstanceId)
                .orderByAsc(ActivityHistory::getActivityTime)
        );
    }
    
    /**
     * 根据节点实例ID查询活动历史列表
     */
    public List<ActivityHistory> findByNodeInstanceId(Long nodeInstanceId) {
        return mapper.selectList(
            new LambdaQueryWrapper<ActivityHistory>()
                .eq(ActivityHistory::getNodeInstanceId, nodeInstanceId)
                .orderByAsc(ActivityHistory::getActivityTime)
        );
    }
    
    /**
     * 保存活动历史
     */
    public void save(ActivityHistory history) {
        if (history.getId() == null) {
            mapper.insert(history);
        } else {
            mapper.updateById(history);
        }
    }
    
    /**
     * 更新活动历史
     */
    public void update(ActivityHistory history) {
        mapper.updateById(history);
    }
    
    /**
     * 删除活动历史
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

