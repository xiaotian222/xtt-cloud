package xtt.cloud.oa.workflow.domain.flow.repository;

import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode;

import java.util.List;
import java.util.Optional;

/**
 * 流程节点仓储接口
 * 
 * @author xtt
 */
public interface FlowNodeRepository {
    
    /**
     * 根据ID查找节点
     */
    Optional<FlowNode> findById(Long id);
    
    /**
     * 根据流程定义ID查找节点列表
     */
    List<FlowNode> findByFlowDefId(Long flowDefId);
    
    /**
     * 根据流程定义ID和顺序号查找节点
     */
    Optional<FlowNode> findByFlowDefIdAndOrderNum(Long flowDefId, Integer orderNum);
    
    /**
     * 根据流程定义ID查找第一个节点
     */
    Optional<FlowNode> findFirstNodeByFlowDefId(Long flowDefId);
    
    /**
     * 保存节点
     */
    void save(FlowNode node);
    
    /**
     * 更新节点
     */
    void update(FlowNode node);
    
    /**
     * 删除节点
     */
    void delete(Long id);
}

