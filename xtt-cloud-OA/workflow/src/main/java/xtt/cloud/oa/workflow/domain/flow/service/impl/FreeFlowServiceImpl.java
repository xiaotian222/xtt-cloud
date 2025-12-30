package xtt.cloud.oa.workflow.domain.flow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowAction;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowActionRule;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowActionRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowActionRuleRepository;
import xtt.cloud.oa.workflow.domain.flow.service.ApproverProvider;
import xtt.cloud.oa.workflow.domain.flow.service.FreeFlowService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 自由流服务实现
 * 
 * @author xtt
 */
@Service
public class FreeFlowServiceImpl implements FreeFlowService {
    
    private static final Logger log = LoggerFactory.getLogger(FreeFlowServiceImpl.class);
    
    private final FlowActionRepository flowActionRepository;
    private final FlowActionRuleRepository flowActionRuleRepository;
    private final ApproverProvider approverProvider;
    
    public FreeFlowServiceImpl(
            FlowActionRepository flowActionRepository,
            FlowActionRuleRepository flowActionRuleRepository,
            ApproverProvider approverProvider) {
        this.flowActionRepository = flowActionRepository;
        this.flowActionRuleRepository = flowActionRuleRepository;
        this.approverProvider = approverProvider;
    }
    
    @Override
    public List<FlowAction> getAvailableActions(Integer documentStatus, List<String> userRoles, Long deptId) {
        log.debug("获取可用动作，文档状态: {}, 用户角色: {}, 部门ID: {}", documentStatus, userRoles, deptId);
        
        if (documentStatus == null) {
            return Collections.emptyList();
        }
        
        if (userRoles == null || userRoles.isEmpty()) {
            log.warn("用户角色列表为空，无法获取可用动作");
            return Collections.emptyList();
        }
        
        // 1. 获取所有启用的动作
        List<FlowAction> allActions = flowActionRepository.findAllEnabled();
        if (allActions.isEmpty()) {
            log.debug("没有启用的动作");
            return Collections.emptyList();
        }
        
        // 2. 对每个动作，查找匹配的规则
        List<FlowAction> availableActions = new ArrayList<>();
        for (FlowAction action : allActions) {
            List<FlowActionRule> rules = flowActionRuleRepository.findByActionId(action.getId());
            
            // 检查是否有匹配的规则
            boolean hasMatchingRule = rules.stream()
                    .filter(FlowActionRule::isEnabled)
                    .anyMatch(rule -> 
                            rule.matchesDocumentStatus(documentStatus) &&
                            rule.matchesUserRoles(userRoles) &&
                            rule.matchesDepartment(deptId));
            
            if (hasMatchingRule) {
                availableActions.add(action);
            }
        }
        
        // 3. 按优先级排序（通过规则优先级）
        availableActions.sort((a1, a2) -> {
            List<FlowActionRule> rules1 = flowActionRuleRepository.findByActionId(a1.getId());
            List<FlowActionRule> rules2 = flowActionRuleRepository.findByActionId(a2.getId());
            
            Integer priority1 = rules1.stream()
                    .filter(FlowActionRule::isEnabled)
                    .filter(r -> r.matchesDocumentStatus(documentStatus) && 
                                r.matchesUserRoles(userRoles) && 
                                r.matchesDepartment(deptId))
                    .map(FlowActionRule::getPriority)
                    .max(Integer::compareTo)
                    .orElse(0);
            
            Integer priority2 = rules2.stream()
                    .filter(FlowActionRule::isEnabled)
                    .filter(r -> r.matchesDocumentStatus(documentStatus) && 
                                r.matchesUserRoles(userRoles) && 
                                r.matchesDepartment(deptId))
                    .map(FlowActionRule::getPriority)
                    .max(Integer::compareTo)
                    .orElse(0);
            
            return priority2.compareTo(priority1); // 降序
        });
        
        log.debug("找到 {} 个可用动作", availableActions.size());
        return availableActions;
    }
    
    @Override
    public List<Approver> getAvailableApprovers(
            Long actionId, 
            List<String> userRoles, 
            Long deptId,
            Map<String, Object> processVariables) {
        
        log.debug("获取可选审批人，动作ID: {}, 用户角色: {}, 部门ID: {}", actionId, userRoles, deptId);
        
        if (actionId == null) {
            return Collections.emptyList();
        }
        
        // 1. 获取动作信息
        FlowAction action = flowActionRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("动作不存在: " + actionId));
        
        if (!action.isEnabled()) {
            log.warn("动作未启用，动作ID: {}", actionId);
            return Collections.emptyList();
        }
        
        // 2. 根据动作类型决定审批人范围
        List<Approver> approvers = new ArrayList<>();
        
        switch (action.getActionType()) {
            case FlowAction.TYPE_UNIT_HANDLE:
                // 单位内办理：获取当前部门的所有用户
                if (deptId != null) {
                    List<Long> userIds = approverProvider.getUserIdsByDepartmentId(deptId);
                    approvers = approverProvider.convertToApprovers(userIds);
                }
                break;
                
            case FlowAction.TYPE_REVIEW:
                // 核稿：获取具有核稿角色的用户
                // TODO: 根据配置获取核稿角色ID，这里暂时返回空列表
                // Long reviewRoleId = getReviewRoleId(); // 需要从配置获取
                // if (reviewRoleId != null) {
                //     List<Long> userIds = approverProvider.getUserIdsByRoleId(reviewRoleId);
                //     approvers = approverProvider.convertToApprovers(userIds);
                // }
                approvers = Collections.emptyList();
                break;
                
            case FlowAction.TYPE_EXTERNAL:
                // 转外单位办理：获取所有部门的用户（排除当前部门）
                // TODO: 实现获取其他部门用户
                // 这里需要调用平台服务获取所有部门，然后排除当前部门
                approvers = Collections.emptyList();
                break;
                
            case FlowAction.TYPE_RETURN:
                // 返回：获取流程发起人
                if (processVariables != null && processVariables.containsKey("initiatorId")) {
                    Long initiatorId = (Long) processVariables.get("initiatorId");
                    approvers = approverProvider.convertToApprovers(List.of(initiatorId));
                }
                break;
                
            default:
                log.warn("未知的动作类型: {}", action.getActionType());
                break;
        }
        
        log.debug("找到 {} 个可选审批人", approvers.size());
        return approvers;
    }
}

