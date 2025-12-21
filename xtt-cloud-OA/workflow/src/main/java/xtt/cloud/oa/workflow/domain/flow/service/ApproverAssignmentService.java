package xtt.cloud.oa.workflow.domain.flow.service;

import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;

import java.util.List;

/**
 * 审批人分配领域服务
 * 
 * 负责根据节点配置和流程上下文分配审批人
 * 
 * @author xtt
 */
public interface ApproverAssignmentService {
    
    /**
     * 分配审批人
     * 
     * @param nodeId 节点ID
     * @param flowDefId 流程定义ID
     * @param flowInstanceId 流程实例ID
     * @param processVariables 流程变量
     * @return 审批人列表
     */
    List<Approver> assignApprovers(Long nodeId, Long flowDefId, Long flowInstanceId,
                                   java.util.Map<String, Object> processVariables);
    
    /**
     * 验证用户是否有审批权限
     * 
     * @param userId 用户ID
     * @param nodeId 节点ID
     * @param flowInstanceId 流程实例ID
     * @return 是否有权限
     */
    boolean hasApprovalPermission(Long userId, Long nodeId, Long flowInstanceId);
}

