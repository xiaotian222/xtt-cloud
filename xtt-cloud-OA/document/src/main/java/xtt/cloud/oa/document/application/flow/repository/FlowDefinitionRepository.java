package xtt.cloud.oa.document.application.flow.repository;

import org.springframework.stereotype.Repository;
import xtt.cloud.oa.document.domain.entity.flow.definition.FlowDefinition;
import xtt.cloud.oa.document.domain.mapper.flow.FlowDefinitionMapper;

/**
 * 流程定义 Repository
 * 封装 FlowDefinitionMapper 的数据访问操作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Repository
public class FlowDefinitionRepository {
    
    private final FlowDefinitionMapper mapper;
    
    public FlowDefinitionRepository(FlowDefinitionMapper mapper) {
        this.mapper = mapper;
    }
    
    /**
     * 根据ID查询流程定义
     */
    public FlowDefinition findById(Long id) {
        return mapper.selectById(id);
    }
    
    /**
     * 保存流程定义
     */
    public void save(FlowDefinition flowDefinition) {
        if (flowDefinition.getId() == null) {
            mapper.insert(flowDefinition);
        } else {
            mapper.updateById(flowDefinition);
        }
    }
    
    /**
     * 更新流程定义
     */
    public void update(FlowDefinition flowDefinition) {
        mapper.updateById(flowDefinition);
    }
    
    /**
     * 删除流程定义
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

