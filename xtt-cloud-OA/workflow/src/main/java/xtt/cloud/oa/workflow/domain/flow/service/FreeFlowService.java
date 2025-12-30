package xtt.cloud.oa.workflow.domain.flow.service;

import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowAction;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;

import java.util.List;

/**
 * 自由流领域服务
 * 
 * 职责：
 * 1. 根据文档状态和用户角色获取可用动作
 * 2. 根据动作和用户角色获取可选审批人
 * 
 * @author xtt
 */
public interface FreeFlowService {
    
    /**
     * 获取可用动作
     * 
     * 根据文档状态（待办/已办）、用户角色来决定可用的动作按钮
     * 
     * @param documentStatus 文档状态（0:草稿,1:审核中,2:已发布,3:已归档）
     * @param userRoles 用户角色列表
     * @param deptId 用户部门ID
     * @return 可用动作列表
     */
    List<FlowAction> getAvailableActions(Integer documentStatus, List<String> userRoles, Long deptId);
    
    /**
     * 获取可选审批人
     * 
     * 根据动作和用户角色来决定可以发送的审批人
     * 
     * @param actionId 动作ID
     * @param userRoles 用户角色列表
     * @param deptId 用户部门ID
     * @param processVariables 流程变量（可选，用于条件判断）
     * @return 可选审批人列表
     */
    List<Approver> getAvailableApprovers(
            Long actionId, 
            List<String> userRoles, 
            Long deptId,
            java.util.Map<String, Object> processVariables);
}

