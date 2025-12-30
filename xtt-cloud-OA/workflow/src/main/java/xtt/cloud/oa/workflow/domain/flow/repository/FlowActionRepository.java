package xtt.cloud.oa.workflow.domain.flow.repository;

import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowAction;

import java.util.List;
import java.util.Optional;

/**
 * 流程动作仓储接口
 * 
 * @author xtt
 */
public interface FlowActionRepository {
    
    /**
     * 根据ID查找动作
     * 
     * @param actionId 动作ID
     * @return 动作
     */
    Optional<FlowAction> findById(Long actionId);
    
    /**
     * 查找所有启用的动作
     * 
     * @return 动作列表
     */
    List<FlowAction> findAllEnabled();
    
    /**
     * 根据动作ID列表查找动作
     * 
     * @param actionIds 动作ID列表
     * @return 动作列表
     */
    List<FlowAction> findByIds(List<Long> actionIds);
}

