package xtt.cloud.oa.workflow.domain.flow.repository;

import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowDefinitionId;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowNodeId;

import java.util.List;
import java.util.Optional;

/**
 * 流程节点仓储接口
 * 
 * 注意：节点的保存、更新、删除通常由 FlowDefinition 聚合根调用
 * 
 * @author xtt
 */
public interface FlowNodeRepository {
    
    /**
     * 根据ID查找节点
     */
    Optional<FlowNode> findById(FlowNodeId id);
    
    /**
     * 根据流程定义ID查找节点列表（读操作）
     */
    List<FlowNode> findByFlowDefId(FlowDefinitionId flowDefId);
    
    /**
     * 根据流程定义ID和顺序号查找节点
     */
    Optional<FlowNode> findByFlowDefIdAndOrderNum(FlowDefinitionId flowDefId, Integer orderNum);
    
    /**
     * 根据流程定义ID查找第一个节点
     */
    Optional<FlowNode> findFirstNodeByFlowDefId(FlowDefinitionId flowDefId);
    
    /**
     * 保存节点（由 FlowDefinition 聚合根调用）
     */
    FlowNode save(FlowNode node);
    
    /**
     * 更新节点（由 FlowDefinition 聚合根调用）
     */
    void update(FlowNode node);
    
    /**
     * 删除节点（由 FlowDefinition 聚合根调用）
     */
    void delete(FlowNodeId id);
    
    /**
     * 根据流程定义ID删除所有节点（级联删除）
     */
    void deleteByFlowDefId(FlowDefinitionId flowDefId);
}

