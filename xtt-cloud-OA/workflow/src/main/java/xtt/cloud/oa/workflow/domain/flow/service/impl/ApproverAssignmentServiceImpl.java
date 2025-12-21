package xtt.cloud.oa.workflow.domain.flow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.NodeStatus;
import xtt.cloud.oa.workflow.domain.flow.service.ApproverAssignmentService;
import xtt.cloud.oa.workflow.infrastructure.external.PlatformUserServiceAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 审批人分配服务实现
 * 
 * @author xtt
 */
@Service
public class ApproverAssignmentServiceImpl implements ApproverAssignmentService {
    
    private static final Logger log = LoggerFactory.getLogger(ApproverAssignmentServiceImpl.class);
    
    private final FlowNodeRepository flowNodeRepository;
    private final FlowNodeInstanceRepository flowNodeInstanceRepository;
    private final PlatformUserServiceAdapter platformUserServiceAdapter;
    
    public ApproverAssignmentServiceImpl(
            FlowNodeRepository flowNodeRepository,
            FlowNodeInstanceRepository flowNodeInstanceRepository,
            PlatformUserServiceAdapter platformUserServiceAdapter) {
        this.flowNodeRepository = flowNodeRepository;
        this.flowNodeInstanceRepository = flowNodeInstanceRepository;
        this.platformUserServiceAdapter = platformUserServiceAdapter;
    }
    
    @Override
    public List<Approver> assignApprovers(Long nodeId, Long flowDefId, Long flowInstanceId,
                                          Map<String, Object> processVariables) {
        List<Approver> approvers = new ArrayList<>();
        
        // 1. 获取节点定义
        FlowNode node = flowNodeRepository.findById(nodeId);
        if (node == null) {
            log.warn("节点不存在，节点ID: {}", nodeId);
            return approvers;
        }
        
        // 2. 获取审批人类型和值
        Integer approverType = node.getApproverType();
        String approverValue = node.getApproverValue();
        
        if (approverType == null || !StringUtils.hasText(approverValue)) {
            log.warn("节点未配置审批人，节点ID: {}", nodeId);
            return approvers;
        }
        
        // 3. 根据审批人类型分配审批人
        switch (approverType) {
            case FlowNode.APPROVER_TYPE_USER:
                // 指定人员：approverValue 是用户ID列表（逗号分隔或JSON数组）
                approvers = assignByUserIds(approverValue);
                break;
            
            case FlowNode.APPROVER_TYPE_ROLE:
                // 指定角色：approverValue 是角色ID列表
                approvers = assignByRoleIds(approverValue);
                break;
            
            case FlowNode.APPROVER_TYPE_DEPT_LEADER:
                // 指定部门负责人：approverValue 是部门ID列表
                approvers = assignByDeptIds(approverValue);
                break;
            
            case FlowNode.APPROVER_TYPE_INITIATOR:
                // 发起人指定：从流程变量中获取
                approvers = assignByInitiator(flowInstanceId, processVariables);
                break;
            
            default:
                log.warn("未知的审批人类型: {}, 节点ID: {}", approverType, nodeId);
        }
        
        log.info("分配审批人完成，节点ID: {}, 审批人类型: {}, 分配数量: {}", 
                nodeId, approverType, approvers.size());
        
        return approvers;
    }
    
    /**
     * 根据用户ID列表分配审批人
     */
    private List<Approver> assignByUserIds(String approverValue) {
        try {
            // 解析用户ID列表（支持逗号分隔或JSON数组）
            List<Long> userIds = parseIdList(approverValue);
            
            // 调用平台用户服务适配器批量获取用户并转换为审批人
            return platformUserServiceAdapter.convertToApprovers(userIds);
        } catch (Exception e) {
            log.error("根据用户ID列表分配审批人失败: {}", approverValue, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 根据角色ID列表分配审批人
     */
    private List<Approver> assignByRoleIds(String approverValue) {
        try {
            // 解析角色ID列表
            List<Long> roleIds = parseIdList(approverValue);
            
            // 调用平台用户服务适配器获取角色下的所有用户（去重）
            return platformUserServiceAdapter.getUsersByRoleIds(roleIds);
        } catch (Exception e) {
            log.error("根据角色ID列表分配审批人失败: {}", approverValue, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 根据部门ID列表分配审批人（部门负责人）
     */
    private List<Approver> assignByDeptIds(String approverValue) {
        try {
            // 解析部门ID列表
            List<Long> deptIds = parseIdList(approverValue);
            
            // 调用平台用户服务适配器获取部门负责人（去重）
            return platformUserServiceAdapter.getDeptLeadersByDeptIds(deptIds);
        } catch (Exception e) {
            log.error("根据部门ID列表分配审批人失败: {}", approverValue, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 根据发起人指定分配审批人
     */
    private List<Approver> assignByInitiator(Long flowInstanceId, Map<String, Object> processVariables) {
        try {
            // 从流程变量中获取发起人指定的审批人ID列表
            Object approverIdsObj = processVariables != null ? processVariables.get("approverIds") : null;
            if (approverIdsObj == null) {
                log.warn("流程变量中未找到审批人ID列表，流程实例ID: {}", flowInstanceId);
                return new ArrayList<>();
            }
            
            // 解析审批人ID列表
            List<Long> approverIds = parseIdList(approverIdsObj.toString());
            
            // 调用平台用户服务适配器批量获取用户并转换为审批人
            return platformUserServiceAdapter.convertToApprovers(approverIds);
        } catch (Exception e) {
            log.error("根据发起人指定分配审批人失败，流程实例ID: {}", flowInstanceId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 解析ID列表（支持逗号分隔或JSON数组）
     */
    private List<Long> parseIdList(String value) {
        if (!StringUtils.hasText(value)) {
            return new ArrayList<>();
        }
        
        try {
            // 尝试解析为JSON数组
            if (value.trim().startsWith("[")) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                return mapper.readValue(value, 
                    new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {});
            }
            
            // 解析为逗号分隔的字符串
            return Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("解析ID列表失败: {}", value, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public boolean hasApprovalPermission(Long userId, Long nodeId, Long flowInstanceId) {
        if (userId == null || nodeId == null || flowInstanceId == null) {
            return false;
        }
        
        try {
            // 1. 获取节点实例列表
            List<xtt.cloud.oa.document.domain.entity.flow.FlowNodeInstance> nodeInstances = 
                    flowNodeInstanceRepository.findByFlowInstanceIdAndNodeId(flowInstanceId, nodeId);
            
            if (nodeInstances.isEmpty()) {
                log.warn("节点实例不存在，节点ID: {}, 流程实例ID: {}", nodeId, flowInstanceId);
                return false;
            }
            
            // 2. 检查用户是否在审批人列表中
            boolean isApprover = nodeInstances.stream()
                    .anyMatch(ni -> userId.equals(ni.getApproverId()));
            
            if (!isApprover) {
                log.debug("用户不是该节点的审批人，用户ID: {}, 节点ID: {}, 流程实例ID: {}", 
                        userId, nodeId, flowInstanceId);
                return false;
            }
            
            // 3. 检查节点状态是否允许审批（待处理或处理中）
            boolean canApprove = nodeInstances.stream()
                    .anyMatch(ni -> {
                        NodeStatus status = NodeStatus.fromValue(ni.getStatus());
                        return status.canHandle() && userId.equals(ni.getApproverId());
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

