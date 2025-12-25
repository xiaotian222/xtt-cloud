package xtt.cloud.oa.workflow.domain.flow.service;

import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;

import java.util.List;

/**
 * 审批人提供者接口
 * 
 * 领域服务接口，用于从外部系统获取审批人信息
 * 实现类在基础设施层，通过依赖倒置实现
 * 
 * @author xtt
 */
public interface ApproverProvider {
    
    /**
     * 根据用户ID列表转换为审批人列表
     * 
     * @param userIds 用户ID列表
     * @return 审批人列表
     */
    List<Approver> convertToApprovers(List<Long> userIds);
    
    /**
     * 根据角色ID列表获取该角色下的所有用户（去重）
     * 
     * @param roleIds 角色ID列表
     * @return 审批人列表
     */
    List<Approver> getUsersByRoleIds(List<Long> roleIds);
    
    /**
     * 根据部门ID列表获取所有部门负责人（去重）
     * 
     * @param deptIds 部门ID列表
     * @return 审批人列表
     */
    List<Approver> getDeptLeadersByDeptIds(List<Long> deptIds);
}

