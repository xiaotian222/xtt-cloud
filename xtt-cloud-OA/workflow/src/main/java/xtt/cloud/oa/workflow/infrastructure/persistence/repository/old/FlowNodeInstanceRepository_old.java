package xtt.cloud.oa.workflow.infrastructure.persistence.repository.old;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.FlowNodeInstanceMapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNodeInstance;

import java.util.List;

/**
 * 节点实例 Repository
 * 封装 FlowNodeInstanceMapper 的数据访问操作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Repository
public class FlowNodeInstanceRepository_old {
    
    private final FlowNodeInstanceMapper mapper;

    public FlowNodeInstanceRepository_old(FlowNodeInstanceMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 根据ID查询节点实例
     */
    public FlowNodeInstance findById(Long id) {
        return mapper.selectById(id);
    }
    
    /**
     * 根据流程实例ID查询节点实例列表
     */
    public List<FlowNodeInstance> findByFlowInstanceId(Long flowInstanceId) {
        return mapper.selectList(
            new LambdaQueryWrapper<FlowNodeInstance>()
                .eq(FlowNodeInstance::getFlowInstanceId, flowInstanceId)
        );
    }
    
    /**
     * 根据节点ID查询节点实例列表
     */
    public List<FlowNodeInstance> findByNodeId(Long nodeId) {
        return mapper.selectList(
            new LambdaQueryWrapper<FlowNodeInstance>()
                .eq(FlowNodeInstance::getNodeId, nodeId)
        );
    }
    
    /**
     * 根据流程实例ID和节点ID查询节点实例列表
     */
    public List<FlowNodeInstance> findByFlowInstanceIdAndNodeId(Long flowInstanceId, Long nodeId) {
        return mapper.selectList(
            new LambdaQueryWrapper<FlowNodeInstance>()
                .eq(FlowNodeInstance::getFlowInstanceId, flowInstanceId)
                .eq(FlowNodeInstance::getNodeId, nodeId)
        );
    }
    
    /**
     * 根据审批人ID查询节点实例列表
     */
    public List<FlowNodeInstance> findByApproverId(Long approverId) {
        return mapper.selectList(
            new LambdaQueryWrapper<FlowNodeInstance>()
                .eq(FlowNodeInstance::getApproverId, approverId)
        );
    }
    
    /**
     * 保存节点实例
     */
    public void save(FlowNodeInstance nodeInstance) {
        if (nodeInstance.getId() == null) {
            mapper.insert(nodeInstance);
        } else {
            mapper.updateById(nodeInstance);
        }
    }
    
    /**
     * 更新节点实例
     */
    public void update(FlowNodeInstance nodeInstance) {
        mapper.updateById(nodeInstance);
    }
    
    /**
     * 删除节点实例
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

