package xtt.cloud.oa.workflow.infrastructure.persistence.repository.old;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.FlowInstanceHistoryMapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowInstanceHistory;

/**
 * 流程实例历史 Repository
 * 封装 FlowInstanceHistoryMapper 的数据访问操作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Repository
public class FlowInstanceHistoryRepositoryImpl {
    
    private final FlowInstanceHistoryMapper mapper;

    public FlowInstanceHistoryRepositoryImpl(FlowInstanceHistoryMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 根据ID查询流程实例历史
     */
    public FlowInstanceHistory findById(Long id) {
        return mapper.selectById(id);
    }
    
    /**
     * 根据流程实例ID查询流程实例历史
     */
    public FlowInstanceHistory findByFlowInstanceId(Long flowInstanceId) {
        return mapper.selectOne(
            new LambdaQueryWrapper<FlowInstanceHistory>()
                .eq(FlowInstanceHistory::getFlowInstanceId, flowInstanceId)
        );
    }
    
    /**
     * 保存流程实例历史
     */
    public void save(FlowInstanceHistory history) {
        if (history.getId() == null) {
            mapper.insert(history);
        } else {
            mapper.updateById(history);
        }
    }
    
    /**
     * 更新流程实例历史
     */
    public void update(FlowInstanceHistory history) {
        mapper.updateById(history);
    }
    
    /**
     * 删除流程实例历史
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

