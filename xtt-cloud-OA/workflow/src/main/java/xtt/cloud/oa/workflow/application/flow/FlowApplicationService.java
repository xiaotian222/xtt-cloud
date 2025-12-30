package xtt.cloud.oa.workflow.application.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.workflow.application.flow.assembler.FlowInstanceAssembler;
import xtt.cloud.oa.workflow.application.flow.command.*;
import xtt.cloud.oa.workflow.application.flow.dto.FlowInstanceDTO;
import xtt.cloud.oa.workflow.domain.flow.factory.FlowDefinitionFactory;
import xtt.cloud.oa.workflow.domain.flow.factory.FlowInstanceFactory;
import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowDefinition;
import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowDefinitionId;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskAction;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowInstanceRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeInstanceRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeRepository;
import xtt.cloud.oa.workflow.domain.flow.service.NodeRoutingService;
import xtt.cloud.oa.workflow.domain.flow.service.ApproverAssignmentService;
import xtt.cloud.oa.workflow.domain.flow.service.TaskService;
import xtt.cloud.oa.workflow.infrastructure.cache.CacheUpdateService;
import xtt.cloud.oa.workflow.infrastructure.cache.flowinstance.FlowInstanceCacheService;
import xtt.cloud.oa.workflow.infrastructure.lock.DistributedLockService;
import xtt.cloud.oa.workflow.infrastructure.messaging.event.DomainEventPublisher;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
    private final CacheUpdateService cacheUpdateService;
    private final FlowInstanceCacheService flowInstanceCacheService;
    private final DistributedLockService distributedLockService;
    private final FlowInstanceFactory flowInstanceFactory;
    private final FlowDefinitionFactory FlowDefinitionFactory;
    private final TaskService taskService;

    public FlowApplicationService(
            FlowInstanceRepository flowInstanceRepository,
            FlowNodeRepository flowNodeRepository,
            FlowNodeInstanceRepository flowNodeInstanceRepository,
            NodeRoutingService nodeRoutingService,
            ApproverAssignmentService approverAssignmentService,
            DomainEventPublisher eventPublisher,
            CacheUpdateService cacheUpdateService,
            FlowInstanceCacheService flowInstanceCacheService,
            DistributedLockService distributedLockService, FlowInstanceFactory flowInstanceFactory, FlowDefinitionFactory flowDefinitionFactory, TaskService taskService) {
        this.flowInstanceRepository = flowInstanceRepository;
        this.flowNodeRepository = flowNodeRepository;
        this.flowNodeInstanceRepository = flowNodeInstanceRepository;
        this.nodeRoutingService = nodeRoutingService;
        this.approverAssignmentService = approverAssignmentService;
        this.eventPublisher = eventPublisher;
        this.cacheUpdateService = cacheUpdateService;
        this.flowInstanceCacheService = flowInstanceCacheService;
        this.distributedLockService = distributedLockService;
        this.flowInstanceFactory = flowInstanceFactory;
        FlowDefinitionFactory = flowDefinitionFactory;
        this.taskService = taskService;
    }
    
    /**
     * 启动流程
     */
    @Transactional
    public FlowInstanceDTO startFlow(StartFlowCommand command) {
        log.info("启动流程，文档ID: {}, 流程定义ID: {}", command.getDocumentId(), command.getFlowDefId());
        
        // 使用工厂创建并组装流程实例
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
        List<FlowNode> nodes = flowInstanceFactory.loadFlowNodes(flowInstance);
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

        // 发布领域事件
        publishDomainEvents(flowInstance);
        
        // 更新缓存
        FlowInstanceDTO dto = FlowInstanceAssembler.toDTO(flowInstance);
        cacheUpdateService.updateFlowInstanceCache(dto);
        
        return dto;
    }
    
    /**
     * 审批通过
     */
    @Transactional
    public void approve(ApproveCommand command) {
        log.info("审批通过，流程实例ID: {}, 节点实例ID: {}, 审批人ID: {}", 
                command.getFlowInstanceId(), command.getNodeInstanceId(), command.getApproverId());
        
        // 1. 加载流程实例聚合根
        FlowInstance flowInstance = flowInstanceFactory.loadFlowInstance(command.getFlowInstanceId());
        flowInstanceFactory.loadFlowNodeInstances(flowInstance);
        
        // 2. 验证流程状态
        if (!flowInstance.getStatus().canProceed()) {
            throw new IllegalStateException("流程状态不允许审批，当前状态: " + flowInstance.getStatus());
        }

        // 3. 验证审批权限
        if (!approverAssignmentService.hasApprovalPermission(
                command.getApproverId(), command.getNodeId(), command.getFlowInstanceId())) {
            throw new SecurityException("无权审批此节点");
        }

        // 4. 完结当前节点实例, todo 取消其他未操作的节点实例。
        FlowNodeInstance nodeInstance = findNodeInstance(command.getNodeInstanceId())
                .orElseThrow(() -> new IllegalArgumentException("节点实例不存在: " + command.getNodeInstanceId()));
        nodeInstance.complete(command.getComments());
        flowNodeInstanceRepository.save(nodeInstance);
        taskService.markAsHandled(nodeInstance, TaskAction.APPROVE, "", flowInstance);
        // todo 针对一个节点中审批人是多个的情况下，暂定为或签。 1.删除其他的nodeInstance 2.删除其他的todoTask

        // 6. 检查并流转到下一个节点
//        FlowNode currentNodeDef = flowNodeRepository.findById(
//                        xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowNodeId.of(nodeInstance.getNodeId()))
//                .orElseThrow(() -> new IllegalArgumentException("节点不存在: " + nodeInstance.getNodeId()));
//        Long flowInstanceId = flowInstance.getId() != null ? flowInstance.getId().getValue() : null;

        FlowDefinition flowDefinition = FlowDefinitionFactory.loadFlowDefinition(FlowDefinitionId.of(flowInstance.getFlowDefId()));
        if (nodeRoutingService.canMoveToNextNode(flowInstance, flowDefinition)) {
            nodeRoutingService.moveToNextNode(flowInstance, flowDefinition);
        } else {
            if (nodeRoutingService.canCompleteFlow(
                    flowInstance.getCurrentNodeId(),
                    flowInstance.getFlowDefId(),
                    flowInstanceId,
                    flowInstance.getProcessVariables().getAllVariables())) {
                flowInstance.complete();
            }
        }
        
        // 7. 保存聚合根
        flowInstanceRepository.save(flowInstance);
        
        // 8. 发布领域事件
        publishDomainEvents(flowInstance);
    }

    /**
     * 审批回退
     */
    @Transactional
    public void rollback(RollbackCommand command) {
        log.info("回退流程，流程实例ID: {}, 目标节点ID: {}, 操作人ID: {}",
                command.getFlowInstanceId(), command.getTargetNodeId(), command.getApproverId());

        // 1. 加载流程实例聚合根
        FlowInstance flowInstance = flowInstanceFactory.loadFlowInstance(command.getFlowInstanceId());

        // 2. 验证权限
        // TODO: 实现权限验证

        // 3. 回退到指定节点
        nodeRoutingService.rollbackToNode(flowInstance, command.getTargetNodeId(), command.getApproverId(), command.getReason());

        // 4. 保存聚合根
        flowInstanceRepository.save(flowInstance);

        // 5. 发布领域事件
        publishDomainEvents(flowInstance);

        // 6. 更新缓存
        FlowInstanceDTO dto = FlowInstanceAssembler.toDTO(flowInstance);
        cacheUpdateService.updateFlowInstanceCache(dto);
    }
    
    /**
     * 审批拒绝
     */
    @Transactional
    public void reject(RejectCommand command) {
        log.info("审批拒绝，流程实例ID: {}, 节点实例ID: {}, 审批人ID: {}", 
                command.getFlowInstanceId(), command.getNodeInstanceId(), command.getApproverId());
        
        // 1. 加载流程实例聚合根
        FlowInstance flowInstance = flowInstanceFactory.loadFlowInstance(command.getFlowInstanceId());
        
        // 2. 验证流程状态
        if (!flowInstance.getStatus().canProceed()) {
            throw new IllegalStateException("流程状态不允许审批，当前状态: " + flowInstance.getStatus());
        }
        
        // 3. 查找节点实例
        FlowNodeInstance nodeInstance = findNodeInstance(command.getNodeInstanceId())
                .orElseThrow(() -> new IllegalArgumentException("节点实例不存在: " + command.getNodeInstanceId()));
        
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
        flowNodeInstanceRepository.save(nodeInstance);
        
        // 6. 如果指定了回退节点，执行回退
        if (command.hasRollback()) {
            nodeRoutingService.rollbackToNode(flowInstance, command.getRollbackToNodeId(), command.getApproverId(), command.getComments());
        } else {
            // 否则终止流程
            flowInstance.terminate();
        }
        
        // 7. 保存聚合根
        flowInstanceRepository.save(flowInstance);
        
        // 8. 发布领域事件
        publishDomainEvents(flowInstance);
        
        // 9. 更新缓存
        FlowInstanceDTO dto = FlowInstanceAssembler.toDTO(flowInstance);
        cacheUpdateService.updateFlowInstanceCache(dto);
    }
    
    /**
     * 撤回流程
     */
    @Transactional
    public void withdraw(WithdrawCommand command) {
        log.info("撤回流程，流程实例ID: {}, 发起人ID: {}", 
                command.getFlowInstanceId(), command.getInitiatorId());
        
        // 1. 加载流程实例聚合根
        FlowInstance flowInstance = flowInstanceFactory.loadFlowInstance(command.getFlowInstanceId());
        
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
        
        // 7. 更新缓存
        FlowInstanceDTO dto = FlowInstanceAssembler.toDTO(flowInstance);
        cacheUpdateService.updateFlowInstanceCache(dto);
    }

    /**
     * 暂停流程
     */
    @Transactional
    public void suspend(Long flowInstanceId) {
        FlowInstance flowInstance = flowInstanceFactory.loadFlowInstance(flowInstanceId);
        flowInstance.suspend();
        flowInstanceRepository.save(flowInstance);
        publishDomainEvents(flowInstance);
        
        // 更新缓存
        FlowInstanceDTO dto = FlowInstanceAssembler.toDTO(flowInstance);
        cacheUpdateService.updateFlowInstanceCache(dto);
    }
    
    /**
     * 恢复流程
     */
    @Transactional
    public void resume(Long flowInstanceId) {
        FlowInstance flowInstance = flowInstanceFactory.loadFlowInstance(flowInstanceId);
        flowInstance.resume();
        flowInstanceRepository.save(flowInstance);
        publishDomainEvents(flowInstance);
        
        // 更新缓存
        FlowInstanceDTO dto = FlowInstanceAssembler.toDTO(flowInstance);
        cacheUpdateService.updateFlowInstanceCache(dto);
    }

    // ===================流程对象获取方法==========================

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
     * 加载流程实例
     * 
     * 使用分布式锁防止缓存击穿：当缓存未命中时，只有一个线程查询数据库并更新缓存
     * 高并发情况下才会出现，
     *
     */
    @Transactional(readOnly = true)
    public FlowInstanceDTO getFlowInstance(Long flowInstanceId) {
        // 1. 先从缓存获取
        Optional<FlowInstanceDTO> cached = flowInstanceCacheService.getFlowInstanceById(flowInstanceId);
        if (cached.isPresent()) {
            return cached.get();
        }
        
        // 2. 缓存未命中，使用分布式锁防止缓存击穿
        String lockKey = "flow_instance:" + flowInstanceId;
        FlowInstanceDTO result = distributedLockService.executeWithLock(
            lockKey,
            3, // 等待3秒
            10, // 锁持有10秒
            TimeUnit.SECONDS,
            () -> {
                // 双重检查：再次检查缓存（可能在等待锁期间，其他线程已经更新了缓存）
                Optional<FlowInstanceDTO> cachedAgain = flowInstanceCacheService.getFlowInstanceById(flowInstanceId);
                if (cachedAgain.isPresent()) {
                    return cachedAgain.get();
                }
                
                // 从数据库加载
                FlowInstance flowInstance = flowInstanceFactory.loadFlowInstance(flowInstanceId);
                FlowInstanceDTO dto = FlowInstanceAssembler.toDTO(flowInstance);
                
                // 更新缓存
                cacheUpdateService.updateFlowInstanceCache(dto);
                
                return dto;
            }
        );
        
        // 如果获取锁失败，直接查数据库（降级策略）
        if (result == null) {
            log.warn("获取分布式锁失败，直接查询数据库，流程实例ID: {}", flowInstanceId);
            FlowInstance flowInstance = flowInstanceFactory.loadFlowInstance(flowInstanceId);
            return FlowInstanceAssembler.toDTO(flowInstance);
        }
        
        return result;
    }

    /**
     * 设置流程变量
     */
    @Transactional
    public void setProcessVariable(Long flowInstanceId, String key, Object value) {
        FlowInstance flowInstance = flowInstanceFactory.loadFlowInstance(flowInstanceId);
        flowInstance.setProcessVariable(key, value);
        flowInstanceRepository.save(flowInstance);
        
        // 更新缓存
        FlowInstanceDTO dto = FlowInstanceAssembler.toDTO(flowInstance);
        cacheUpdateService.updateFlowInstanceCache(dto);
    }
    
    /**
     * 获取流程变量
     */
    @Transactional(readOnly = true)
    public Object getProcessVariable(Long flowInstanceId, String key) {
        FlowInstance flowInstance = flowInstanceFactory.loadFlowInstance(flowInstanceId);
        return flowInstance.getProcessVariable(key);
    }
    
    /**
     * 查找节点实例
     */
    private Optional<FlowNodeInstance> findNodeInstance(Long nodeInstanceId) {
        return flowNodeInstanceRepository.findById(nodeInstanceId);
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
}
