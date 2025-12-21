package xtt.cloud.oa.workflow.application.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.workflow.application.flow.assembler.FlowInstanceAssembler;
import xtt.cloud.oa.workflow.application.flow.command.*;
import xtt.cloud.oa.workflow.application.flow.dto.FlowInstanceDTO;
import xtt.cloud.oa.workflow.application.flow.query.FlowInstanceQuery;
import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.NodeStatus;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowInstanceRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeInstanceRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeRepository;
import xtt.cloud.oa.workflow.domain.flow.service.NodeRoutingService;
import xtt.cloud.oa.workflow.domain.flow.service.ApproverAssignmentService;
import xtt.cloud.oa.workflow.infrastructure.messaging.event.DomainEventPublisher;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 流程应用服务
 * 
 * 职责：
 * 1. 编排领域服务和聚合根
 * 2. 事务管理
 * 3. 权限控制
 * 4. 发布领域事件
 * 
 * @author xtt
 */
@Service
public class FlowApplicationService {
    
    private static final Logger log = LoggerFactory.getLogger(FlowApplicationService.class);
    
    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowNodeRepository flowNodeRepository;
    private final FlowNodeInstanceRepository flowNodeInstanceRepository;
    private final NodeRoutingService nodeRoutingService;
    private final ApproverAssignmentService approverAssignmentService;
    private final DomainEventPublisher eventPublisher;
    
