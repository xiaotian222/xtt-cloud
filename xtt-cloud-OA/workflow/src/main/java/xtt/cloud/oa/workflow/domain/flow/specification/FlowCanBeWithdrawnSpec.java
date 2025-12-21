package xtt.cloud.oa.workflow.domain.flow.specification;

import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowStatus;

/**
 * 流程是否可以撤回的规格
 * 
 * 使用规格模式（Specification Pattern）封装业务规则
 * 
 * @author xtt
 */
public class FlowCanBeWithdrawnSpec {
    
    /**
     * 判断流程是否可以撤回
     * 
     * 业务规则：
     * 1. 流程状态必须是进行中
     * 2. 只有发起人可以撤回
     * 3. 如果流程已经开始审批，不能撤回
     * 
     * @param flowInstance 流程实例
     * @param userId 用户ID（发起人ID）
     * @return true 如果可以撤回
     */
    public boolean isSatisfiedBy(FlowInstance flowInstance, Long userId) {
        if (flowInstance == null) {
            return false;
        }
        
        // 1. 检查流程状态
        FlowStatus status = flowInstance.getStatus();
        if (status == null || !status.equals(FlowStatus.PROCESSING)) {
            return false;
        }
        
        // 2. 检查是否为发起人
        // 发起人信息可能存储在流程变量中，或者从文档的创建人获取
        // 这里简化处理，从流程变量中获取发起人ID
        Object initiatorIdObj = flowInstance.getProcessVariables().getVariable("initiatorId");
        if (initiatorIdObj == null) {
            // 如果没有流程变量，则无法判断，返回false
            return false;
        }
        Long initiatorId = initiatorIdObj instanceof Long ? (Long) initiatorIdObj : Long.valueOf(initiatorIdObj.toString());
        if (!initiatorId.equals(userId)) {
            return false;
        }
        
        // 3. 检查是否已经开始审批（如果有节点实例已完成，则不能撤回）
        boolean hasCompletedNode = flowInstance.getNodeInstances().stream()
                .anyMatch(node -> node.getStatus().isCompleted());
        
        if (hasCompletedNode) {
            return false;
        }
        
        return true;
    }
}

