package xtt.cloud.oa.workflow.domain.flow.specification;

import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowStatus;

/**
 * 流程是否可以回退的规格
 * 
 * 使用规格模式（Specification Pattern）封装业务规则
 * 
 * @author xtt
 */
public class FlowCanRollbackSpec {
    
    /**
     * 判断流程是否可以回退到指定节点
     * 
     * 业务规则：
     * 1. 流程状态必须是进行中
     * 2. 目标节点必须是已完成的节点
     * 3. 目标节点必须在当前节点之前
     * 
     * @param flowInstance 流程实例
     * @param targetNodeId 目标节点ID
     * @return true 如果可以回退
     */
    public boolean isSatisfiedBy(FlowInstance flowInstance, Long targetNodeId) {
        if (flowInstance == null || targetNodeId == null) {
            return false;
        }
        
        // 1. 检查流程状态
        FlowStatus status = flowInstance.getStatus();
        if (status == null || !status.equals(FlowStatus.PROCESSING)) {
            return false;
        }
        
        // 2. 检查目标节点是否存在且已完成
        boolean targetNodeExists = flowInstance.getNodeInstances().stream()
                .anyMatch(node -> node.getNodeId().equals(targetNodeId) 
                        && node.getStatus().isCompleted());
        
        if (!targetNodeExists) {
            return false;
        }
        
        // 3. 检查目标节点是否在当前节点之前（通过节点顺序判断）
        // TODO: 根据实际业务规则实现节点顺序判断逻辑
        
        return true;
    }
}

