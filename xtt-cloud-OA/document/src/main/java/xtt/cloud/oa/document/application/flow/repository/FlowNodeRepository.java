package xtt.cloud.oa.document.application.flow.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;
import xtt.cloud.oa.document.domain.entity.flow.definition.FlowNode;
import xtt.cloud.oa.document.domain.mapper.flow.FlowNodeMapper;

import java.util.List;

/**
 * 流程节点 Repository
 * 封装 FlowNodeMapper 的数据访问操作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Repository
public class FlowNodeRepository {
    
    private final FlowNodeMapper mapper;
    
    public FlowNodeRepository(FlowNodeMapper mapper) {
        this.mapper = mapper;
    }
    
    /**
     * 根据ID查询节点
     */
    public FlowNode findById(Long id) {
        return mapper.selectById(id);
    }
    
    /**
     * 根据流程定义ID查询节点列表
     */
    public List<FlowNode> findByFlowDefId(Long flowDefId) {
        return mapper.selectList(
            new LambdaQueryWrapper<FlowNode>()
                .eq(FlowNode::getFlowDefId, flowDefId)
                .orderByAsc(FlowNode::getOrderNum)
        );
    }
    
    /**
     * 根据流程定义ID和顺序号查询节点
     */
    public FlowNode findByFlowDefIdAndOrderNum(Long flowDefId, Integer orderNum) {
        return mapper.selectOne(
            new LambdaQueryWrapper<FlowNode>()
                .eq(FlowNode::getFlowDefId, flowDefId)
                .eq(FlowNode::getOrderNum, orderNum)
                .orderByAsc(FlowNode::getOrderNum)
                .last("LIMIT 1")
        );
    }
    
    /**
     * 保存节点
     */
    public void save(FlowNode node) {
        if (node.getId() == null) {
            mapper.insert(node);
        } else {
            mapper.updateById(node);
        }
    }
    
    /**
     * 更新节点
     */
    public void update(FlowNode node) {
        mapper.updateById(node);
    }
    
    /**
     * 删除节点
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

