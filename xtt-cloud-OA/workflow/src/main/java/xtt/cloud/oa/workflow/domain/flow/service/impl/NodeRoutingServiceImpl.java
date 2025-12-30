package xtt.cloud.oa.workflow.domain.flow.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowDefinition;
import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowDefinitionId;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowNodeId;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.GatewayMode;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.GatewayType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.NodeStatus;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeInstanceRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeRepository;
import xtt.cloud.oa.workflow.domain.flow.service.ApproverAssignmentService;
import xtt.cloud.oa.workflow.domain.flow.service.ConditionEvaluationService;
import xtt.cloud.oa.workflow.domain.flow.service.NodeRoutingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 节点路由领域服务实现
 * 
 * 负责计算流程的下一个节点，处理串行、并行、条件流转等逻辑
 * 支持网关控制（并行网关、条件网关）
 * 
 * @author xtt
 */
@Service
public class NodeRoutingServiceImpl implements NodeRoutingService {

    private static final Logger log = LoggerFactory.getLogger(NodeRoutingServiceImpl.class);

    private final FlowNodeInstanceRepository flowNodeInstanceRepository;
    private final FlowNodeRepository flowNodeRepository;
    private final ConditionEvaluationService conditionEvaluationService;
    private final ApproverAssignmentService approverAssignmentService;
    private final ObjectMapper objectMapper;

