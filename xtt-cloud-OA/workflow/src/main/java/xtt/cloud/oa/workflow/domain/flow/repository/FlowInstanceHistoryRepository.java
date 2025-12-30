package xtt.cloud.oa.workflow.domain.flow.repository;

import xtt.cloud.oa.workflow.domain.flow.model.entity.history.FlowInstanceHistory;

import java.util.Optional;

/**
 * 流程实例历史仓储接口
 * 
 * @author xtt
 */
public interface FlowInstanceHistoryRepository {
    
    /**
     * 根据ID查找流程实例历史
     */
    Optional<FlowInstanceHistory> findById(Long id);
    
    /**
     * 根据流程实例ID查找流程实例历史
     */
    Optional<FlowInstanceHistory> findByFlowInstanceId(Long flowInstanceId);
    
    /**
     * 保存流程实例历史
     */
    FlowInstanceHistory save(FlowInstanceHistory history);
    
    /**
     * 更新流程实例历史
     */
    void update(FlowInstanceHistory history);
    
    /**
     * 删除流程实例历史
     */
    void delete(Long id);
}

