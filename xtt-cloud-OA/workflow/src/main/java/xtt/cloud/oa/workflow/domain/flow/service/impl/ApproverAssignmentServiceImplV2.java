package xtt.cloud.oa.workflow.domain.flow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowNodeId;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.NodeStatus;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeInstanceRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeRepository;
import xtt.cloud.oa.workflow.domain.flow.service.ApproverAssignmentService;
import xtt.cloud.oa.workflow.domain.flow.service.strategy.ApproverAssignmentStrategy;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 审批人分配服务实现（策略模式版本）
 * 
 * 使用策略模式消除 switch-case，提高可扩展性
 * 
 * @author xtt
 */
@Service
public class ApproverAssignmentServiceImplV2 implements ApproverAssignmentService {
    
    private static final Logger log = LoggerFactory.getLogger(ApproverAssignmentServiceImplV2.class);
    
    private final FlowNodeRepository flowNodeRepository;
    private final FlowNodeInstanceRepository flowNodeInstanceRepository;
    private final List<ApproverAssignmentStrategy> strategies;
    
    public ApproverAssignmentServiceImplV2(
            FlowNodeRepository flowNodeRepository,
            FlowNodeInstanceRepository flowNodeInstanceRepository,
            List<ApproverAssignmentStrategy> strategies) {
        this.flowNodeRepository = flowNodeRepository;
        this.flowNodeInstanceRepository = flowNodeInstanceRepository;
        // 按优先级排序策略
        this.strategies = strategies.stream()
                .sorted(Comparator.comparingInt(ApproverAssignmentStrategy::getPriority))
                .toList();
    }
    
    @Override
    public List<Approver> assignApprovers(Long nodeId, Long flowDefId, Long flowInstanceId,
                                          Map<String, Object> processVariables) {
        // 1. 获取节点定义
        Optional<FlowNode> nodeOpt = flowNodeRepository.findById(FlowNodeId.of(nodeId));
        if (nodeOpt.isEmpty()) {
            log.warn("节点不存在，节点ID: {}", nodeId);
            return List.of();
        }
        FlowNode node = nodeOpt.get();
        
        // 2. 获取审批人类型和值
        Integer approverType = node.getApproverType();
        String approverValue = node.getApproverValue();
        
        if (approverType == null || !StringUtils.hasText(approverValue)) {
            log.warn("节点未配置审批人，节点ID: {}", nodeId);
            return List.of();
        }
        
        // 3. 使用策略模式分配审批人
        ApproverAssignmentStrategy strategy = strategies.stream()
                .filter(s -> s.supports(approverType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "不支持的审批人类型: " + approverType + ", 节点ID: " + nodeId));
        
        List<Approver> approvers = strategy.assign(approverValue, flowInstanceId, processVariables);
        
        log.info("分配审批人完成，节点ID: {}, 审批人类型: {}, 策略: {}, 分配数量: {}", 
                nodeId, approverType, strategy.getClass().getSimpleName(), approvers.size());
        
        return approvers;
    }
    
    @Override
    public boolean hasApprovalPermission(Long userId, Long nodeId, Long flowInstanceId) {
        if (userId == null || nodeId == null || flowInstanceId == null) {
            return false;
        }
        
        try {
            // 1. 获取节点实例列表
            List<FlowNodeInstance> nodeInstances = 
                    flowNodeInstanceRepository.findByNodeIdAndFlowInstanceId(nodeId, flowInstanceId);
            
            if (nodeInstances.isEmpty()) {
                log.warn("节点实例不存在，节点ID: {}, 流程实例ID: {}", nodeId, flowInstanceId);
                return false;
            }
            
            // 2. 检查用户是否在审批人列表中
            boolean isApprover = nodeInstances.stream()
                    .anyMatch(ni -> {
                        Approver approver = ni.getApprover();
                        return approver != null && userId.equals(approver.getUserId());
                    });
            
            if (!isApprover) {
                log.debug("用户不是该节点的审批人，用户ID: {}, 节点ID: {}, 流程实例ID: {}", 
                        userId, nodeId, flowInstanceId);
                return false;
            }
            
            // 3. 检查节点状态是否允许审批（待处理或处理中）
            boolean canApprove = nodeInstances.stream()
                    .anyMatch(ni -> {
                        Approver approver = ni.getApprover();
                        if (approver == null || !userId.equals(approver.getUserId())) {
                            return false;
                        }
                        NodeStatus status = ni.getStatus();
                        return status != null && status.canHandle();
                    });
            
            if (!canApprove) {
                log.debug("节点状态不允许审批，用户ID: {}, 节点ID: {}, 流程实例ID: {}", 
                        userId, nodeId, flowInstanceId);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("验证审批权限失败，用户ID: {}, 节点ID: {}, 流程实例ID: {}", 
                    userId, nodeId, flowInstanceId, e);
            return false;
        }
    }
}

