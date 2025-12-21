package xtt.cloud.oa.workflow.domain.flow.model.factory;

import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.NodeStatus;

/**
 * 节点实例工厂
 * 
 * 负责创建节点实例实体
 * 
 * @author xtt
 */
public class FlowNodeInstanceFactory {
    
    /**
     * 创建节点实例
     * 
     * @param flowInstanceId 流程实例ID
     * @param nodeId 节点ID
     * @param approver 审批人
     * @return 节点实例实体
     */
    public static FlowNodeInstance create(Long flowInstanceId, Long nodeId, Approver approver) {
        FlowNodeInstance nodeInstance = new FlowNodeInstance();
        nodeInstance.setFlowInstanceId(flowInstanceId);
        nodeInstance.setNodeId(nodeId);
        nodeInstance.setApprover(approver);
        nodeInstance.setStatus(NodeStatus.PENDING);
        nodeInstance.setCreatedAt(java.time.LocalDateTime.now());
        nodeInstance.setUpdatedAt(java.time.LocalDateTime.now());
        return nodeInstance;
    }
    
    /**
     * 创建已跳过的节点实例
     * 
     * @param flowInstanceId 流程实例ID
     * @param nodeId 节点ID
     * @param reason 跳过原因
     * @return 节点实例实体
     */
    public static FlowNodeInstance createSkipped(Long flowInstanceId, Long nodeId, String reason) {
        FlowNodeInstance nodeInstance = new FlowNodeInstance();
        nodeInstance.setFlowInstanceId(flowInstanceId);
        nodeInstance.setNodeId(nodeId);
        nodeInstance.setStatus(NodeStatus.SKIPPED);
        nodeInstance.setComments(reason);
        nodeInstance.setCreatedAt(java.time.LocalDateTime.now());
        nodeInstance.setUpdatedAt(java.time.LocalDateTime.now());
        return nodeInstance;
    }
}

