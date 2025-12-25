package xtt.cloud.oa.workflow.domain.flow.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.NodeStatus;
import xtt.cloud.oa.workflow.domain.flow.service.ConditionEvaluationService;
import xtt.cloud.oa.workflow.domain.flow.service.NodeRoutingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 节点路由服务实现
 * 
 * @author xtt
 */
@Service
public class NodeRoutingServiceImpl implements NodeRoutingService {
    
    private static final Logger log = LoggerFactory.getLogger(NodeRoutingServiceImpl.class);
    
    private final FlowNodeInstanceRepository flowNodeInstanceRepository;
    private final ConditionEvaluationService conditionEvaluationService;
    private final ObjectMapper objectMapper;
    private final FlowNodeRepository flowNodeRepository;
    
    public NodeRoutingServiceImpl(
            FlowNodeInstanceRepository flowNodeInstanceRepository,
            ConditionEvaluationService conditionEvaluationService,
            ObjectMapper objectMapper,
            FlowNodeRepository flowNodeRepository) {
        this.flowNodeInstanceRepository = flowNodeInstanceRepository;
        this.conditionEvaluationService = conditionEvaluationService;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.flowNodeRepository = flowNodeRepository;
    }
    
    @Override
    public List<Long> getNextNodeIds(Long currentNodeId, Long flowDefId, Map<String, Object> processVariables) {
        // 从 FlowNodeRepository 获取当前节点定义
        java.util.Optional<xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode> currentNodeOpt = 
                flowNodeRepository.findById(currentNodeId);
        if (currentNodeOpt.isEmpty()) {
            log.warn("节点不存在，节点ID: {}", currentNodeId);
            return List.of();
        }
        
        xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode currentNode = currentNodeOpt.get();
        List<Long> nextNodeIds = new ArrayList<>();
        
        // 1. 优先使用 nextNodeIds（JSON格式，多个下一个节点，用于并行分支和条件流转）
        if (StringUtils.hasText(currentNode.getNextNodeIds())) {
            try {
                List<Long> nextNodeIdList = objectMapper.readValue(
                        currentNode.getNextNodeIds(),
                        new TypeReference<List<Long>>() {}
                );
                if (nextNodeIdList != null && !nextNodeIdList.isEmpty()) {
                    // 如果是条件节点，需要评估条件表达式
                    if (currentNode.isConditionNode()) {
                        return evaluateConditionalNodes(currentNode, nextNodeIdList, processVariables);
                    }
                    return nextNodeIdList;
                }
            } catch (Exception e) {
                log.error("解析 nextNodeIds 失败，节点ID: {}, nextNodeIds: {}", currentNodeId, currentNode.getNextNodeIds(), e);
            }
        }
        
        // 2. 使用 nextNodeId（单个下一个节点，用于串行流程）
        if (currentNode.getNextNodeId() != null) {
            nextNodeIds.add(currentNode.getNextNodeId());
            return nextNodeIds;
        }
        
        // 3. 兼容旧数据：使用 orderNum + 1
        List<xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode> allNodes = 
                flowNodeRepository.findByFlowDefId(flowDefId);
        java.util.Optional<xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode> nextNodeOpt = 
                allNodes.stream()
                        .filter(n -> n.getOrderNum() != null && 
                                     currentNode.getOrderNum() != null &&
                                     n.getOrderNum().equals(currentNode.getOrderNum() + 1))
                        .findFirst();
        if (nextNodeOpt.isPresent()) {
            nextNodeIds.add(nextNodeOpt.get().getId());
        }
        
        return nextNodeIds;
    }
    
    /**
     * 评估条件节点的下一个节点
     */
    private List<Long> evaluateConditionalNodes(
            xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode currentNode,
            List<Long> candidateNodeIds,
            Map<String, Object> processVariables) {
        
        List<Long> result = new ArrayList<>();
        
        // 遍历候选节点，评估每个节点的条件表达式
        for (Long nodeId : candidateNodeIds) {
            java.util.Optional<xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode> candidateNodeOpt = 
                    flowNodeRepository.findById(nodeId);
            if (candidateNodeOpt.isEmpty()) {
                continue;
            }
            
            xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode candidateNode = candidateNodeOpt.get();
            if (StringUtils.hasText(candidateNode.getSkipCondition())) {
                // skipCondition 作为条件表达式
                if (conditionEvaluationService.evaluate(candidateNode.getSkipCondition(), processVariables)) {
                    result.add(nodeId);
                }
            } else {
                // 没有条件表达式，默认通过
                result.add(nodeId);
            }
        }
        
        return result;
    }
    
    @Override
    public boolean isConvergenceNode(Long nodeId, Long flowDefId) {
        // 查询所有节点，检查是否有多个节点指向当前节点
        List<xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode> allNodes = 
                flowNodeRepository.findByFlowDefId(flowDefId);
        
        // 检查是否有多个节点的 nextNodeId 或 nextNodeIds 包含当前节点
        long count = allNodes.stream()
                .filter(n -> {
                    if (n.getNextNodeId() != null && n.getNextNodeId().equals(nodeId)) {
                        return true;
                    }
                    if (StringUtils.hasText(n.getNextNodeIds())) {
                        try {
                            List<Long> nextNodeIds = objectMapper.readValue(
                                    n.getNextNodeIds(),
                                    new TypeReference<List<Long>>() {}
                            );
                            return nextNodeIds != null && nextNodeIds.contains(nodeId);
                        } catch (Exception e) {
                            log.error("解析 nextNodeIds 失败", e);
                        }
                    }
                    return false;
                })
                .count();
        
        return count > 1;
    }
    
