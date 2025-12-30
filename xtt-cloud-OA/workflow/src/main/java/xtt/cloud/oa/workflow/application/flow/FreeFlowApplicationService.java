package xtt.cloud.oa.workflow.application.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.workflow.application.flow.command.SendFreeFlowCommand;
import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowAction;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;
import xtt.cloud.oa.workflow.domain.flow.repository.DocumentRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowInstanceRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeInstanceRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeRepository;
import xtt.cloud.oa.workflow.domain.flow.service.FreeFlowService;
import xtt.cloud.oa.workflow.infrastructure.external.client.PlatformFeignClient;
import xtt.cloud.oa.workflow.infrastructure.messaging.event.DomainEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自由流应用服务
 * 
 * @author xtt
 */
@Service
public class FreeFlowApplicationService {
    
    private static final Logger log = LoggerFactory.getLogger(FreeFlowApplicationService.class);
    
    private final FreeFlowService freeFlowService;
    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowNodeInstanceRepository flowNodeInstanceRepository;
    private final FlowNodeRepository flowNodeRepository;
    private final DocumentRepository documentRepository;
    private final PlatformFeignClient platformFeignClient;
    private final DomainEventPublisher eventPublisher;
    
    public FreeFlowApplicationService(
            FreeFlowService freeFlowService,
            FlowInstanceRepository flowInstanceRepository,
            FlowNodeInstanceRepository flowNodeInstanceRepository,
            FlowNodeRepository flowNodeRepository,
            DocumentRepository documentRepository,
            PlatformFeignClient platformFeignClient,
            DomainEventPublisher eventPublisher) {
        this.freeFlowService = freeFlowService;
        this.flowInstanceRepository = flowInstanceRepository;
        this.flowNodeInstanceRepository = flowNodeInstanceRepository;
        this.flowNodeRepository = flowNodeRepository;
        this.documentRepository = documentRepository;
        this.platformFeignClient = platformFeignClient;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * 发送自由流
     * 
     * @param command 发送命令
     */
    @Transactional
    public void sendFreeFlow(SendFreeFlowCommand command) {
        log.info("发送自由流，流程实例ID: {}, 节点实例ID: {}, 动作ID: {}, 操作人ID: {}", 
                command.getFlowInstanceId(), command.getNodeInstanceId(), 
                command.getActionId(), command.getOperatorId());
        
        // 1. 加载流程实例
        FlowInstance flowInstance = flowInstanceRepository.findById(
                xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowInstanceId.of(
                        command.getFlowInstanceId()))
                .orElseThrow(() -> new IllegalArgumentException("流程实例不存在: " + command.getFlowInstanceId()));
        
        // 2. 验证流程状态
        if (!flowInstance.getStatus().canProceed()) {
            throw new IllegalStateException("流程状态不允许发送，当前状态: " + flowInstance.getStatus());
        }
        
        // 2.1 如果是固定流，验证是否已开启自由流转
        if (flowInstance.getFlowMode().isFixed() && !flowInstance.isInFreeFlow()) {
            throw new IllegalStateException("固定流中必须先开启自由流转才能发送自由流");
        }
        
        // 3. 获取当前节点实例
        FlowNodeInstance currentNodeInstance = flowNodeInstanceRepository.findById(
                command.getNodeInstanceId())
                .orElseThrow(() -> new IllegalArgumentException("节点实例不存在: " + command.getNodeInstanceId()));
        
        // 4. 验证节点状态
        if (!currentNodeInstance.getStatus().canHandle()) {
            throw new IllegalStateException("节点状态不允许发送，当前状态: " + currentNodeInstance.getStatus());
        }
        
        // 5. 验证操作人权限
        if (!currentNodeInstance.getApprover().getUserId().equals(command.getOperatorId())) {
            throw new SecurityException("无权操作此节点");
        }
        
        // 6. 获取操作人信息
        List<String> operatorRoles = getUserRoles(command.getOperatorId());
        Long operatorDeptId = getUserDeptId(command.getOperatorId());
        
        // 7. 获取动作信息
        FlowAction action = freeFlowService.getAvailableActions(
                getDocumentStatus(flowInstance),
                operatorRoles,
                operatorDeptId)
                .stream()
                .filter(a -> a.getId().equals(command.getActionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("动作不可用: " + command.getActionId()));
        
        // 8. 获取审批人
        List<Approver> approvers = getApproversFromCommand(command, operatorRoles, operatorDeptId);
        if (approvers.isEmpty()) {
            throw new IllegalArgumentException("必须选择至少一个审批人");
        }
        
        // 9. 完成当前节点实例
        currentNodeInstance.complete(command.getComments());
        flowNodeInstanceRepository.save(currentNodeInstance);
        
        // 10. 创建新的自由流节点实例
        // 注意：自由流不需要预定义节点，直接创建节点实例
        for (Approver approver : approvers) {
            FlowNodeInstance newNodeInstance = FlowNodeInstance.create(
                    flowInstance.getId() != null ? flowInstance.getId().getValue() : null,
                    null, // 自由流节点没有预定义的节点ID
                    approver
            );
            newNodeInstance.startProcessing();
            flowNodeInstanceRepository.save(newNodeInstance);
            
            // TODO: 保存自由流节点实例扩展信息（动作、选择的审批人等）
            // FreeFlowNodeInstance freeFlowNodeInstance = ...
        }
        
        // 11. 更新流程变量（如果有）
        if (command.getProcessVariables() != null && !command.getProcessVariables().isEmpty()) {
            command.getProcessVariables().forEach(flowInstance::setProcessVariable);
        }
        
        // 12. 保存流程实例
        flowInstanceRepository.save(flowInstance);
        
        // 13. 发布领域事件
        publishDomainEvents(flowInstance);
        
        log.info("自由流发送成功，流程实例ID: {}, 创建了 {} 个节点实例", 
                command.getFlowInstanceId(), approvers.size());
    }
    
    /**
     * 获取文档状态
     */
    private Integer getDocumentStatus(FlowInstance flowInstance) {
        if (flowInstance.getDocumentId() == null) {
            return 1; // 默认审核中
        }
        
        // 从文档仓储获取文档状态
        return documentRepository.findById(flowInstance.getDocumentId())
                .map(doc -> doc.getStatus())
                .orElse(1); // 默认审核中
    }
    
    /**
     * 获取用户角色列表
     */
    private List<String> getUserRoles(Long userId) {
        if (userId == null) {
            return List.of();
        }
        
        try {
            // 从平台服务获取用户信息
            xtt.cloud.oa.common.dto.UserInfoDto user = platformFeignClient.getUserById(userId);
            if (user != null && user.getRoles() != null) {
                // 从角色信息中提取角色编码或ID
                return user.getRoles().stream()
                        .map(role -> role.getCode() != null ? role.getCode() : role.getId().toString())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("获取用户角色失败，用户ID: {}", userId, e);
        }
        
        return List.of();
    }
    
    /**
     * 获取用户部门ID
     */
    private Long getUserDeptId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            // 从平台服务获取用户信息
            xtt.cloud.oa.common.dto.UserInfoDto user = platformFeignClient.getUserById(userId);
            if (user != null && user.getDepartments() != null && !user.getDepartments().isEmpty()) {
                // 返回第一个部门ID（用户可能有多个部门）
                return user.getDepartments().iterator().next().getId();
            }
        } catch (Exception e) {
            log.error("获取用户部门失败，用户ID: {}", userId, e);
        }
        
        return null;
    }
    
    /**
     * 从命令中获取审批人列表
     */
    private List<Approver> getApproversFromCommand(
            SendFreeFlowCommand command, 
            List<String> operatorRoles, 
            Long operatorDeptId) {
        if (command.getApproverIds() != null && !command.getApproverIds().isEmpty()) {
            // 获取所有可选审批人
            List<Approver> availableApprovers = freeFlowService.getAvailableApprovers(
                    command.getActionId(),
                    operatorRoles,
                    operatorDeptId,
                    command.getProcessVariables());
            
            // 过滤出命令中指定的审批人
            return availableApprovers.stream()
                    .filter(a -> command.getApproverIds().contains(a.getUserId()))
                    .collect(Collectors.toList());
        }
        return List.of();
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

