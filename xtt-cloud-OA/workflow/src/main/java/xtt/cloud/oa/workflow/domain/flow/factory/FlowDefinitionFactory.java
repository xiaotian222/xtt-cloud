package xtt.cloud.oa.workflow.domain.flow.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowDefinition;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowDefinitionId;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.GatewayType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.GatewayMode;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowDefinitionRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeRepository;

import java.util.List;
import java.util.Optional;

/**
 * 流程定义领域工厂
 * 
 * 职责：
 * 1. 创建并初始化流程定义（领域逻辑）
 * 2. 创建网关节点
 * 3. 验证流程定义的完整性
 * 
 * 注意：这是领域层的工厂，可以依赖领域服务和仓储接口
 * 
 * @author xtt
 */
@Component
public class FlowDefinitionFactory {
    
    private static final Logger log = LoggerFactory.getLogger(FlowDefinitionFactory.class);
    
    private final FlowNodeRepository flowNodeRepository;
    private final FlowDefinitionRepository flowDefinitionRepository;
    
    public FlowDefinitionFactory(FlowNodeRepository flowNodeRepository, FlowDefinitionRepository flowDefinitionRepository) {
        this.flowNodeRepository = flowNodeRepository;
        this.flowDefinitionRepository = flowDefinitionRepository;
    }
    
    /**
     * 创建并初始化流程定义（领域逻辑）
     * 
     * 包括：
     * 1. 创建流程定义聚合根
     * 2. 验证流程定义的完整性
     * 
     * @param name 流程名称
     * @param code 流程编码
     * @param docTypeId 文档类型ID
     * @param description 描述
     * @param creatorId 创建人ID
     * @return 已创建的流程定义
     */
    public FlowDefinition createAndInitialize(
            String name,
            String code,
            Long docTypeId,
            String description,
            Long creatorId) {
        
        log.debug("创建并初始化流程定义，名称: {}, 编码: {}", name, code);
        
        // 1. 创建流程定义聚合根（使用聚合根内部的简单工厂方法）
        FlowDefinition flowDef = FlowDefinition.create(
                name, code, docTypeId, description, creatorId);
        
        // 2. 验证流程定义的完整性（如果有节点的话）
        // 注意：节点通常在创建后添加，这里只是预留验证逻辑
        
        return flowDef;
    }
    
    /**
     * 创建并行网关节点对（Split 和 Join）
     * 
     * @param flowDefId 流程定义ID
     * @param gatewayName 网关名称
     * @param gatewayMode 并行网关模式
     * @param splitNodeOrderNum Split 节点顺序号
     * @param joinNodeOrderNum Join 节点顺序号
     * @return Split 和 Join 节点
     */
    public GatewayNodePair createParallelGatewayNodes(
            Long flowDefId,
            String gatewayName,
            GatewayMode gatewayMode,
            Integer splitNodeOrderNum,
            Integer joinNodeOrderNum) {
        
        if (flowDefId == null || flowDefId <= 0) {
            throw new IllegalArgumentException("流程定义ID必须大于0");
        }
        if (gatewayName == null || gatewayName.trim().isEmpty()) {
            throw new IllegalArgumentException("网关名称不能为空");
        }
        if (gatewayMode == null) {
            throw new IllegalArgumentException("并行网关模式不能为空");
        }
        
        // 生成网关ID（可以使用 UUID 或序列号）
        Long gatewayId = System.currentTimeMillis(); // 简化实现，实际应该使用 ID 生成器
        
        // 创建 Split 节点
        FlowNode splitNode = FlowNode.create(
                flowDefId,
                gatewayName + "-分支",
                FlowNode.NODE_TYPE_GATEWAY,
                splitNodeOrderNum);
        splitNode.getPO().setGatewayType(GatewayType.PARALLEL_SPLIT.getValue());
        splitNode.getPO().setGatewayMode(gatewayMode != null ? gatewayMode.getValue() : null);
        splitNode.getPO().setGatewayId(gatewayId);
        
        // 创建 Join 节点
        FlowNode joinNode = FlowNode.create(
                flowDefId,
                gatewayName + "-汇聚",
                FlowNode.NODE_TYPE_GATEWAY,
                joinNodeOrderNum);
        joinNode.getPO().setGatewayType(GatewayType.PARALLEL_JOIN.getValue());
        joinNode.getPO().setGatewayMode(gatewayMode != null ? gatewayMode.getValue() : null);
        joinNode.getPO().setGatewayId(gatewayId);
        
        return new GatewayNodePair(splitNode, joinNode);
    }
    
