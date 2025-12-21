package xtt.cloud.oa.workflow.domain.flow.repository;

import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;

import java.util.List;
import java.util.Optional;

/**
 * 节点实例仓储接口
 * 
 * @author xtt
 */
public interface FlowNodeInstanceRepository {
    
    /**
     * 保存节点实例
     */
    FlowNodeInstance save(FlowNodeInstance nodeInstance);
    
    /**
     * 批量保存节点实例
     */
    List<FlowNodeInstance> saveAll(List<FlowNodeInstance> nodeInstances);
    
    /**
     * 根据ID查找节点实例
     */
    Optional<FlowNodeInstance> findById(Long id);
    
    /**
     * 根据流程实例ID查找所有节点实例
     */
    List<FlowNodeInstance> findByFlowInstanceId(Long flowInstanceId);
    
    /**
     * 根据节点ID和流程实例ID查找节点实例
     */
    List<FlowNodeInstance> findByNodeIdAndFlowInstanceId(Long nodeId, Long flowInstanceId);
    
    /**
     * 根据审批人ID查找待处理的节点实例
     */
    List<FlowNodeInstance> findPendingByApproverId(Long approverId);
    
    /**
     * 删除节点实例
     */
    void delete(FlowNodeInstance nodeInstance);
    
    /**
     * 删除流程实例的所有节点实例
     */
    void deleteByFlowInstanceId(Long flowInstanceId);
}