    public NodeRoutingServiceImpl(
            FlowNodeInstanceRepository flowNodeInstanceRepository,
            FlowNodeRepository flowNodeRepository,
            ConditionEvaluationService conditionEvaluationService,
            ApproverAssignmentService approverAssignmentService,
            ObjectMapper objectMapper) {
        this.flowNodeInstanceRepository = flowNodeInstanceRepository;
        this.flowNodeRepository = flowNodeRepository;
        this.conditionEvaluationService = conditionEvaluationService;
        this.approverAssignmentService = approverAssignmentService;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Override
    public boolean canMoveToNextNode(FlowInstance flowInstance, FlowDefinition flowDefinition) {
        if (flowInstance == null || flowDefinition == null) {
            return false;
        }

        Long currentNodeId = flowInstance.getCurrentNodeId();
        if (currentNodeId == null) {
            // 没有当前节点，可以流转
            return true;
        }

        Long flowInstanceId = flowInstance.getId() != null ? flowInstance.getId().getValue() : null;
        if (flowInstanceId == null) {
            return false;
        }

        // 获取当前节点定义
        Optional<FlowNode> currentNodeOpt = flowNodeRepository.findById(FlowNodeId.of(currentNodeId));
        if (currentNodeOpt.isEmpty()) {
            log.warn("当前节点不存在，节点ID: {}", currentNodeId);
            return false;
        }

        FlowNode currentNode = currentNodeOpt.get();

        // 1. 如果是网关节点，检查网关类型
        if (currentNode.isGateway()) {
            GatewayType gatewayType = currentNode.getGatewayType();
            
            if (gatewayType == GatewayType.PARALLEL_JOIN) {
                // 并行汇聚网关：检查是否可以汇聚
                return canParallelJoinConverge(currentNodeId, flowInstanceId);
            } else if (gatewayType == GatewayType.CONDITION_JOIN) {
                // 条件汇聚网关：检查是否可以汇聚
                return canConditionJoinConverge(currentNodeId, flowInstanceId);
            } else if (gatewayType == GatewayType.PARALLEL_SPLIT || gatewayType == GatewayType.CONDITION_SPLIT) {
                // 分支网关：直接可以流转
                return true;
            }
        }

        // 2. 普通节点：检查节点实例是否完成
        List<FlowNodeInstance> nodeInstances = flowNodeInstanceRepository.findByNodeIdAndFlowInstanceId(
                currentNodeId, flowInstanceId);
        
        if (nodeInstances.isEmpty()) {
            return false;
        }

        // 检查是否至少有一个节点实例已完成
        return nodeInstances.stream()
                .anyMatch(ni -> {
                    NodeStatus status = ni.getStatus();
                    return status != null && status.isFinished();
                });
    }

    @Override
    public void moveToNextNode(FlowInstance flowInstance, FlowDefinition flowDefinition) {
        if (flowInstance == null || flowDefinition == null) {
            throw new IllegalArgumentException("流程实例和流程定义不能为空");
        }

        Long currentNodeId = flowInstance.getCurrentNodeId();
        Long flowInstanceId = flowInstance.getId() != null ? flowInstance.getId().getValue() : null;
        Long flowDefId = flowInstance.getFlowDefId();

        log.debug("流转到下一个节点，流程实例ID: {}, 当前节点ID: {}", flowInstanceId, currentNodeId);

        if (currentNodeId == null) {
            log.debug("没有当前节点，无法流转");
            return;
        }

        // 1. 获取下一个节点列表
        Map<String, Object> processVariables = flowInstance.getProcessVariables().getAllVariables();
        List<Long> nextNodeIds = getNextNodeIds(currentNodeId, flowDefId, processVariables);

        if (nextNodeIds.isEmpty()) {
            log.debug("没有下一个节点，流程将结束");
            return;
        }

        // 2. 处理每个下一个节点
        List<Long> processedNodeIds = new ArrayList<>();
        for (Long nextNodeId : nextNodeIds) {
            // 2.1 检查是否为汇聚网关节点
            Optional<FlowNode> nextNodeOpt = flowNodeRepository.findById(FlowNodeId.of(nextNodeId));
            if (nextNodeOpt.isEmpty()) {
                log.warn("下一个节点不存在，节点ID: {}", nextNodeId);
                continue;
            }

            FlowNode nextNode = nextNodeOpt.get();

            // 2.2 如果是汇聚网关，检查是否可以汇聚
            if (nextNode.isJoinGateway()) {
                boolean canConverge = false;
                if (nextNode.getGatewayType() == GatewayType.PARALLEL_JOIN) {
                    canConverge = canParallelJoinConverge(nextNodeId, flowInstanceId);
                } else if (nextNode.getGatewayType() == GatewayType.CONDITION_JOIN) {
                    canConverge = canConditionJoinConverge(nextNodeId, flowInstanceId);
                }

                if (!canConverge) {
                    log.debug("汇聚网关节点 {} 尚未满足汇聚条件，等待其他分支完成", nextNodeId);
                    continue;
                }
            }

            // 2.3 检查是否应该跳过节点
            if (shouldSkipNode(nextNodeId, flowDefId, processVariables)) {
                // 创建已跳过的节点实例
                createSkippedNodeInstance(flowInstance, nextNode);
                // 递归处理下一个节点
                flowInstance.moveToNode(nextNodeId);
                moveToNextNode(flowInstance, flowDefinition);
                return;
            }

            // 2.4 创建节点实例并分配审批人
            createAndAssignNodeInstances(flowInstance, nextNode, processVariables);
            processedNodeIds.add(nextNodeId);
        }

        // 3. 更新当前节点（如果有处理成功的节点，更新为第一个）
        if (!processedNodeIds.isEmpty()) {
            flowInstance.moveToNode(processedNodeIds.get(0));
        }
    }

    @Override
    public boolean canCompleteFlow(Long currentNodeId, Long flowDefId, Long flowInstanceId,
                                   Map<String, Object> processVariables) {
        if (currentNodeId == null) {
            // 没有当前节点，可以直接完成
            return true;
        }

        Optional<FlowNode> currentNodeOpt = flowNodeRepository.findById(FlowNodeId.of(currentNodeId));
        if (currentNodeOpt.isEmpty()) {
            // 节点不存在，检查是否没有下一个节点
            List<Long> nextNodeIds = getNextNodeIds(currentNodeId, flowDefId, processVariables);
            return nextNodeIds.isEmpty();
        }

        FlowNode currentNode = currentNodeOpt.get();

        // 1. 如果是网关节点
        if (currentNode.isGateway()) {
            GatewayType gatewayType = currentNode.getGatewayType();
            
            if (gatewayType == GatewayType.PARALLEL_JOIN) {
                // 并行汇聚网关：检查是否可以汇聚
                return canParallelJoinConverge(currentNodeId, flowInstanceId);
            } else if (gatewayType == GatewayType.CONDITION_JOIN) {
                // 条件汇聚网关：检查是否可以汇聚
                return canConditionJoinConverge(currentNodeId, flowInstanceId);
            }
        }

        // 2. 检查节点实例是否完成
        List<FlowNodeInstance> nodeInstances = flowNodeInstanceRepository.findByNodeIdAndFlowInstanceId(
                currentNodeId, flowInstanceId);
        
        if (nodeInstances.isEmpty()) {
            // 没有节点实例，检查是否没有下一个节点
            List<Long> nextNodeIds = getNextNodeIds(currentNodeId, flowDefId, processVariables);
            return nextNodeIds.isEmpty();
        }

        // 检查是否至少有一个节点实例已完成
        boolean hasCompleted = nodeInstances.stream()
                .anyMatch(ni -> {
                    NodeStatus status = ni.getStatus();
                    return status != null && status.isFinished();
                });

        if (!hasCompleted) {
            return false;
        }

        // 3. 检查是否没有下一个节点
        List<Long> nextNodeIds = getNextNodeIds(currentNodeId, flowDefId, processVariables);
        return nextNodeIds.isEmpty();
    }

    @Override
    public void rollbackToNode(FlowInstance flowInstance, Long targetNodeId, Long operatorId, String reason) {
        if (flowInstance == null) {
            throw new IllegalArgumentException("流程实例不能为空");
        }
        if (targetNodeId == null || targetNodeId <= 0) {
            throw new IllegalArgumentException("目标节点ID必须大于0");
        }

        Long flowInstanceId = flowInstance.getId() != null ? flowInstance.getId().getValue() : null;
        log.info("回退到节点，流程实例ID: {}, 目标节点ID: {}, 操作人ID: {}", flowInstanceId, targetNodeId, operatorId);

        // 1. 验证目标节点是否存在
        Optional<FlowNode> targetNodeOpt = flowNodeRepository.findById(FlowNodeId.of(targetNodeId));
        if (targetNodeOpt.isEmpty()) {
            throw new IllegalArgumentException("目标节点不存在: " + targetNodeId);
        }
        FlowNode targetNode = targetNodeOpt.get();

        // 2. 验证目标节点是否属于当前流程定义
        if (!targetNode.getFlowDefId().equals(flowInstance.getFlowDefId())) {
            throw new IllegalArgumentException("目标节点不属于当前流程定义");
        }

        // 3. 取消当前节点及之后的所有待办任务
        // TODO: 实现取消待办任务的逻辑
        // taskService.cancelTasksAfterNode(flowInstanceId, targetNodeId);

        // 4. 更新流程实例当前节点
        flowInstance.moveToNode(targetNodeId);

        // 5. 创建目标节点的节点实例并分配审批人
        Map<String, Object> processVariables = flowInstance.getProcessVariables().getAllVariables();
        createAndAssignNodeInstances(flowInstance, targetNode, processVariables);

        log.info("回退成功，流程实例ID: {}, 目标节点ID: {}", flowInstanceId, targetNodeId);
    }

    /**
     * 判断并行网关的Join节点是否可以汇聚
     */
    public boolean canParallelJoinConverge(Long joinGatewayNodeId, Long flowInstanceId) {
        if (joinGatewayNodeId == null || flowInstanceId == null) {
            return false;
        }

        // 1. 获取Join节点定义
        Optional<FlowNode> joinNodeOpt = flowNodeRepository.findById(FlowNodeId.of(joinGatewayNodeId));
        if (joinNodeOpt.isEmpty()) {
            return false;
        }

        FlowNode joinNode = joinNodeOpt.get();
        if (joinNode.getGatewayType() != GatewayType.PARALLEL_JOIN) {
            return false;
        }

        // 2. 获取对应的Split节点
        Long gatewayId = joinNode.getGatewayId();
        if (gatewayId == null) {
            return false;
        }

        // 3. 查找所有指向Join节点的前驱节点（Split分支后的节点）
        List<FlowNode> predecessorNodes = findPredecessorNodes(joinGatewayNodeId, joinNode.getFlowDefId());

        if (predecessorNodes.isEmpty()) {
            return false;
        }

        // 4. 根据网关模式判断
        GatewayMode gatewayMode = joinNode.getGatewayMode();
        if (gatewayMode == null) {
            gatewayMode = GatewayMode.PARALLEL_ALL; // 默认会签模式
        }

        if (gatewayMode.isAll()) {
            // 会签模式：所有分支都完成
            return predecessorNodes.stream().allMatch(node -> {
                List<FlowNodeInstance> nodeInstances = flowNodeInstanceRepository.findByNodeIdAndFlowInstanceId(
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
                List<FlowNodeInstance> nodeInstances = flowNodeInstanceRepository.findByNodeIdAndFlowInstanceId(
                        node.getId(), flowInstanceId);
                return !nodeInstances.isEmpty() && nodeInstances.stream()
                        .anyMatch(ni -> {
                            NodeStatus status = ni.getStatus();
                            return status != null && status.isFinished();
                        });
            });
        }
    }

    /**
     * 评估条件网关Split节点的分支条件
     */
    public List<Long> evaluateConditionSplit(Long splitGatewayNodeId, Long flowInstanceId, 
                                             Map<String, Object> processVariables) {
        if (splitGatewayNodeId == null || flowInstanceId == null) {
            return new ArrayList<>();
        }

        // 1. 获取Split节点定义
        Optional<FlowNode> splitNodeOpt = flowNodeRepository.findById(FlowNodeId.of(splitGatewayNodeId));
        if (splitNodeOpt.isEmpty()) {
            return new ArrayList<>();
        }

        FlowNode splitNode = splitNodeOpt.get();
        if (splitNode.getGatewayType() != GatewayType.CONDITION_SPLIT) {
            return new ArrayList<>();
        }

        // 2. 获取下一个节点列表
        List<Long> nextNodeIds = getNextNodeIds(splitGatewayNodeId, splitNode.getFlowDefId(), processVariables);

        // 3. 评估每个分支的条件表达式
        List<Long> result = new ArrayList<>();
        for (Long nextNodeId : nextNodeIds) {
            Optional<FlowNode> nextNodeOpt = flowNodeRepository.findById(FlowNodeId.of(nextNodeId));
            if (nextNodeOpt.isEmpty()) {
                continue;
            }

            FlowNode nextNode = nextNodeOpt.get();
            
            // 如果节点有条件表达式，评估条件
            String conditionExpression = nextNode.getConditionExpression();
            if (StringUtils.hasText(conditionExpression)) {
                if (conditionEvaluationService.evaluate(conditionExpression, processVariables)) {
                    result.add(nextNodeId);
                }
            } else {
                // 没有条件表达式，默认通过
                result.add(nextNodeId);
            }
        }

        return result;
    }

    /**
     * 判断条件网关的Join节点是否可以汇聚
     */
    public boolean canConditionJoinConverge(Long joinGatewayNodeId, Long flowInstanceId) {
        if (joinGatewayNodeId == null || flowInstanceId == null) {
            return false;
        }

        // 1. 获取Join节点定义
        Optional<FlowNode> joinNodeOpt = flowNodeRepository.findById(FlowNodeId.of(joinGatewayNodeId));
        if (joinNodeOpt.isEmpty()) {
            return false;
        }

        FlowNode joinNode = joinNodeOpt.get();
        if (joinNode.getGatewayType() != GatewayType.CONDITION_JOIN) {
            return false;
        }

        // 2. 查找所有指向Join节点的前驱节点（条件分支后的节点）
        List<FlowNode> predecessorNodes = findPredecessorNodes(joinGatewayNodeId, joinNode.getFlowDefId());

        if (predecessorNodes.isEmpty()) {
            return false;
        }

        // 3. 条件网关Join：至少有一个分支完成即可汇聚
        return predecessorNodes.stream().anyMatch(node -> {
            List<FlowNodeInstance> nodeInstances = flowNodeInstanceRepository.findByNodeIdAndFlowInstanceId(
                    node.getId(), flowInstanceId);
            return !nodeInstances.isEmpty() && nodeInstances.stream()
                    .anyMatch(ni -> {
                        NodeStatus status = ni.getStatus();
                        return status != null && status.isFinished();
                    });
        });
    }

    /**
     * 获取下一个节点ID列表
     */
    private List<Long> getNextNodeIds(Long currentNodeId, Long flowDefId, Map<String, Object> processVariables) {
        Optional<FlowNode> currentNodeOpt = flowNodeRepository.findById(FlowNodeId.of(currentNodeId));
        if (currentNodeOpt.isEmpty()) {
            log.warn("节点不存在，节点ID: {}", currentNodeId);
            return new ArrayList<>();
        }

        FlowNode currentNode = currentNodeOpt.get();
        List<Long> nextNodeIds = new ArrayList<>();

        // 1. 如果是网关Split节点，需要特殊处理
        if (currentNode.isSplitGateway()) {
            if (currentNode.getGatewayType() == GatewayType.CONDITION_SPLIT) {
                // 条件分支网关：评估条件表达式
                Long flowInstanceId = null; // 这里需要从上下文获取，暂时使用null
                return evaluateConditionSplit(currentNodeId, flowInstanceId, processVariables);
            } else if (currentNode.getGatewayType() == GatewayType.PARALLEL_SPLIT) {
                // 并行分支网关：获取所有分支节点
                // 继续使用 nextNodeIds 逻辑
            }
        }

        // 2. 优先使用 nextNodeIds（JSON格式，多个下一个节点）
        if (StringUtils.hasText(currentNode.getNextNodeIds())) {
            try {
                List<Long> nextNodeIdList = objectMapper.readValue(
                        currentNode.getNextNodeIds(),
                        new TypeReference<List<Long>>() {}
                );
                if (nextNodeIdList != null && !nextNodeIdList.isEmpty()) {
                    return nextNodeIdList;
                }
            } catch (Exception e) {
                log.error("解析 nextNodeIds 失败，节点ID: {}, nextNodeIds: {}", currentNodeId, currentNode.getNextNodeIds(), e);
            }
        }

        // 3. 使用 nextNodeId（单个下一个节点）
        if (currentNode.getNextNodeId() != null) {
            nextNodeIds.add(currentNode.getNextNodeId());
            return nextNodeIds;
        }

        // 4. 兼容旧数据：使用 orderNum + 1
        List<FlowNode> allNodes = flowNodeRepository.findByFlowDefId(FlowDefinitionId.of(flowDefId));
        Optional<FlowNode> nextNodeOpt = allNodes.stream()
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
     * 查找指向指定节点的前驱节点列表
     */
    private List<FlowNode> findPredecessorNodes(Long targetNodeId, Long flowDefId) {
        List<FlowNode> allNodes = flowNodeRepository.findByFlowDefId(FlowDefinitionId.of(flowDefId));
        List<FlowNode> predecessorNodes = new ArrayList<>();

        for (FlowNode node : allNodes) {
            // 检查 nextNodeId
            if (node.getNextNodeId() != null && node.getNextNodeId().equals(targetNodeId)) {
                predecessorNodes.add(node);
                continue;
            }

            // 检查 nextNodeIds
            if (StringUtils.hasText(node.getNextNodeIds())) {
                try {
                    List<Long> nextNodeIds = objectMapper.readValue(
                            node.getNextNodeIds(),
                            new TypeReference<List<Long>>() {}
                    );
                    if (nextNodeIds != null && nextNodeIds.contains(targetNodeId)) {
                        predecessorNodes.add(node);
                    }
                } catch (Exception e) {
                    log.error("解析 nextNodeIds 失败", e);
                }
            }
        }

        return predecessorNodes;
    }

    /**
     * 判断节点是否应该跳过
     */
    private boolean shouldSkipNode(Long nodeId, Long flowDefId, Map<String, Object> processVariables) {
        Optional<FlowNode> nodeOpt = flowNodeRepository.findById(FlowNodeId.of(nodeId));
        if (nodeOpt.isEmpty()) {
            return false;
        }

        FlowNode node = nodeOpt.get();

        // 如果节点有跳过条件，评估条件表达式
        if (StringUtils.hasText(node.getSkipCondition())) {
            return conditionEvaluationService.evaluate(node.getSkipCondition(), processVariables);
        }

        return false;
    }

    /**
     * 创建节点实例并分配审批人
     */
    private void createAndAssignNodeInstances(FlowInstance flowInstance, FlowNode node, 
                                               Map<String, Object> processVariables) {
        if (flowInstance == null || node == null) {
            throw new IllegalArgumentException("流程实例和节点不能为空");
        }

        Long flowInstanceId = flowInstance.getId() != null ? flowInstance.getId().getValue() : null;

        // 1. 分配审批人
        List<Approver> approvers = approverAssignmentService.assignApprovers(
                node.getId(),
                flowInstance.getFlowDefId(),
                flowInstanceId,
                processVariables);

        if (approvers.isEmpty()) {
            throw new IllegalStateException("节点 " + node.getNodeName() + " 无法分配审批人");
        }

        // 2. 为每个审批人创建节点实例
        for (Approver approver : approvers) {
            FlowNodeInstance nodeInstance = FlowNodeInstance.create(
                    flowInstanceId,
                    node.getId(),
                    approver);

            // 保存节点实例
            flowNodeInstanceRepository.save(nodeInstance);

            // 添加到聚合根
            flowInstance.addNodeInstance(nodeInstance);
        }

        log.info("节点实例创建成功，节点名称: {}, 创建了 {} 个实例", node.getNodeName(), approvers.size());
    }

    /**
     * 创建已跳过的节点实例
     */
    private void createSkippedNodeInstance(FlowInstance flowInstance, FlowNode node) {
        Long flowInstanceId = flowInstance.getId() != null ? flowInstance.getId().getValue() : null;

        // 创建跳过状态的节点实例（不需要审批人）
        FlowNodeInstance nodeInstance = FlowNodeInstance.create(
                flowInstanceId,
                node.getId(),
                null  // 跳过节点不需要审批人
        );

        // 设置为跳过状态
        nodeInstance.skip("节点已跳过（满足跳过条件）");
        flowNodeInstanceRepository.save(nodeInstance);
        flowInstance.addNodeInstance(nodeInstance);

        log.info("节点已跳过，节点名称: {}, 节点ID: {}", node.getNodeName(), node.getId());
    }
}