    /**
     * 创建条件网关节点对（Split 和 Join）
     * 
     * @param flowDefId 流程定义ID
     * @param gatewayName 网关名称
     * @param conditionExpression 条件表达式（SpEL格式）
     * @param splitNodeOrderNum Split 节点顺序号
     * @param joinNodeOrderNum Join 节点顺序号
     * @return Split 和 Join 节点
     */
    public GatewayNodePair createConditionGatewayNodes(
            Long flowDefId,
            String gatewayName,
            String conditionExpression,
            Integer splitNodeOrderNum,
            Integer joinNodeOrderNum) {
        
        if (flowDefId == null || flowDefId <= 0) {
            throw new IllegalArgumentException("流程定义ID必须大于0");
        }
        if (gatewayName == null || gatewayName.trim().isEmpty()) {
            throw new IllegalArgumentException("网关名称不能为空");
        }
        
        // 生成网关ID
        Long gatewayId = System.currentTimeMillis();
        
        // 创建 Split 节点
        FlowNode splitNode = FlowNode.create(
                flowDefId,
                gatewayName + "-分支",
                FlowNode.NODE_TYPE_GATEWAY,
                splitNodeOrderNum);
        splitNode.getPO().setGatewayType(GatewayType.CONDITION_SPLIT.getValue());
        splitNode.getPO().setGatewayId(gatewayId);
        splitNode.getPO().setConditionExpression(conditionExpression);
        
        // 创建 Join 节点
        FlowNode joinNode = FlowNode.create(
                flowDefId,
                gatewayName + "-汇聚",
                FlowNode.NODE_TYPE_GATEWAY,
                joinNodeOrderNum);
        joinNode.getPO().setGatewayType(GatewayType.CONDITION_JOIN.getValue());
        joinNode.getPO().setGatewayId(gatewayId);
        
        return new GatewayNodePair(splitNode, joinNode);
    }
    
    /**
     * 验证流程定义的完整性
     * 
     * 检查：
     * 1. 是否有节点
     * 2. 网关节点是否成对出现
     * 3. 节点连接是否完整
     * 
     * @param flowDefId 流程定义ID
     * @throws IllegalArgumentException 如果流程定义不完整
     */
    public void validateFlowDefinition(FlowDefinitionId flowDefId) {
        List<FlowNode> nodes = flowNodeRepository.findByFlowDefId(flowDefId);
        
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("流程定义没有配置节点");
        }
        
        // 验证网关节点是否成对出现
        validateGatewayPairs(nodes);
        
        // TODO: 验证节点连接是否完整
    }
    
    /**
     * 验证网关节点是否成对出现
     */
    private void validateGatewayPairs(List<FlowNode> nodes) {
        // 统计 Split 和 Join 的数量
        long splitCount = nodes.stream()
                .filter(node -> {
                    GatewayType type = GatewayType.fromValue(node.getPO().getGatewayType());
                    return type != null && type.isSplit();
                })
                .count();
        
        long joinCount = nodes.stream()
                .filter(node -> {
                    GatewayType type = GatewayType.fromValue(node.getPO().getGatewayType());
                    return type != null && type.isJoin();
                })
                .count();
        
        if (splitCount != joinCount) {
            throw new IllegalArgumentException(
                    String.format("网关节点不成对，Split: %d, Join: %d", splitCount, joinCount));
        }
    }
    
    /**
     * 网关节点对（内部类）
     */
    public static class GatewayNodePair {
        private final FlowNode splitNode;
        private final FlowNode joinNode;
        
        public GatewayNodePair(FlowNode splitNode, FlowNode joinNode) {
            this.splitNode = splitNode;
            this.joinNode = joinNode;
        }
        
        public FlowNode getSplitNode() {
            return splitNode;
        }
        
        public FlowNode getJoinNode() {
            return joinNode;
        }
    }

    public FlowDefinition loadFlowDefinition(FlowDefinitionId flowDefId) {
        if (flowDefId == null || flowDefId.getValue()<= 0) {
            throw new IllegalArgumentException("流程定义id必须大于0！");
        }
        Optional<FlowDefinition> flowDefinition = flowDefinitionRepository.findById(flowDefId);
        return flowDefinition.orElse(null);
    }

    public void loadFlowNodes(FlowDefinition flowDefinition) {
        List<FlowNode> flowNodes = flowNodeRepository.findByFlowDefId(flowDefinition.getId());
        flowDefinition.setFlowNodeList(flowNodes);
    }

}

