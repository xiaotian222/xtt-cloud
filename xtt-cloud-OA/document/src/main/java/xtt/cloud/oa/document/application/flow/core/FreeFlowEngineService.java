package xtt.cloud.oa.document.application.flow.core;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import xtt.cloud.oa.common.BusinessException;
import xtt.cloud.oa.document.domain.entity.flow.Document;
import xtt.cloud.oa.document.domain.entity.flow.ApproverScope;
import xtt.cloud.oa.document.domain.entity.flow.FlowAction;
import xtt.cloud.oa.document.domain.entity.flow.FlowActionRule;
import xtt.cloud.oa.document.domain.entity.flow.FlowInstance;
import xtt.cloud.oa.document.domain.entity.flow.FlowNode;
import xtt.cloud.oa.document.domain.entity.flow.FlowNodeInstance;
import xtt.cloud.oa.document.domain.entity.flow.FreeFlowNodeInstance;
import xtt.cloud.oa.document.domain.mapper.flow.DocumentMapper;
import xtt.cloud.oa.document.domain.mapper.flow.ApproverScopeMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FlowActionMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FlowActionRuleMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FlowInstanceMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FlowNodeInstanceMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FlowNodeMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FreeFlowNodeInstanceMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自由流引擎服务
 * 处理动态流程的核心逻辑
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Service
public class FreeFlowEngineService {
    
