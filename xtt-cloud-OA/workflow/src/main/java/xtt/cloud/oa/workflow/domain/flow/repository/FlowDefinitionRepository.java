package xtt.cloud.oa.workflow.domain.flow.repository;

import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowDefinition;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowDefinitionId;

import java.util.List;
import java.util.Optional;

/**
 * 流程定义仓储接口
 * 
 * @author xtt
 */
public interface FlowDefinitionRepository {
    
    /**
     * 根据ID查找流程定义（读操作，不加载节点）
     */
    Optional<FlowDefinition> findById(FlowDefinitionId id);
    
    /**
     * 根据编码查找流程定义
     */
    Optional<FlowDefinition> findByCode(String code);
    
    /**
     * 根据文档类型ID查找启用的流程定义
     */
    List<FlowDefinition> findByDocTypeId(Long docTypeId);
    
    /**
     * 查找所有启用的流程定义
     */
    List<FlowDefinition> findAllEnabled();
    
    /**
     * 保存流程定义（写操作）
     * 同时处理节点的保存、更新、删除（通过变更追踪）
     */
    FlowDefinition save(FlowDefinition flowDefinition);
    
    /**
     * 删除流程定义
     * 注意：应该级联删除所有节点
     */
    void delete(FlowDefinitionId id);
}

