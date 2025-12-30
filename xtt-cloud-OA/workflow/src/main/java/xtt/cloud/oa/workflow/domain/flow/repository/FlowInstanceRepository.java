package xtt.cloud.oa.workflow.domain.flow.repository;

import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowInstanceId;

import java.util.List;
import java.util.Optional;

/**
 * 流程实例仓储接口
 * 
 * 定义在领域层，实现在基础设施层
 * 
 * @author xtt
 */
public interface FlowInstanceRepository {
    
    /**
     * 保存流程实例
     */
    FlowInstance save(FlowInstance flowInstance);
    
    /**
     * 根据ID查找流程实例
     */
    Optional<FlowInstance> findById(FlowInstanceId id);
    
    /**
     * 根据ID查找流程实例（Long类型，用于兼容）
     */
    Optional<FlowInstance> findById(Long id);
    
    /**
     * 根据文档ID查找流程实例
     */
    Optional<FlowInstance> findByDocumentId(Long documentId);
    
    /**
     * 查找指定状态的所有流程实例
     */
    List<FlowInstance> findByStatus(Integer status);
    
    /**
     * 删除流程实例
     */
    void delete(FlowInstance flowInstance);
    
    /**
     * 判断流程实例是否存在
     */
    boolean existsById(FlowInstanceId id);

    List<FlowNodeInstance> findAllByFlowInstanceId(FlowInstanceId id);
}

