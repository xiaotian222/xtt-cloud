package xtt.cloud.oa.workflow.domain.flow.service.strategy.gateway.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowNodeId;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.GatewayMode;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.GatewayType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.NodeStatus;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeInstanceRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeRepository;
import xtt.cloud.oa.workflow.domain.flow.service.strategy.gateway.GatewayRoutingStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 并行网关路由策略
 * 
 * 处理并行网关（PARALLEL_SPLIT、PARALLEL_JOIN）的路由逻辑
 * 
 * @author xtt
 */
@Component
public class ParallelGatewayStrategy implements GatewayRoutingStrategy {
    
    private static final Logger log = LoggerFactory.getLogger(ParallelGatewayStrategy.class);
    
    private final FlowNodeRepository flowNodeRepository;
    private final FlowNodeInstanceRepository flowNodeInstanceRepository;
    
    public ParallelGatewayStrategy(
            FlowNodeRepository flowNodeRepository,
            FlowNodeInstanceRepository flowNodeInstanceRepository) {
        this.flowNodeRepository = flowNodeRepository;
        this.flowNodeInstanceRepository = flowNodeInstanceRepository;
    }
    
    @Override
    public List<Long> getNextNodes(Long gatewayNodeId, Long flowInstanceId, 
                                   Map<String, Object> processVariables) {
        // 并行网关 Split：获取所有分支节点
        Optional<FlowNode> gatewayNodeOpt = flowNodeRepository.findById(FlowNodeId.of(gatewayNodeId));
        if (gatewayNodeOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        FlowNode gatewayNode = gatewayNodeOpt.get();
        
        // 获取下一个节点列表（所有分支）
        // 这里需要从节点的 nextNodeIds 获取
        // TODO: 实现获取所有分支节点的逻辑
        
        return new ArrayList<>();
    }
    
    @Override
    public boolean canConverge(Long joinNodeId, Long flowInstanceId) {
        Optional<FlowNode> joinNodeOpt = flowNodeRepository.findById(FlowNodeId.of(joinNodeId));
        if (joinNodeOpt.isEmpty()) {
            return false;
        }
        
        FlowNode joinNode = joinNodeOpt.get();
        if (joinNode.getGatewayType() != GatewayType.PARALLEL_JOIN) {
            return false;
        }
        
        // 获取网关模式
        GatewayMode gatewayMode = joinNode.getGatewayMode();
        if (gatewayMode == null) {
            gatewayMode = GatewayMode.PARALLEL_ALL; // 默认会签模式
        }
        
        // 查找所有指向Join节点的前驱节点
        List<FlowNode> predecessorNodes = findPredecessorNodes(joinNodeId, joinNode.getFlowDefId());
        
        if (predecessorNodes.isEmpty()) {
            return false;
        }
        
        if (gatewayMode.isAll()) {
            // 会签模式：所有分支都完成
            return predecessorNodes.stream().allMatch(node -> {
                List<FlowNodeInstance> nodeInstances = 
                        flowNodeInstanceRepository.findByNodeIdAndFlowInstanceId(
                                node.getId(), flowInstanceId);
                return !nodeInstances.isEmpty() && nodeInstances.stream()
                        .allMatch(ni -> {
                            NodeStatus status = ni.getStatus();
                            return status != null && status.isFinished();
                        });
            });
        } else {
            // 或签模式：任一分支完成
            return predecessorNodes.stream().anyMatch(node -> {
                List<FlowNodeInstance> nodeInstances = 
                        flowNodeInstanceRepository.findByNodeIdAndFlowInstanceId(
                                node.getId(), flowInstanceId);
                return !nodeInstances.isEmpty() && nodeInstances.stream()
                        .anyMatch(ni -> {
                            NodeStatus status = ni.getStatus();
                            return status != null && status.isFinished();
                        });
            });
        }
    }
    
    @Override
    public boolean supports(GatewayType gatewayType) {
        return gatewayType == GatewayType.PARALLEL_SPLIT 
            || gatewayType == GatewayType.PARALLEL_JOIN;
    }
    
    /**
     * 查找指向指定节点的前驱节点列表
     */
    private List<FlowNode> findPredecessorNodes(Long targetNodeId, Long flowDefId) {
        // TODO: 实现查找前驱节点的逻辑
        return new ArrayList<>();
    }
}