    public FlowApplicationService(
            FlowInstanceRepository flowInstanceRepository,
            FlowNodeRepository flowNodeRepository,
            FlowNodeInstanceRepository flowNodeInstanceRepository,
            NodeRoutingService nodeRoutingService,
            ApproverAssignmentService approverAssignmentService,
            DomainEventPublisher eventPublisher) {
        this.flowInstanceRepository = flowInstanceRepository;
        this.flowNodeRepository = flowNodeRepository;
        this.flowNodeInstanceRepository = flowNodeInstanceRepository;
        this.nodeRoutingService = nodeRoutingService;
        this.approverAssignmentService = approverAssignmentService;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * 启动流程
     */
    @Transactional
    public FlowInstanceDTO startFlow(StartFlowCommand command) {
        log.info("启动流程，文档ID: {}, 流程定义ID: {}", command.getDocumentId(), command.getFlowDefId());
        
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
        
        // 3. 保存聚合根
        flowInstance = flowInstanceRepository.save(flowInstance);
        
        // 4. 获取第一个节点并创建节点实例
        FlowNode firstNode = getFirstNode(command.getFlowDefId());
        if (firstNode != null) {
            createAndAssignNodeInstances(flowInstance, firstNode);
            flowInstance.moveToNode(firstNode.getId());
        }
        
        // 5. 保存聚合根
        flowInstance = flowInstanceRepository.save(flowInstance);
        
        // 6. 发布领域事件
        publishDomainEvents(flowInstance);
        
        // 7. 转换为DTO返回
        return FlowInstanceAssembler.toDTO(flowInstance);
    }
    
    /**
     * 审批通过
     */
    @Transactional
    public void approve(ApproveCommand command) {
        log.info("审批通过，流程实例ID: {}, 节点实例ID: {}, 审批人ID: {}", 
                command.getFlowInstanceId(), command.getNodeInstanceId(), command.getApproverId());
        
        // 1. 加载流程实例聚合根
        FlowInstance flowInstance = loadFlowInstance(command.getFlowInstanceId());
        
        // 2. 验证流程状态
        if (!flowInstance.getStatus().canProceed()) {
            throw new IllegalStateException("流程状态不允许审批，当前状态: " + flowInstance.getStatus());
        }
        
        // 3. 查找节点实例
        FlowNodeInstance nodeInstance = findNodeInstance(command.getNodeInstanceId());
        if (nodeInstance == null) {
            throw new IllegalArgumentException("节点实例不存在: " + command.getNodeInstanceId());
        }
        
        // 4. 验证审批权限
        if (!approverAssignmentService.hasApprovalPermission(
                command.getApproverId(), nodeInstance.getNodeId(), command.getFlowInstanceId())) {
            throw new SecurityException("无权审批此节点");
        }
        
        // 5. 完成节点实例
        nodeInstance.setStatus(FlowNodeInstance.STATUS_COMPLETED);
        nodeInstance.setComments(command.getComments());
        nodeInstance.setHandledAt(java.time.LocalDateTime.now());
        nodeInstance.setUpdatedAt(java.time.LocalDateTime.now());
        flowNodeInstanceRepository.update(nodeInstance);
        
        // 6. 检查是否可以流转到下一个节点
        FlowNode currentNodeDef = flowNodeRepository.findById(nodeInstance.getNodeId());
        if (canMoveToNextNode(flowInstance, currentNodeDef, nodeInstance)) {
            moveToNextNode(flowInstance, currentNodeDef, nodeInstance);
        } else {
            // 检查流程是否完成
            checkAndCompleteFlow(flowInstance);
        }
        
        // 7. 保存聚合根
        flowInstanceRepository.save(flowInstance);
        
        // 8. 发布领域事件
        publishDomainEvents(flowInstance);
    }
    
    /**
     * 审批拒绝
     */
    @Transactional
    public void reject(RejectCommand command) {
        log.info("审批拒绝，流程实例ID: {}, 节点实例ID: {}, 审批人ID: {}", 
                command.getFlowInstanceId(), command.getNodeInstanceId(), command.getApproverId());
        
        // 1. 加载流程实例聚合根
        FlowInstance flowInstance = loadFlowInstance(command.getFlowInstanceId());
        
        // 2. 验证流程状态
        if (!flowInstance.getStatus().canProceed()) {
            throw new IllegalStateException("流程状态不允许审批，当前状态: " + flowInstance.getStatus());
        }
        
        // 3. 查找节点实例
        FlowNodeInstance nodeInstance = findNodeInstance(command.getNodeInstanceId());
        if (nodeInstance == null) {
            throw new IllegalArgumentException("节点实例不存在: " + command.getNodeInstanceId());
        }
        
        // 4. 验证审批权限
        if (!approverAssignmentService.hasApprovalPermission(
                command.getApproverId(), nodeInstance.getNodeId(), command.getFlowInstanceId())) {
            throw new SecurityException("无权审批此节点");
        }
        
        // 5. 拒绝节点实例
        nodeInstance.setStatus(FlowNodeInstance.STATUS_REJECTED);
        nodeInstance.setComments(command.getComments());
        nodeInstance.setHandledAt(java.time.LocalDateTime.now());
        nodeInstance.setUpdatedAt(java.time.LocalDateTime.now());
        flowNodeInstanceRepository.update(nodeInstance);
        
        // 6. 如果指定了回退节点，执行回退
        if (command.hasRollback()) {
            rollbackToNode(flowInstance, command.getRollbackToNodeId(), command.getApproverId(), command.getComments());
        } else {
            // 否则终止流程
            flowInstance.terminate();
        }
        
        // 7. 保存聚合根
        flowInstanceRepository.save(flowInstance);
        
        // 8. 发布领域事件
        publishDomainEvents(flowInstance);
    }
    
    /**
     * 撤回流程
     */
    @Transactional
    public void withdraw(WithdrawCommand command) {
        log.info("撤回流程，流程实例ID: {}, 发起人ID: {}", 
                command.getFlowInstanceId(), command.getInitiatorId());
        
        // 1. 加载流程实例聚合根
        FlowInstance flowInstance = loadFlowInstance(command.getFlowInstanceId());
        
        // 2. 验证权限（只有发起人可以撤回）
        // TODO: 实现权限验证，需要从流程变量或文档中获取发起人ID
        // if (!canWithdraw(flowInstance, command.getInitiatorId())) {
        //     throw new FlowWithdrawException("无权撤回此流程");
        // }
        
        // 3. 验证流程状态
        if (!flowInstance.getStatus().canProceed()) {
            throw new IllegalStateException("流程状态不允许撤回，当前状态: " + flowInstance.getStatus());
        }
        
        // 4. 取消流程
        flowInstance.cancel();
        
        // 5. 保存聚合根
        flowInstanceRepository.save(flowInstance);
        
        // 6. 发布领域事件
        publishDomainEvents(flowInstance);
    }
    
    /**
     * 回退流程
     */
    @Transactional
    public void rollback(RollbackCommand command) {
        log.info("回退流程，流程实例ID: {}, 目标节点ID: {}, 操作人ID: {}", 
                command.getFlowInstanceId(), command.getTargetNodeId(), command.getApproverId());
        
        // 1. 加载流程实例聚合根
        FlowInstance flowInstance = loadFlowInstance(command.getFlowInstanceId());
        
        // 2. 验证权限
        // TODO: 实现权限验证
        
        // 3. 回退到指定节点
        rollbackToNode(flowInstance, command.getTargetNodeId(), command.getApproverId(), command.getReason());
        
        // 4. 保存聚合根
        flowInstanceRepository.save(flowInstance);
        
        // 5. 发布领域事件
        publishDomainEvents(flowInstance);
    }
    
    /**
     * 暂停流程
     */
    @Transactional
    public void suspend(Long flowInstanceId) {
        FlowInstance flowInstance = loadFlowInstance(flowInstanceId);
        flowInstance.suspend();
        flowInstanceRepository.save(flowInstance);
        publishDomainEvents(flowInstance);
    }
    
    /**
     * 恢复流程
     */
    @Transactional
    public void resume(Long flowInstanceId) {
        FlowInstance flowInstance = loadFlowInstance(flowInstanceId);
        flowInstance.resume();
        flowInstanceRepository.save(flowInstance);
        publishDomainEvents(flowInstance);
    }
    
    /**
     * 查询流程实例
     */
    @Transactional(readOnly = true)
    public FlowInstanceDTO getFlowInstance(Long flowInstanceId) {
        FlowInstance flowInstance = loadFlowInstance(flowInstanceId);
        return FlowInstanceAssembler.toDTO(flowInstance);
    }
    
    /**
     * 查询流程实例列表
     */
    @Transactional(readOnly = true)
    public List<FlowInstanceDTO> queryFlowInstances(FlowInstanceQuery query) {
        // TODO: 实现查询逻辑
        // 这里需要根据查询条件从仓储中查询
        // List<FlowInstance> flowInstances = flowInstanceRepository.findByQuery(query);
        // return flowInstances.stream()
        //         .map(FlowInstanceAssembler::toDTO)
        //         .collect(Collectors.toList());
        return List.of();
    }
    
    /**
     * 设置流程变量
     */
    @Transactional
    public void setProcessVariable(Long flowInstanceId, String key, Object value) {
        FlowInstance flowInstance = loadFlowInstance(flowInstanceId);
        flowInstance.setProcessVariable(key, value);
        flowInstanceRepository.save(flowInstance);
    }
    
    /**
     * 获取流程变量
     */
    @Transactional(readOnly = true)
    public Object getProcessVariable(Long flowInstanceId, String key) {
        FlowInstance flowInstance = loadFlowInstance(flowInstanceId);
        return flowInstance.getProcessVariable(key);
    }
    
    // ========== 私有方法 ==========
    
    /**
     * 加载流程实例（如果不存在则抛出异常）
     */
    private FlowInstance loadFlowInstance(Long flowInstanceId) {
        Optional<FlowInstance> optional = flowInstanceRepository.findById(flowInstanceId);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Flow instance not found: " + flowInstanceId);
        }
        return optional.get();
    }
    