    @Override
    public boolean canConverge(Long convergenceNodeId, Long flowInstanceId) {
        // TODO: 查找所有指向汇聚节点的前驱节点
        // List<FlowNode> predecessorNodes = findPredecessorNodes(convergenceNodeId, flowDefId);
        // 
        // // 检查所有前驱节点的实例是否都已完成
        // List<FlowNodeInstance> nodeInstances = flowNodeInstanceRepository.findByFlowInstanceId(flowInstanceId);
        // 
        // for (FlowNode predecessorNode : predecessorNodes) {
        //     boolean allCompleted = nodeInstances.stream()
        //             .filter(ni -> ni.getNodeId().equals(predecessorNode.getId()))
        //             .allMatch(ni -> {
        //                 NodeStatus status = NodeStatus.fromValue(ni.getStatus());
        //                 return status.isFinished();
        //             });
        //     
        //     if (!allCompleted) {
        //         return false;
        //     }
        // }
        
        return true;
    }
    
    @Override
    public boolean shouldSkipNode(Long nodeId, Long flowDefId, Map<String, Object> processVariables) {
        // 从 FlowNodeRepository 获取节点定义
        java.util.Optional<xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode> nodeOpt = 
                flowNodeRepository.findById(nodeId);
        if (nodeOpt.isEmpty()) {
            return false;
        }
        
        xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode node = nodeOpt.get();
        
        // 如果节点有跳过条件，评估条件表达式
        if (StringUtils.hasText(node.getSkipCondition())) {
            return conditionEvaluationService.evaluate(node.getSkipCondition(), processVariables);
        }
        
        return false;
    }
    
    @Override
    public boolean canMoveToNextNode(xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode currentNodeDef, Long flowInstanceId) {
        // 如果是并行节点，需要检查并行模式
        if (currentNodeDef.isParallelMode()) {
            if (currentNodeDef.isParallelAllMode()) {
                // 会签模式：所有节点都完成
                return allParallelNodesCompleted(currentNodeDef, flowInstanceId);
            } else if (currentNodeDef.isParallelAnyMode()) {
                // 或签模式：任一节点完成
                return anyParallelNodeCompleted(currentNodeDef, flowInstanceId);
            }
        }
        
        // 串行节点：当前节点完成即可流转
        return true;
    }
    
    @Override
    public boolean allParallelNodesCompleted(xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode node, Long flowInstanceId) {
        List<xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance> nodeInstances = 
                flowNodeInstanceRepository.findByNodeIdAndFlowInstanceId(node.getId(), flowInstanceId);
        
        if (nodeInstances.isEmpty()) {
            return false;
        }
        
        return nodeInstances.stream()
                .allMatch(ni -> {
                    NodeStatus status = ni.getStatus();
                    return status != null && status.isFinished();
                });
    }
    
    @Override
    public boolean anyParallelNodeCompleted(xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode node, Long flowInstanceId) {
        List<xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance> nodeInstances = 
                flowNodeInstanceRepository.findByNodeIdAndFlowInstanceId(node.getId(), flowInstanceId);
        
        return nodeInstances.stream()
                .anyMatch(ni -> {
                    NodeStatus status = ni.getStatus();
                    return status != null && status.isFinished();
                });
    }
    
    @Override
    public boolean canCompleteFlow(Long currentNodeId, Long flowDefId, Long flowInstanceId, 
                                   Map<String, Object> processVariables) {
        if (currentNodeId == null) {
            // 没有当前节点，可以直接完成
            return true;
        }
        
        java.util.Optional<xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode> currentNodeOpt = 
                flowNodeRepository.findById(currentNodeId);
        if (currentNodeOpt.isPresent()) {
            xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode currentNode = currentNodeOpt.get();
            
            // 检查最后一个节点的所有实例是否完成
            if (currentNode.isParallelAllMode()) {
                return allParallelNodesCompleted(currentNode, flowInstanceId);
            } else {
                // 串行或或签模式，任一完成即可
                List<xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance> nodeInstances = 
                        flowNodeInstanceRepository.findByNodeIdAndFlowInstanceId(currentNode.getId(), flowInstanceId);
                if (!nodeInstances.isEmpty()) {
                    return nodeInstances.stream()
                            .anyMatch(ni -> {
                                NodeStatus status = ni.getStatus();
                                return status != null && status.isFinished();
                            });
                }
            }
        } else {
            // 检查是否没有下一个节点
            List<Long> nextNodeIds = getNextNodeIds(currentNodeId, flowDefId, processVariables);
            return nextNodeIds.isEmpty();
        }
        
        return false;
    }
}

