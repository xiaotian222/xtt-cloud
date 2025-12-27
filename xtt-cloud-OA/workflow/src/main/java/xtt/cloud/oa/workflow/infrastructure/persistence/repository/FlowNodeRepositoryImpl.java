package xtt.cloud.oa.workflow.infrastructure.persistence.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeRepository;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.FlowNodeMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 流程节点仓储实现
 * 
 * @author xtt
 */
@Repository
public class FlowNodeRepositoryImpl implements FlowNodeRepository {
    
    private static final Logger log = LoggerFactory.getLogger(FlowNodeRepositoryImpl.class);
    
    private final FlowNodeMapper flowNodeMapper;

    public FlowNodeRepositoryImpl(FlowNodeMapper flowNodeMapper) {
        this.flowNodeMapper = flowNodeMapper;
    }


    @Override
    public Optional<FlowNode> findById(xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowNodeId id) {
        if (id == null) {
            return Optional.empty();
        }
        
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode po =
                flowNodeMapper.selectById(id.getValue());
        if (po == null) {
            return Optional.empty();
        }
        
        return Optional.of(new FlowNode(po));
    }
    
    @Override
    public List<FlowNode> findByFlowDefId(xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowDefinitionId flowDefId) {
        if (flowDefId == null) {
            return List.of();
        }
        
        List<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode> pos =
                flowNodeMapper.findByFlowDefId(flowDefId.getValue());
        if (pos == null || pos.isEmpty()) {
            return List.of();
        }
        
        return pos.stream()
                .map(FlowNode::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<FlowNode> findByFlowDefIdAndOrderNum(xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowDefinitionId flowDefId, Integer orderNum) {
        if (flowDefId == null || orderNum == null) {
            return Optional.empty();
        }
        
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode po =
                flowNodeMapper.findByFlowDefIdAndOrderNum(flowDefId.getValue(), orderNum);
        if (po == null) {
            return Optional.empty();
        }
        
        return Optional.of(new FlowNode(po));
    }
    
    @Override
    public Optional<FlowNode> findFirstNodeByFlowDefId(xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowDefinitionId flowDefId) {
        if (flowDefId == null) {
            return Optional.empty();
        }
        
        List<FlowNode> nodes = findByFlowDefId(flowDefId);
        if (nodes.isEmpty()) {
            return Optional.empty();
        }
        
        // 按 orderNum 排序，取第一个
        return nodes.stream()
                .sorted((a, b) -> {
                    Integer orderA = a.getOrderNum() != null ? a.getOrderNum() : Integer.MAX_VALUE;
                    Integer orderB = b.getOrderNum() != null ? b.getOrderNum() : Integer.MAX_VALUE;
                    return orderA.compareTo(orderB);
                })
                .findFirst();
    }
    
    @Override
    public FlowNode save(FlowNode node) {
        if (node == null || node.getPO() == null) {
            throw new IllegalArgumentException("Flow node cannot be null");
        }

        flowNodeMapper.insertOrUpdate(node.getPO());
        log.debug("保存流程节点成功，ID: {}", node.getId());
        return node;
    }
    
    @Override
    public void update(FlowNode node) {
        if (node == null || node.getPO() == null) {
            throw new IllegalArgumentException("Flow node cannot be null");
        }

        flowNodeMapper.updateById(node.getPO());
        log.debug("更新流程节点成功，ID: {}", node.getId());
    }
    
    @Override
    public void delete(xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowNodeId id) {
        if (id == null) {
            return;
        }
        flowNodeMapper.deleteById(id.getValue());
        log.debug("删除流程节点成功，ID: {}", id.getValue());
    }
    
    @Override
    public void deleteByFlowDefId(xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowDefinitionId flowDefId) {
        if (flowDefId == null) {
            return;
        }
        flowNodeMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode>()
                        .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode::getFlowDefId, flowDefId.getValue()));
        log.debug("删除流程定义的所有节点成功，流程定义ID: {}", flowDefId.getValue());
    }
}