    /**
     * 发布领域事件
     */
    private void publishDomainEvents(FlowInstance flowInstance) {
        List<Object> events = flowInstance.getAndClearDomainEvents();
        if (!events.isEmpty()) {
            eventPublisher.publishAll(events);
        }
    }
    
    /**
     * 获取第一个节点
     */
    private FlowNode getFirstNode(Long flowDefId) {
        List<FlowNode> nodes = flowNodeRepository.findByFlowDefId(flowDefId);
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("流程定义没有配置节点，流程定义ID: " + flowDefId);
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
     */
    private void createAndAssignNodeInstances(FlowInstance flowInstance, FlowNode node) {
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
            // 创建持久化层的节点实例（PO）
            FlowNodeInstance nodeInstance = new FlowNodeInstance();
            nodeInstance.setFlowInstanceId(flowInstance.getId() != null ? flowInstance.getId().getValue() : null);
            nodeInstance.setNodeId(node.getId());
            nodeInstance.setApproverId(approver.getUserId());
            nodeInstance.setApproverDeptId(approver.getDeptId());
            nodeInstance.setStatus(FlowNodeInstance.STATUS_PENDING);
            nodeInstance.setCreatedAt(java.time.LocalDateTime.now());
            nodeInstance.setUpdatedAt(java.time.LocalDateTime.now());
            
            // 保存节点实例
            flowNodeInstanceRepository.save(nodeInstance);
            
            // TODO: 生成待办任务
            // taskService.createTodoTask(nodeInstance, approver.getUserId(), flowInstance, document);
        }
        
        log.info("节点实例创建成功，节点名称: {}, 创建了 {} 个实例", node.getNodeName(), approvers.size());
    }
    
