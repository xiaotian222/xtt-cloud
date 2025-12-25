package xtt.cloud.oa.document.application.flow.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;
import xtt.cloud.oa.document.domain.entity.flow.FlowInstance;
import xtt.cloud.oa.document.domain.mapper.flow.FlowInstanceMapper;

import java.util.List;

/**
 * 流程实例 Repository
 * 封装 FlowInstanceMapper 的数据访问操作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Repository
public class FlowInstanceRepository {
    
    private final FlowInstanceMapper mapper;
    
    public FlowInstanceRepository(FlowInstanceMapper mapper) {
        this.mapper = mapper;
    }
    
    /**
     * 根据ID查询流程实例
     */
    public FlowInstance findById(Long id) {
        return mapper.selectById(id);
    }
    
    /**
     * 根据文档ID查询流程实例列表
     */
    public List<FlowInstance> findByDocumentId(Long documentId) {
        return mapper.selectList(
            new LambdaQueryWrapper<FlowInstance>()
                .eq(FlowInstance::getDocumentId, documentId)
        );
    }
    
    /**
     * 根据流程定义ID查询流程实例列表
     */
    public List<FlowInstance> findByFlowDefId(Long flowDefId) {
        return mapper.selectList(
            new LambdaQueryWrapper<FlowInstance>()
                .eq(FlowInstance::getFlowDefId, flowDefId)
        );
    }
    
    /**
     * 根据父流程实例ID查询子流程实例列表
     */
    public List<FlowInstance> findByParentFlowInstanceId(Long parentFlowInstanceId) {
        return mapper.selectList(
            new LambdaQueryWrapper<FlowInstance>()
                .eq(FlowInstance::getParentFlowInstanceId, parentFlowInstanceId)
        );
    }
    
    /**
     * 保存流程实例
     */
    public void save(FlowInstance instance) {
        if (instance.getId() == null) {
            mapper.insert(instance);
        } else {
            mapper.updateById(instance);
        }
    }
    
    /**
     * 更新流程实例
     */
    public void update(FlowInstance instance) {
        mapper.updateById(instance);
    }
    
    /**
     * 删除流程实例
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

