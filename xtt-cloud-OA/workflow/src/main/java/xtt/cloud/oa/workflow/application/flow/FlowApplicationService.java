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
import xtt.cloud.oa.workflow.domain.flow.repository.FlowInstanceRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeInstanceRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeRepository;
import xtt.cloud.oa.workflow.domain.flow.service.NodeRoutingService;
import xtt.cloud.oa.workflow.domain.flow.service.ApproverAssignmentService;
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
    private final FlowInstanceApplicationFactory flowInstanceFactory;
    private final CacheUpdateService cacheUpdateService;
    private final FlowInstanceCacheService flowInstanceCacheService;
    private final DistributedLockService distributedLockService;
    
    public FlowApplicationService(
            FlowInstanceRepository flowInstanceRepository,
            FlowNodeRepository flowNodeRepository,
            FlowNodeInstanceRepository flowNodeInstanceRepository,
            NodeRoutingService nodeRoutingService,
            ApproverAssignmentService approverAssignmentService,
            DomainEventPublisher eventPublisher,
            FlowInstanceApplicationFactory flowInstanceFactory,
            CacheUpdateService cacheUpdateService,
            FlowInstanceCacheService flowInstanceCacheService,
            DistributedLockService distributedLockService) {
        this.flowInstanceRepository = flowInstanceRepository;
        this.flowNodeRepository = flowNodeRepository;
        this.flowNodeInstanceRepository = flowNodeInstanceRepository;
        this.nodeRoutingService = nodeRoutingService;
        this.approverAssignmentService = approverAssignmentService;
        this.eventPublisher = eventPublisher;
        this.flowInstanceFactory = flowInstanceFactory;
        this.cacheUpdateService = cacheUpdateService;
        this.flowInstanceCacheService = flowInstanceCacheService;
        this.distributedLockService = distributedLockService;
    }
    
    /**
     * 启动流程
     */
    @Transactional
    public FlowInstanceDTO startFlow(StartFlowCommand command) {
        log.info("启动流程，文档ID: {}, 流程定义ID: {}", command.getDocumentId(), command.getFlowDefId());
        
        // 使用工厂创建并组装流程实例
        FlowInstance flowInstance = flowInstanceFactory.createAndAssemble(command);
        
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
        
        // 2. 验证流程状态
        if (!flowInstance.getStatus().canProceed()) {
            throw new IllegalStateException("流程状态不允许审批，当前状态: " + flowInstance.getStatus());
        }

        // 3. 获取当前节点实例
        FlowNodeInstance nodeInstance = findNodeInstance(command.getNodeInstanceId())
                .orElseThrow(() -> new IllegalArgumentException("节点实例不存在: " + command.getNodeInstanceId()));

        // 4. 验证审批权限
        if (!approverAssignmentService.hasApprovalPermission(
                command.getApproverId(), nodeInstance.getNodeId(), command.getFlowInstanceId())) {
            throw new SecurityException("无权审批此节点");
        }

        // 5. 完结当前节点实例
        nodeInstance.setStatus(FlowNodeInstance.STATUS_COMPLETED);
        nodeInstance.setComments(command.getComments());
        nodeInstance.setHandledAt(java.time.LocalDateTime.now());
        nodeInstance.setUpdatedAt(java.time.LocalDateTime.now());
        flowNodeInstanceRepository.save(nodeInstance);

        // 6. 检查是否可以流转到下一个节点
        FlowNode currentNodeDef = flowNodeRepository.findById(
                        xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowNodeId.of(nodeInstance.getNodeId()))
                .orElseThrow(() -> new IllegalArgumentException("节点不存在: " + nodeInstance.getNodeId()));
        Long flowInstanceId = flowInstance.getId() != null ? flowInstance.getId().getValue() : null;
        if (nodeRoutingService.canMoveToNextNode(currentNodeDef, flowInstanceId)) {
            nodeRoutingService.moveToNextNode(flowInstance, currentNodeDef, nodeInstance);
        } else {
            // 检查流程是否完成
            Map<String, Object> processVariables = flowInstance.getProcessVariables().getAllVariables();
            if (nodeRoutingService.canCompleteFlow(
                    flowInstance.getCurrentNodeId(),
                    flowInstance.getFlowDefId(),
                    flowInstanceId,
                    processVariables)) {
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
    
    // ========== 私有方法 ==========

    /**
     * 加载流程实例的所有节点实例
     */
    private FlowInstance loadFlowNodeInstances(FlowInstance flowInstance) {
        if (flowInstance == null || flowInstance.getId() == null) {
            return flowInstance;
        }
        
        List<FlowNodeInstance> nodeInstances = flowNodeInstanceRepository.findByFlowInstanceId(
                flowInstance.getId().getValue());
        flowInstance.setNodeInstances(nodeInstances);
        
        return flowInstance;
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

}