    /**
     * 查找节点实例
     */
    private FlowNodeInstance findNodeInstance(Long nodeInstanceId) {
        return flowNodeInstanceRepository.findById(nodeInstanceId);
    }
    
    /**
     * 判断是否可以流转到下一个节点
     */
    private boolean canMoveToNextNode(FlowInstance flowInstance, FlowNode currentNodeDef, FlowNodeInstance currentNode) {
        // 如果是并行节点，需要检查并行模式
        if (currentNodeDef.isParallelMode()) {
            if (currentNodeDef.isParallelAllMode()) {
                // 会签模式：所有节点都完成
                return allParallelNodesCompleted(flowInstance, currentNodeDef);
            } else if (currentNodeDef.isParallelAnyMode()) {
                // 或签模式：任一节点完成
                return anyParallelNodeCompleted(flowInstance, currentNodeDef);
            }
        }
        
        // 串行节点：当前节点完成即可流转
        return true;
    }
    
    /**
     * 检查并行节点是否全部完成（会签模式）
     */
    private boolean allParallelNodesCompleted(FlowInstance flowInstance, FlowNode node) {
        List<FlowNodeInstance> nodeInstances = flowNodeInstanceRepository.findByFlowInstanceIdAndNodeId(
                flowInstance.getId() != null ? flowInstance.getId().getValue() : null, 
                node.getId());
        
        if (nodeInstances.isEmpty()) {
            return false;
        }
        
        return nodeInstances.stream()
                .allMatch(ni -> {
                    NodeStatus status = NodeStatus.fromValue(ni.getStatus());
                    return status.isFinished();
                });
    }
    
    /**
     * 检查并行节点是否任一完成（或签模式）
     */
    private boolean anyParallelNodeCompleted(FlowInstance flowInstance, FlowNode node) {
        List<FlowNodeInstance> nodeInstances = flowNodeInstanceRepository.findByFlowInstanceIdAndNodeId(
                flowInstance.getId() != null ? flowInstance.getId().getValue() : null, 
                node.getId());
        
        return nodeInstances.stream()
                .anyMatch(ni -> {
                    NodeStatus status = NodeStatus.fromValue(ni.getStatus());
                    return status.isFinished();
                });
    }
    
    /**
     * 流转到下一个节点
     */
    private void moveToNextNode(FlowInstance flowInstance, FlowNode currentNodeDef, FlowNodeInstance currentNode) {
        log.debug("流转到下一个节点，流程实例ID: {}, 当前节点ID: {}", 
                flowInstance.getId() != null ? flowInstance.getId().getValue() : null, 
                currentNodeDef.getId());
        
        // 1. 获取下一个节点列表
        Map<String, Object> processVariables = flowInstance.getProcessVariables().getAllVariables();
        List<Long> nextNodeIds = nodeRoutingService.getNextNodeIds(
                currentNodeDef.getId(), 
                flowInstance.getFlowDefId(), 
                processVariables);
        
        if (nextNodeIds.isEmpty()) {
            log.debug("没有下一个节点，流程将结束");
            return;
        }
        
        // 2. 检查汇聚节点
        for (Long nextNodeId : nextNodeIds) {
            if (nodeRoutingService.isConvergenceNode(nextNodeId, flowInstance.getFlowDefId())) {
                // 如果是汇聚节点，检查是否可以汇聚
                if (!nodeRoutingService.canConverge(nextNodeId, 
                        flowInstance.getId() != null ? flowInstance.getId().getValue() : null)) {
                    log.debug("汇聚节点 {} 尚未满足汇聚条件，等待其他分支完成", nextNodeId);
                    continue;
                }
            }
            
            // 3. 获取节点定义
            FlowNode nextNode = flowNodeRepository.findById(nextNodeId);
            if (nextNode == null) {
                log.warn("下一个节点不存在，节点ID: {}", nextNodeId);
                continue;
            }
            
            // 4. 检查是否应该跳过节点
            if (nodeRoutingService.shouldSkipNode(nextNodeId, flowInstance.getFlowDefId(), processVariables)) {
                // 创建已跳过的节点实例
                createSkippedNodeInstance(flowInstance, nextNode);
                // 递归处理下一个节点
                moveToNextNode(flowInstance, nextNode, null);
                return;
            }
            
            // 5. 创建节点实例并分配审批人
            createAndAssignNodeInstances(flowInstance, nextNode);
        }
        
        // 6. 更新当前节点（如果有下一个节点，更新为第一个）
        if (!nextNodeIds.isEmpty()) {
            flowInstance.moveToNode(nextNodeIds.get(0));
        }
    }
    
