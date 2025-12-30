package xtt.cloud.oa.workflow.domain.flow.service.strategy.gateway.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowNodeId;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.GatewayType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.NodeStatus;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeInstanceRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeRepository;
import xtt.cloud.oa.workflow.domain.flow.service.ConditionEvaluationService;
import xtt.cloud.oa.workflow.domain.flow.service.strategy.gateway.GatewayRoutingStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 条件网关路由策略
 * 
 * 处理条件网关（CONDITION_SPLIT、CONDITION_JOIN）的路由逻辑
 * 
 * @author xtt
 */
@Component
public class ConditionGatewayStrategy implements GatewayRoutingStrategy {
    
    private static final Logger log = LoggerFactory.getLogger(ConditionGatewayStrategy.class);
    
    private final FlowNodeRepository flowNodeRepository;
    private final FlowNodeInstanceRepository flowNodeInstanceRepository;
    private final ConditionEvaluationService conditionEvaluationService;
    
    public ConditionGatewayStrategy(
            FlowNodeRepository flowNodeRepository,
            FlowNodeInstanceRepository flowNodeInstanceRepository,
            ConditionEvaluationService conditionEvaluationService) {
        this.flowNodeRepository = flowNodeRepository;
        this.flowNodeInstanceRepository = flowNodeInstanceRepository;
        this.conditionEvaluationService = conditionEvaluationService;
    }
    
    @Override
    public List<Long> getNextNodes(Long gatewayNodeId, Long flowInstanceId, 
                                   Map<String, Object> processVariables) {
        // 条件网关 Split：评估条件表达式，返回满足条件的分支
        Optional<FlowNode> gatewayNodeOpt = flowNodeRepository.findById(FlowNodeId.of(gatewayNodeId));
        if (gatewayNodeOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        FlowNode gatewayNode = gatewayNodeOpt.get();
        if (gatewayNode.getGatewayType() != GatewayType.CONDITION_SPLIT) {
            return new ArrayList<>();
        }
        
        // 获取下一个节点列表
        // TODO: 从节点的 nextNodeIds 获取候选分支节点
        
        // 评估每个分支的条件表达式
        List<Long> result = new ArrayList<>();
        // TODO: 实现条件评估逻辑
        
        return result;
    }
    
    @Override
    public boolean canConverge(Long joinNodeId, Long flowInstanceId) {
        Optional<FlowNode> joinNodeOpt = flowNodeRepository.findById(FlowNodeId.of(joinNodeId));
        if (joinNodeOpt.isEmpty()) {
            return false;
        }
        
        FlowNode joinNode = joinNodeOpt.get();
        if (joinNode.getGatewayType() != GatewayType.CONDITION_JOIN) {
            return false;
        }
        
        // 查找所有指向Join节点的前驱节点
        List<FlowNode> predecessorNodes = findPredecessorNodes(joinNodeId, joinNode.getFlowDefId());
        
        if (predecessorNodes.isEmpty()) {
            return false;
        }
        
        // 条件网关Join：至少有一个分支完成即可汇聚
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
    
    @Override
    public boolean supports(GatewayType gatewayType) {
        return gatewayType == GatewayType.CONDITION_SPLIT 
            || gatewayType == GatewayType.CONDITION_JOIN;
    }
    
    /**
     * 查找指向指定节点的前驱节点列表
     */
    private List<FlowNode> findPredecessorNodes(Long targetNodeId, Long flowDefId) {
        // TODO: 实现查找前驱节点的逻辑
        return new ArrayList<>();
    }
}

