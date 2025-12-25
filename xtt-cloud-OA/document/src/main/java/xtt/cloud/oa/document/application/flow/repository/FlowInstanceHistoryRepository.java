package xtt.cloud.oa.document.application.flow.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;
import xtt.cloud.oa.document.domain.entity.flow.history.FlowInstanceHistory;
import xtt.cloud.oa.document.domain.mapper.flow.FlowInstanceHistoryMapper;

/**
 * 流程实例历史 Repository
 * 封装 FlowInstanceHistoryMapper 的数据访问操作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Repository
public class FlowInstanceHistoryRepository {
    
    private final FlowInstanceHistoryMapper mapper;
    
    public FlowInstanceHistoryRepository(FlowInstanceHistoryMapper mapper) {
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