    /**
     * 创建已跳过的节点实例
     */
    private void createSkippedNodeInstance(FlowInstance flowInstance, FlowNode node) {
        FlowNodeInstance nodeInstance = new FlowNodeInstance();
        nodeInstance.setFlowInstanceId(flowInstance.getId() != null ? flowInstance.getId().getValue() : null);
        nodeInstance.setNodeId(node.getId());
        nodeInstance.setStatus(FlowNodeInstance.STATUS_SKIPPED);
        nodeInstance.setComments("节点已跳过（满足跳过条件）");
        nodeInstance.setHandledAt(java.time.LocalDateTime.now());
        nodeInstance.setCreatedAt(java.time.LocalDateTime.now());
        nodeInstance.setUpdatedAt(java.time.LocalDateTime.now());
        
        flowNodeInstanceRepository.save(nodeInstance);
        
        log.info("节点已跳过，节点名称: {}, 节点ID: {}", node.getNodeName(), node.getId());
    }
    
    /**
     * 检查并完成流程
     */
    private void checkAndCompleteFlow(FlowInstance flowInstance) {
        FlowNode currentNode = flowNodeRepository.findById(flowInstance.getCurrentNodeId());
        if (currentNode != null && currentNode.getIsLastNode() != null && currentNode.getIsLastNode() == 1) {
            // 检查最后一个节点的所有实例是否完成
            if (currentNode.isParallelAllMode()) {
                if (allParallelNodesCompleted(flowInstance, currentNode)) {
                    flowInstance.complete();
                }
            } else {
                // 串行或或签模式，任一完成即可
                List<FlowNodeInstance> nodeInstances = flowNodeInstanceRepository.findByFlowInstanceIdAndNodeId(
                        flowInstance.getId() != null ? flowInstance.getId().getValue() : null, 
                        currentNode.getId());
                if (!nodeInstances.isEmpty()) {
                    boolean hasCompleted = nodeInstances.stream()
                            .anyMatch(ni -> {
                                NodeStatus status = NodeStatus.fromValue(ni.getStatus());
                                return status.isFinished();
                            });
                    if (hasCompleted) {
                        flowInstance.complete();
                    }
                }
            }
        } else {
            // 检查是否没有下一个节点
            Map<String, Object> processVariables = flowInstance.getProcessVariables().getAllVariables();
            List<Long> nextNodeIds = nodeRoutingService.getNextNodeIds(
                    flowInstance.getCurrentNodeId(), 
                    flowInstance.getFlowDefId(), 
                    processVariables);
            if (nextNodeIds.isEmpty()) {
                flowInstance.complete();
            }
        }
    }
    
    /**
     * 回退到指定节点
     */
    private void rollbackToNode(FlowInstance flowInstance, Long targetNodeId, Long operatorId, String reason) {
        log.info("回退到节点，流程实例ID: {}, 目标节点ID: {}, 操作人ID: {}", 
                flowInstance.getId() != null ? flowInstance.getId().getValue() : null, 
                targetNodeId, operatorId);
        
        // 1. 验证目标节点是否存在
        FlowNode targetNode = flowNodeRepository.findById(targetNodeId);
        if (targetNode == null) {
            throw new IllegalArgumentException("目标节点不存在: " + targetNodeId);
        }
        
        // 2. 验证目标节点是否属于当前流程定义
        if (!targetNode.getFlowDefId().equals(flowInstance.getFlowDefId())) {
            throw new IllegalArgumentException("目标节点不属于当前流程定义");
        }
        
        // 3. 取消当前节点及之后的所有待办任务
        // TODO: 实现取消待办任务的逻辑
        
        // 4. 更新流程实例当前节点
        flowInstance.moveToNode(targetNodeId);
        
        // 5. 创建目标节点的节点实例
        createAndAssignNodeInstances(flowInstance, targetNode);
        
        log.info("回退成功，流程实例ID: {}, 目标节点ID: {}", 
                flowInstance.getId() != null ? flowInstance.getId().getValue() : null, 
                targetNodeId);
    }
}
