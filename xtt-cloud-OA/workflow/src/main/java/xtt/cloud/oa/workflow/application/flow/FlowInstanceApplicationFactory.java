package xtt.cloud.oa.workflow.application.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xtt.cloud.oa.workflow.application.flow.command.StartFlowCommand;
import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowInstanceRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeInstanceRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeRepository;
import xtt.cloud.oa.workflow.domain.flow.service.ApproverAssignmentService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 流程实例应用工厂
 * 
 * 职责：
 * 1. 创建流程实例聚合根
 * 2. 组装流程实例（加载节点、创建节点实例、分配审批人等）
 * 3. 加载流程实例（从仓储加载并验证）
 * 4. 处理创建过程中的业务规则验证
 * 
 * 注意：这是应用层的工厂，可以依赖 Repository 和 Service
 * 
 * @author xtt
 */
@Component
public class FlowInstanceApplicationFactory {
    
    private static final Logger log = LoggerFactory.getLogger(FlowInstanceApplicationFactory.class);
    
    private final FlowNodeRepository flowNodeRepository;
    private final FlowNodeInstanceRepository flowNodeInstanceRepository;
    private final ApproverAssignmentService approverAssignmentService;
    private final FlowInstanceRepository flowInstanceRepository ;
    
    public FlowInstanceApplicationFactory(
            FlowNodeRepository flowNodeRepository,
            FlowNodeInstanceRepository flowNodeInstanceRepository,
            ApproverAssignmentService approverAssignmentService,
            FlowInstanceRepository flowInstanceRepository) {
        this.flowNodeRepository = flowNodeRepository;
        this.flowNodeInstanceRepository = flowNodeInstanceRepository;
        this.approverAssignmentService = approverAssignmentService;
        this.flowInstanceRepository = flowInstanceRepository;
    }
    
    /**
     * 创建并组装流程实例
     * 
     * 包括：
     * 1. 创建流程实例聚合根
     * 2. 启动流程
     * 3. 加载节点列表
     * 4. 创建第一个节点实例
     * 5. 分配审批人
     * 6. 设置当前节点
     * 
     * @param command 启动流程命令
     * @return 已创建和组装的流程实例
     */
    public FlowInstance createAndAssemble(StartFlowCommand command) {
        log.info("创建并组装流程实例，文档ID: {}, 流程定义ID: {}", 
                command.getDocumentId(), command.getFlowDefId());
        
        // 1. 创建流程实例聚合根
        FlowInstance flowInstance = FlowInstance.create(
                command.getDocumentId(),
                command.getFlowDefId(),
                command.getFlowType(),
                command.getFlowMode(),
                command.toProcessVariables()
        );
        
        // 2. 启动流程
        flowInstance.start();
        
        // 3. 保存聚合根（需要先保存以获取ID）
        flowInstance = flowInstanceRepository.save(flowInstance);
        
        // 4. 加载节点列表
        List<FlowNode> nodes = loadFlowNodes(flowInstance);
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("流程定义没有配置节点，流程定义ID: " + flowInstance.getFlowDefId());
        }
        
        // 5. 获取第一个节点
        FlowNode firstNode = getFirstNode(nodes);
        if (firstNode == null) {
            throw new IllegalArgumentException("无法找到第一个节点，流程定义ID: " + flowInstance.getFlowDefId());
        }
        
        // 6. 创建第一个节点实例并分配审批人
        createAndAssignNodeInstances(flowInstance, firstNode);
        
        // 7. 设置当前节点
        flowInstance.moveToNode(firstNode.getId());
        
        // 8. 再次保存聚合根（更新当前节点）
        flowInstance = flowInstanceRepository.save(flowInstance);
        
        log.info("流程实例创建并组装完成，流程实例ID: {}, 当前节点ID: {}", 
                flowInstance.getId() != null ? flowInstance.getId().getValue() : null, 
                firstNode.getId());
        
        return flowInstance;
    }
    
    /**
     * 加载流程节点列表
     */
    private List<FlowNode> loadFlowNodes(FlowInstance flowInstance) {
        if (flowInstance == null || flowInstance.getFlowDefId() == null) {
            throw new IllegalArgumentException("流程定义 FlowDefId 为空，流程实例ID: " + 
                    (flowInstance != null && flowInstance.getId() != null ? flowInstance.getId().getValue() : null));
        }
        
        List<FlowNode> nodes = flowNodeRepository.findByFlowDefId(flowInstance.getFlowDefId());
        return nodes;
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
                .sorted((a, b) -> {
                    Integer orderA = a.getOrderNum() != null ? a.getOrderNum() : Integer.MAX_VALUE;
                    Integer orderB = b.getOrderNum() != null ? b.getOrderNum() : Integer.MAX_VALUE;
                    return orderA.compareTo(orderB);
                })
                .findFirst()
                .orElse(nodes.get(0));
    }
    
    /**
     * 创建并分配节点实例
     * 
     * 此方法也可以被应用服务在流程运行过程中调用
     */
    public void createAndAssignNodeInstances(FlowInstance flowInstance, FlowNode node) {
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
            // 使用工厂方法创建节点实例
            FlowNodeInstance nodeInstance = FlowNodeInstance.create(
                    flowInstance.getId() != null ? flowInstance.getId().getValue() : null,
                    node.getId(),
                    approver
            );
            
            // 保存节点实例
            flowNodeInstanceRepository.save(nodeInstance);
            
            // TODO: 生成待办任务
            // taskService.createTodoTask(nodeInstance, approver.getUserId(), flowInstance, document);
        }
        
        log.info("节点实例创建成功，节点名称: {}, 创建了 {} 个实例", node.getNodeName(), approvers.size());
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
}

