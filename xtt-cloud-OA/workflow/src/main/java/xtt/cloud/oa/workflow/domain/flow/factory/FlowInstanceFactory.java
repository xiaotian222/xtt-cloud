package xtt.cloud.oa.workflow.domain.flow.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowDefinitionId;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowMode;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.ProcessVariables;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowInstanceRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeRepository;
import xtt.cloud.oa.workflow.domain.flow.service.ApproverAssignmentService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 流程实例领域工厂
 * 
 * 职责：
 * 1. 创建并初始化流程实例（领域逻辑）
 * 2. 加载节点列表
 * 3. 创建第一个节点实例
 * 4. 分配审批人
 * 
 * 注意：这是领域层的工厂，可以依赖领域服务和仓储接口
 * 
 * @author xtt
 */
@Component
public class FlowInstanceFactory {
    
    private static final Logger log = LoggerFactory.getLogger(FlowInstanceFactory.class);

    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowNodeRepository flowNodeRepository;
    private final ApproverAssignmentService approverAssignmentService;
    
    public FlowInstanceFactory(
            FlowInstanceRepository flowInstanceRepository, FlowNodeRepository flowNodeRepository,
            ApproverAssignmentService approverAssignmentService) {
        this.flowInstanceRepository = flowInstanceRepository;
        this.flowNodeRepository = flowNodeRepository;
        this.approverAssignmentService = approverAssignmentService;
    }
    
    /**
     * 创建并初始化流程实例（领域逻辑）
     * 
     * 包括：
     * 1. 创建流程实例聚合根
     * 2. 启动流程
     * 3. 加载节点列表
     * 4. 创建第一个节点实例
     * 5. 分配审批人
     * 6. 设置当前节点
     * 
     * @param documentId 文档ID
     * @param flowDefId 流程定义ID
     * @param flowType 流程类型
     * @param flowMode 流程模式
     * @param initialVariables 初始流程变量
     * @return 已创建和初始化的流程实例
     */
    public FlowInstance createAndInitialize(
            Long documentId,
            Long flowDefId,
            FlowType flowType,
            FlowMode flowMode,
            ProcessVariables initialVariables) {
        
        log.debug("创建并初始化流程实例，文档ID: {}, 流程定义ID: {}", documentId, flowDefId);
        
        // 1. 创建流程实例聚合根（使用聚合根内部的简单工厂方法）
        FlowInstance flowInstance = FlowInstance.create(
                documentId,
                flowDefId,
                flowType,
                flowMode,
                initialVariables);
        
        // 2. 启动流程
        flowInstance.start();
        
        // 3. 加载节点列表
        List<FlowNode> nodes = loadFlowNodes(flowDefId);
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("流程定义没有配置节点，流程定义ID: " + flowDefId);
        }
        
        // 4. 获取第一个节点
        FlowNode firstNode = getFirstNode(nodes);
        if (firstNode == null) {
            throw new IllegalArgumentException("无法找到第一个节点，流程定义ID: " + flowDefId);
        }
        
        // 5. 创建第一个节点实例并分配审批人
        createNodeInstances(flowInstance, firstNode);
        
        // 6. 设置当前节点
        flowInstance.moveToNode(firstNode.getId());
        
        log.debug("流程实例创建并初始化完成，当前节点ID: {}", firstNode.getId());
        
        return flowInstance;
    }

    /**
     * 加载流程节点列表
     */
    public List<FlowNode> loadFlowNodes(Long flowDefId) {
        if (flowDefId == null || flowDefId <= 0) {
            throw new IllegalArgumentException("流程定义ID必须大于0");
        }

        return flowNodeRepository.findByFlowDefId(FlowDefinitionId.of(flowDefId));
    }

    /**
     * 获取第一个节点（按 orderNum 排序）
     */
    private FlowNode getFirstNode(List<FlowNode> nodes) {
        if (nodes.isEmpty()) {
            return null;
        }
        
        // 按 orderNum 排序，取第一个
        return nodes.stream()
                .sorted(Comparator.comparing(node -> 
                    node.getOrderNum() != null ? node.getOrderNum() : Integer.MAX_VALUE))
                .findFirst()
                .orElse(nodes.get(0));
    }
    
    /**
     * 创建节点实例并分配审批人
     * 
     * 此方法封装了节点实例创建和审批人分配的逻辑
     * 
     * @param flowInstance 流程实例
     * @param node 节点定义
     */
    public void createNodeInstances(FlowInstance flowInstance, FlowNode node) {
        if (flowInstance == null || node == null) {
            throw new IllegalArgumentException("流程实例和节点不能为空");
        }
        
        // 1. 分配审批人
        Map<String, Object> processVariables = flowInstance.getProcessVariables().getAllVariables();
        List<Approver> approvers = approverAssignmentService.assignApprovers(
                node.getId(),
                flowInstance.getFlowDefId(),
                flowInstance.getId() != null ? flowInstance.getId().getValue() : null,
                processVariables);
        
        if (approvers.isEmpty()) {
            throw new IllegalStateException("节点 " + node.getNodeName() + " 无法分配审批人");
        }
        
        // 2. 为每个审批人创建节点实例
        for (Approver approver : approvers) {
            FlowNodeInstance nodeInstance = FlowNodeInstance.create(
                    flowInstance.getId() != null ? flowInstance.getId().getValue() : null,
                    node.getId(),
                    approver);
            
            // 添加到聚合根
            flowInstance.addNodeInstance(nodeInstance);
        }
        
        log.debug("节点实例创建成功，节点名称: {}, 创建了 {} 个实例", node.getNodeName(), approvers.size());
    }

    /**
     * 加载流程实例（如果不存在则抛出异常）
     *
     * @param flowInstanceId 流程实例ID
     * @return 流程实例聚合根
     * @throws IllegalArgumentException 如果流程实例不存在
     */
    public FlowInstance loadFlowInstance(Long flowInstanceId) {
        if (flowInstanceId == null) {
            throw new IllegalArgumentException("流程实例ID不能为空");
        }

        Optional<FlowInstance> optional = flowInstanceRepository.findById(flowInstanceId);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("流程实例不存在: " + flowInstanceId);
        }
        return optional.get();
    }

    /**
     * 加载流程 节点实例 列表
     */
    public void loadFlowNodeInstances(FlowInstance flowInstance) {
        if (flowInstance == null || flowInstance.getFlowDefId() == null) {
            throw new IllegalArgumentException("流程定义 FlowDefId 为空，流程实例ID: " +
                    (flowInstance != null && flowInstance.getId() != null ? flowInstance.getId().getValue() : null));
        }
        List<FlowNodeInstance> FlowNodeInstance = flowInstanceRepository.findAllByFlowInstanceId(flowInstance.getId());
        flowInstance.setNodeInstances(FlowNodeInstance);
    }


}