    private static final Logger log = LoggerFactory.getLogger(FreeFlowEngineService.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final FlowActionMapper flowActionMapper;
    
    private final FlowActionRuleMapper flowActionRuleMapper;
    
    private final ApproverScopeMapper approverScopeMapper;
    
    private final FreeFlowNodeInstanceMapper freeFlowNodeInstanceMapper;
    
    private final FlowNodeInstanceMapper flowNodeInstanceMapper;
    
    private final FlowNodeMapper flowNodeMapper;
    
    private final FlowInstanceMapper flowInstanceMapper;
    
    private final DocumentMapper documentMapper;
    
    private final DocumentService documentService;

    public FreeFlowEngineService(FlowActionMapper flowActionMapper, FlowActionRuleMapper flowActionRuleMapper, ApproverScopeMapper approverScopeMapper, FreeFlowNodeInstanceMapper freeFlowNodeInstanceMapper, FlowNodeInstanceMapper flowNodeInstanceMapper, FlowNodeMapper flowNodeMapper, FlowInstanceMapper flowInstanceMapper, DocumentMapper documentMapper, DocumentService documentService) {
        this.flowActionMapper = flowActionMapper;
        this.flowActionRuleMapper = flowActionRuleMapper;
        this.approverScopeMapper = approverScopeMapper;
        this.freeFlowNodeInstanceMapper = freeFlowNodeInstanceMapper;
        this.flowNodeInstanceMapper = flowNodeInstanceMapper;
        this.flowNodeMapper = flowNodeMapper;
        this.flowInstanceMapper = flowInstanceMapper;
        this.documentMapper = documentMapper;
        this.documentService = documentService;
    }

    /**
     * 获取当前用户可用的发送动作
     * 根据文件状态和用户角色动态计算
     */
    public List<FlowAction> getAvailableActions(Long documentId, Long userId) {
        log.debug("获取可用发送动作，文档ID: {}, 用户ID: {}", documentId, userId);
        
        try {
            // 1. 获取文件信息
            Document document = documentMapper.selectById(documentId);
            if (document == null) {
                log.warn("文档不存在，ID: {}", documentId);
                throw new BusinessException("文档不存在");
            }
            
            // 2. 获取用户角色（这里需要调用用户服务，暂时使用模拟数据）
            // TODO: 集成用户服务获取用户角色
            List<String> userRoles = getUserRoles(userId);
            
            // 3. 查询匹配的动作规则
            List<FlowActionRule> rules = matchActionRules(
                document.getStatus(),
                userRoles,
                null // 暂时不限制部门
            );
            
            if (rules.isEmpty()) {
                log.debug("未找到匹配的动作规则，文档ID: {}, 用户ID: {}", documentId, userId);
                return Collections.emptyList();
            }
            
            // 4. 获取对应的动作
            List<Long> actionIds = rules.stream()
                .map(FlowActionRule::getActionId)
                .distinct()
                .collect(Collectors.toList());
            
            List<FlowAction> actions = flowActionMapper.selectBatchIds(actionIds);
            
            // 5. 过滤启用的动作
            actions = actions.stream()
                .filter(action -> action.getEnabled() == 1)
                .collect(Collectors.toList());
            
            log.debug("找到 {} 个可用发送动作，文档ID: {}, 用户ID: {}", actions.size(), documentId, userId);
            
            return actions;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取可用发送动作失败，文档ID: {}, 用户ID: {}", documentId, userId, e);
            throw new BusinessException("获取可用发送动作失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取发送动作对应的审批人选择范围
     */
    public ApproverScope getApproverScope(Long actionId) {
        log.debug("获取审批人选择范围，动作ID: {}", actionId);
        
        try {
            ApproverScope scope = approverScopeMapper.selectOne(
                new LambdaQueryWrapper<ApproverScope>()
                    .eq(ApproverScope::getActionId, actionId)
            );
            
            if (scope == null) {
                log.warn("未找到审批人选择范围，动作ID: {}，使用默认范围", actionId);
                // 如果没有配置，返回默认范围（允许所有部门和人员）
                scope = createDefaultScope(actionId);
            }
            
            return scope;
        } catch (Exception e) {
            log.error("获取审批人选择范围失败，动作ID: {}", actionId, e);
            throw new BusinessException("获取审批人选择范围失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行发送动作
     * 创建新的节点实例并分配审批人
     */
    @Transactional
    public FlowNodeInstance executeAction(
            Long currentNodeInstanceId,
            Long actionId,
            List<Long> selectedDeptIds,
            List<Long> selectedUserIds,
            String comment,
            Long operatorId) {
        
        log.info("执行发送动作，当前节点实例ID: {}, 动作ID: {}, 操作人ID: {}", 
                currentNodeInstanceId, actionId, operatorId);
        
        try {
            // 1. 验证当前节点
            FlowNodeInstance currentNode = flowNodeInstanceMapper.selectById(currentNodeInstanceId);
            if (currentNode == null) {
                log.warn("节点实例不存在，ID: {}", currentNodeInstanceId);
                throw new BusinessException("节点实例不存在");
            }
            
            if (!currentNode.getApproverId().equals(operatorId)) {
                log.warn("无权操作此节点，节点实例ID: {}, 操作人ID: {}", currentNodeInstanceId, operatorId);
                throw new BusinessException("无权操作此节点");
            }
            
            if (currentNode.getStatus() != FlowNodeInstance.STATUS_PENDING) {
                log.warn("节点状态不正确，节点实例ID: {}, 状态: {}", currentNodeInstanceId, currentNode.getStatus());
                throw new BusinessException("节点状态不正确");
            }
            
            // 2. 获取流程实例和文档信息
            FlowInstance flowInstance = flowInstanceMapper.selectById(currentNode.getFlowInstanceId());
            Document document = documentMapper.selectById(flowInstance.getDocumentId());
            
            // 3. 验证动作可用性
            if (!isActionAvailable(actionId, document.getId(), operatorId)) {
                log.warn("发送动作不可用，动作ID: {}, 文档ID: {}, 用户ID: {}", actionId, document.getId(), operatorId);
                throw new BusinessException("该发送动作不可用");
            }
            
            // 4. 验证审批人范围
            ApproverScope scope = getApproverScope(actionId);
            if (!validateApproverScope(scope, selectedDeptIds, selectedUserIds)) {
                log.warn("选择的审批人不在允许范围内，动作ID: {}", actionId);
                throw new BusinessException("选择的审批人不在允许范围内");
            }
            
            // 5. 根据范围类型分配审批人
            List<Long> approverIds = assignApprovers(scope, selectedDeptIds, selectedUserIds);
            if (approverIds.isEmpty()) {
                log.warn("未找到可分配的审批人，动作ID: {}", actionId);
                throw new BusinessException("未找到可分配的审批人");
            }
            
            // 6. 获取动作信息
            FlowAction action = flowActionMapper.selectById(actionId);
            if (action == null) {
                log.warn("发送动作不存在，ID: {}", actionId);
                throw new BusinessException("发送动作不存在");
            }
            
            // 7. 处理返回操作（特殊处理）
            if (action.getActionType() == FlowAction.TYPE_RETURN) {
                return handleReturnAction(currentNode, comment, operatorId);
            }
            
            // 8. 创建自由流节点定义（动态创建）
            FlowNode freeNode = createFreeFlowNode(action, flowInstance);
            
            // 9. 为每个审批人创建节点实例
            List<FlowNodeInstance> nodeInstances = new ArrayList<>();
            for (Long approverId : approverIds) {
                FlowNodeInstance nodeInstance = new FlowNodeInstance();
                nodeInstance.setFlowInstanceId(flowInstance.getId());
                nodeInstance.setNodeId(freeNode.getId());
                nodeInstance.setApproverId(approverId);
                nodeInstance.setStatus(FlowNodeInstance.STATUS_PENDING);
                nodeInstance.setCreatedAt(LocalDateTime.now());
                nodeInstance.setUpdatedAt(LocalDateTime.now());
                flowNodeInstanceMapper.insert(nodeInstance);
                nodeInstances.add(nodeInstance);
                
                // 创建自由流节点扩展信息
                FreeFlowNodeInstance freeNodeInstance = new FreeFlowNodeInstance();
                freeNodeInstance.setNodeInstanceId(nodeInstance.getId());
                freeNodeInstance.setActionId(actionId);
                freeNodeInstance.setActionName(action.getActionName());
                freeNodeInstance.setSelectedDeptIds(convertToJson(selectedDeptIds));
                freeNodeInstance.setSelectedUserIds(convertToJson(selectedUserIds));
                freeNodeInstance.setCustomComment(comment);
                freeNodeInstance.setCreatedAt(LocalDateTime.now());
                freeNodeInstance.setUpdatedAt(LocalDateTime.now());
                freeFlowNodeInstanceMapper.insert(freeNodeInstance);
                
                log.debug("创建节点实例成功，ID: {}, 审批人ID: {}", nodeInstance.getId(), approverId);
            }
            
            // 10. 更新当前节点实例状态为"已完成"
            currentNode.setStatus(FlowNodeInstance.STATUS_COMPLETED);
            currentNode.setComments(comment);
            currentNode.setHandledAt(LocalDateTime.now());
            currentNode.setUpdatedAt(LocalDateTime.now());
            flowNodeInstanceMapper.updateById(currentNode);
            
            // 11. 更新流程实例当前节点
            flowInstance.setCurrentNodeId(freeNode.getId());
            flowInstance.setUpdatedAt(LocalDateTime.now());
            flowInstanceMapper.updateById(flowInstance);
            
            log.info("执行发送动作成功，创建了 {} 个节点实例，动作: {}", nodeInstances.size(), action.getActionName());
            
            return nodeInstances.get(0);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("执行发送动作失败，节点实例ID: {}, 动作ID: {}", currentNodeInstanceId, actionId, e);
            throw new BusinessException("执行发送动作失败: " + e.getMessage());
        }
    }
    
    /**
     * 匹配动作规则
     */
    private List<FlowActionRule> matchActionRules(
            Integer documentStatus,
            List<String> userRoles,
            Long deptId) {
        
        LambdaQueryWrapper<FlowActionRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlowActionRule::getDocumentStatus, documentStatus)
               .eq(FlowActionRule::getEnabled, 1);
        
        // 角色匹配（支持多个角色，逗号分隔，*表示所有角色）
        if (userRoles != null && !userRoles.isEmpty()) {
            wrapper.and(w -> {
                // 检查是否有通配符规则
                w.like(FlowActionRule::getUserRole, "*").or();
                // 检查每个角色
                for (String role : userRoles) {
                    w.like(FlowActionRule::getUserRole, role).or();
                }
            });
        }
        
        // 部门匹配（可选）
        if (deptId != null) {
            wrapper.and(w -> w.eq(FlowActionRule::getDeptId, deptId).or().isNull(FlowActionRule::getDeptId));
        }
        
        wrapper.orderByDesc(FlowActionRule::getPriority);
        
        return flowActionRuleMapper.selectList(wrapper);
    }
    
    /**
     * 验证发送动作是否可用
     */
    private boolean isActionAvailable(Long actionId, Long documentId, Long userId) {
        Document document = documentMapper.selectById(documentId);
        List<String> userRoles = getUserRoles(userId);
        
        List<FlowActionRule> rules = matchActionRules(document.getStatus(), userRoles, null);
        return rules.stream()
            .anyMatch(rule -> rule.getActionId().equals(actionId));
    }
    
    /**
     * 验证选择的审批人是否在范围内
     */
    private boolean validateApproverScope(
            ApproverScope scope,
            List<Long> selectedDeptIds,
            List<Long> selectedUserIds) {
        
        // 如果允许自定义，直接通过
        if (scope.getAllowCustom() != null && scope.getAllowCustom() == 1) {
            return true;
        }
        
        switch (scope.getScopeType()) {
            case ApproverScope.SCOPE_TYPE_DEPT:
                // 验证部门是否在允许范围内
                if (StringUtils.hasText(scope.getDeptIds())) {
                    List<Long> allowedDeptIds = parseJsonToList(scope.getDeptIds(), Long.class);
                    if (allowedDeptIds != null && !allowedDeptIds.isEmpty()) {
                        return allowedDeptIds.containsAll(selectedDeptIds != null ? selectedDeptIds : Collections.emptyList());
                    }
                }
                return true; // 如果没有限制，允许所有部门
                
            case ApproverScope.SCOPE_TYPE_USER:
                // 验证人员是否在允许范围内
                if (StringUtils.hasText(scope.getUserIds())) {
                    List<Long> allowedUserIds = parseJsonToList(scope.getUserIds(), Long.class);
                    if (allowedUserIds != null && !allowedUserIds.isEmpty()) {
                        return allowedUserIds.containsAll(selectedUserIds != null ? selectedUserIds : Collections.emptyList());
                    }
                }
                return true;
                
            case ApproverScope.SCOPE_TYPE_DEPT_USER:
                // 验证部门和人员组合
                boolean deptValid = true;
                boolean userValid = true;
                
                if (StringUtils.hasText(scope.getDeptIds())) {
                    List<Long> allowedDeptIds = parseJsonToList(scope.getDeptIds(), Long.class);
                    if (allowedDeptIds != null && !allowedDeptIds.isEmpty()) {
                        deptValid = allowedDeptIds.containsAll(selectedDeptIds != null ? selectedDeptIds : Collections.emptyList());
                    }
                }
                
                if (StringUtils.hasText(scope.getUserIds())) {
                    List<Long> allowedUserIds = parseJsonToList(scope.getUserIds(), Long.class);
                    if (allowedUserIds != null && !allowedUserIds.isEmpty()) {
                        userValid = allowedUserIds.containsAll(selectedUserIds != null ? selectedUserIds : Collections.emptyList());
                    }
                }
                
                return deptValid && userValid;
                
            default:
                return false;
        }
    }
    
    /**
     * 根据范围类型分配审批人
     */
    private List<Long> assignApprovers(
            ApproverScope scope,
            List<Long> selectedDeptIds,
            List<Long> selectedUserIds) {
        
        List<Long> approverIds = new ArrayList<>();
        
        switch (scope.getScopeType()) {
            case ApproverScope.SCOPE_TYPE_DEPT:
                // 部门类型：获取部门负责人或部门内所有人员
                if (selectedDeptIds != null && !selectedDeptIds.isEmpty()) {
                    for (Long deptId : selectedDeptIds) {
                        // TODO: 调用用户服务获取部门负责人
                        Long deptLeaderId = getDeptLeaderId(deptId);
                        if (deptLeaderId != null) {
                            approverIds.add(deptLeaderId);
                        } else {
                            // 如果没有负责人，获取部门内所有人员
                            List<Long> deptUserIds = getUserIdsByDeptId(deptId);
                            approverIds.addAll(deptUserIds);
                        }
                    }
                }
                break;
                
            case ApproverScope.SCOPE_TYPE_USER:
                // 人员类型：直接使用选择的人员
                if (selectedUserIds != null && !selectedUserIds.isEmpty()) {
                    approverIds.addAll(selectedUserIds);
                }
                break;
                
            case ApproverScope.SCOPE_TYPE_DEPT_USER:
                // 部门+人员类型：先选择部门，再从部门中选择人员
                if (selectedDeptIds != null && !selectedDeptIds.isEmpty()) {
                    for (Long deptId : selectedDeptIds) {
                        if (selectedUserIds != null && !selectedUserIds.isEmpty()) {
                            // 从该部门中选择指定人员
                            List<Long> deptUserIds = getUserIdsByDeptId(deptId);
                            List<Long> validUserIds = selectedUserIds.stream()
                                .filter(deptUserIds::contains)
                                .collect(Collectors.toList());
                            approverIds.addAll(validUserIds);
                        } else {
                            // 没有指定人员，获取部门负责人
                            Long deptLeaderId = getDeptLeaderId(deptId);
                            if (deptLeaderId != null) {
                                approverIds.add(deptLeaderId);
                            }
                        }
                    }
                }
                break;
        }
        
        return approverIds.stream().distinct().collect(Collectors.toList());
    }
    
    /**
     * 创建自由流节点定义（动态创建）
     */
    private FlowNode createFreeFlowNode(FlowAction action, FlowInstance flowInstance) {
        FlowNode node = new FlowNode();
        node.setFlowDefId(flowInstance.getFlowDefId());
        node.setNodeName(action.getActionName());
        node.setNodeType(FlowNode.NODE_TYPE_FREE_FLOW);
        node.setApproverType(FlowNode.APPROVER_TYPE_USER);
        node.setOrderNum(9999); // 使用特殊顺序号标识自由流节点
        node.setParallelMode(FlowNode.PARALLEL_MODE_SERIAL);
        node.setIsFreeFlow(1);
        node.setAllowFreeFlow(1);
        node.setRequired(1);
        node.setCreatedAt(LocalDateTime.now());
        node.setUpdatedAt(LocalDateTime.now());
        
        flowNodeMapper.insert(node);
        
        log.debug("创建自由流节点定义成功，ID: {}, 名称: {}", node.getId(), node.getNodeName());
        
        return node;
    }
    
    /**
     * 处理返回操作
     */
    private FlowNodeInstance handleReturnAction(
            FlowNodeInstance currentNode,
            String comment,
            Long operatorId) {
        
        log.info("处理返回操作，节点实例ID: {}", currentNode.getId());
        
        FlowInstance flowInstance = flowInstanceMapper.selectById(currentNode.getFlowInstanceId());
        
        // 查找上一个节点实例
        FlowNodeInstance previousNode = findPreviousNodeInstance(
            flowInstance.getId(),
            currentNode.getId()
        );
        
        Long returnToUserId;
        if (previousNode == null) {
            // 没有上一个节点，退回给发起人
            returnToUserId = getDocumentCreatorId(flowInstance.getDocumentId());
            log.debug("退回给文档发起人，用户ID: {}", returnToUserId);
        } else {
            // 退回给上一个节点
            returnToUserId = previousNode.getApproverId();
            log.debug("退回给上一节点审批人，用户ID: {}", returnToUserId);
        }
        
        // 创建返回节点实例
        FlowAction returnAction = flowActionMapper.selectOne(
            new LambdaQueryWrapper<FlowAction>()
                .eq(FlowAction::getActionType, FlowAction.TYPE_RETURN)
        );
        
        if (returnAction == null) {
            throw new BusinessException("未找到返回动作定义");
        }
        
        FlowNode returnNode = createFreeFlowNode(returnAction, flowInstance);
        
        FlowNodeInstance returnNodeInstance = new FlowNodeInstance();
        returnNodeInstance.setFlowInstanceId(flowInstance.getId());
        returnNodeInstance.setNodeId(returnNode.getId());
        returnNodeInstance.setApproverId(returnToUserId);
        returnNodeInstance.setStatus(FlowNodeInstance.STATUS_PENDING);
        returnNodeInstance.setCreatedAt(LocalDateTime.now());
        returnNodeInstance.setUpdatedAt(LocalDateTime.now());
        flowNodeInstanceMapper.insert(returnNodeInstance);
        
        // 创建自由流节点扩展信息
        FreeFlowNodeInstance freeNodeInstance = new FreeFlowNodeInstance();
        freeNodeInstance.setNodeInstanceId(returnNodeInstance.getId());
        freeNodeInstance.setActionId(returnAction.getId());
        freeNodeInstance.setActionName(returnAction.getActionName());
        freeNodeInstance.setCustomComment(comment);
        freeNodeInstance.setCreatedAt(LocalDateTime.now());
        freeNodeInstance.setUpdatedAt(LocalDateTime.now());
        freeFlowNodeInstanceMapper.insert(freeNodeInstance);
        
        // 更新当前节点状态
        currentNode.setStatus(FlowNodeInstance.STATUS_COMPLETED);
        currentNode.setComments(comment);
        currentNode.setHandledAt(LocalDateTime.now());
        currentNode.setUpdatedAt(LocalDateTime.now());
        flowNodeInstanceMapper.updateById(currentNode);
        
        // 更新流程实例当前节点
        flowInstance.setCurrentNodeId(returnNode.getId());
        flowInstance.setUpdatedAt(LocalDateTime.now());
        flowInstanceMapper.updateById(flowInstance);
        
        log.info("返回操作处理成功，创建返回节点实例，ID: {}", returnNodeInstance.getId());
        
        return returnNodeInstance;
    }
    
    /**
     * 查找上一个节点实例
     */
    private FlowNodeInstance findPreviousNodeInstance(Long flowInstanceId, Long currentNodeInstanceId) {
        FlowNodeInstance currentNode = flowNodeInstanceMapper.selectById(currentNodeInstanceId);
        
        // 查找同一流程实例中，创建时间早于当前节点的节点实例
        List<FlowNodeInstance> previousInstances = flowNodeInstanceMapper.selectList(
            new LambdaQueryWrapper<FlowNodeInstance>()
                .eq(FlowNodeInstance::getFlowInstanceId, flowInstanceId)
                .lt(FlowNodeInstance::getCreatedAt, currentNode.getCreatedAt())
                .orderByDesc(FlowNodeInstance::getCreatedAt)
        );
        
        return previousInstances.isEmpty() ? null : previousInstances.get(0);
    }
    
    /**
     * 获取文档创建人ID
     */
    private Long getDocumentCreatorId(Long documentId) {
        Document document = documentMapper.selectById(documentId);
        return document != null ? document.getCreatorId() : null;
    }
    
    /**
     * 创建默认审批人范围
     */
    private ApproverScope createDefaultScope(Long actionId) {
        ApproverScope scope = new ApproverScope();
        scope.setActionId(actionId);
        scope.setScopeType(ApproverScope.SCOPE_TYPE_DEPT_USER);
        scope.setAllowCustom(1); // 允许自定义选择
        return scope;
    }
    

    
    /**
     * 获取用户角色（TODO: 集成用户服务）
     */
    private List<String> getUserRoles(Long userId) {
        // TODO: 调用用户服务获取用户角色
        // 暂时返回默认角色
        return Arrays.asList("USER");
    }
    
    /**
     * 获取部门负责人ID（TODO: 集成用户服务）
     */
    private Long getDeptLeaderId(Long deptId) {
        // TODO: 调用用户服务获取部门负责人
        return null;
    }
    
    /**
     * 根据部门ID获取用户ID列表（TODO: 集成用户服务）
     */
    private List<Long> getUserIdsByDeptId(Long deptId) {
        // TODO: 调用用户服务获取部门内所有人员
        return Collections.emptyList();
    }
    
    /**
     * 将列表转换为JSON字符串
     */
    private String convertToJson(List<Long> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.error("JSON序列化失败", e);
            return "[]";
        }
    }
    
    /**
     * 将JSON字符串解析为列表
     */
    private <T> List<T> parseJsonToList(String json, Class<T> clazz) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<T>>() {});
        } catch (Exception e) {
            log.error("JSON反序列化失败: {}", json, e);
            return Collections.emptyList();
        }
    }
    
}

