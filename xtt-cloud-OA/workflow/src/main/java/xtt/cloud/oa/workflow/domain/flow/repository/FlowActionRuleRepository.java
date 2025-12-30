package xtt.cloud.oa.workflow.domain.flow.repository;

import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowActionRule;

import java.util.List;

/**
 * 动作规则仓储接口
 * 
 * @author xtt
 */
public interface FlowActionRuleRepository {
    
    /**
     * 根据动作ID查找规则
     * 
     * @param actionId 动作ID
     * @return 规则列表
     */
    List<FlowActionRule> findByActionId(Long actionId);
    
    /**
     * 匹配规则
     * 根据文档状态、用户角色、部门ID查找匹配的规则
     * 
     * @param documentStatus 文档状态
     * @param userRoles 用户角色列表
     * @param deptId 部门ID
     * @return 匹配的规则列表（按优先级降序）
     */
    List<FlowActionRule> matchRules(Integer documentStatus, List<String> userRoles, Long deptId);
}

