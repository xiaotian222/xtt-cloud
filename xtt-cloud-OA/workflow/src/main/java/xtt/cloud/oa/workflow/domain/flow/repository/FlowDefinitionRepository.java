package xtt.cloud.oa.workflow.domain.flow.repository;

import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowDefinition;

import java.util.List;
import java.util.Optional;

/**
 * 流程定义仓储接口
 * 
 * @author xtt
 */
public interface FlowDefinitionRepository {
    
    /**
     * 根据ID查找流程定义
     */
    Optional<FlowDefinition> findById(Long id);
    
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
     * 保存流程定义
     */
    void save(FlowDefinition flowDefinition);
    
    /**
     * 更新流程定义
     */
    void update(FlowDefinition flowDefinition);
    
    /**
     * 删除流程定义
     */
    void delete(Long id);
}

