package xtt.cloud.oa.workflow.domain.flow.service;

import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;

import java.util.List;

/**
 * 节点路由领域服务
 * 
 * 负责计算流程的下一个节点，处理串行、并行、条件流转等逻辑
 * 
 * @author xtt
 */
public interface NodeRoutingService {
    
    /**
     * 获取下一个节点列表
     * 
     * @param currentNodeId 当前节点ID
     * @param flowDefId 流程定义ID
     * @param processVariables 流程变量（用于条件判断）
     * @return 下一个节点ID列表
     */
    List<Long> getNextNodeIds(Long currentNodeId, Long flowDefId, 
                              java.util.Map<String, Object> processVariables);

    /**
     * 判断是否为汇聚节点
     * 
     * @param nodeId 节点ID
     * @param flowDefId 流程定义ID
     * @return 是否为汇聚节点
     */
    boolean isConvergenceNode(Long nodeId, Long flowDefId);
    
    /**
     * 判断是否可以汇聚到指定节点
     * 
     * @param convergenceNodeId 汇聚节点ID
     * @param flowInstanceId 流程实例ID
     * @return 是否可以汇聚
     */
    boolean canConverge(Long convergenceNodeId, Long flowInstanceId);
    
    /**
     * 判断节点是否应该跳过
     * 
     * @param nodeId 节点ID
     * @param flowDefId 流程定义ID
     * @param processVariables 流程变量
     * @return 是否应该跳过
     */
    boolean shouldSkipNode(Long nodeId, Long flowDefId, 
                          java.util.Map<String, Object> processVariables);
    
    /**
     * 判断是否可以流转到下一个节点
     * 
     * 根据节点类型（串行、并行会签、并行或签）判断是否可以流转
     * 
     * @param currentNodeDef 当前节点定义
     * @param flowInstanceId 流程实例ID
     * @return 是否可以流转
     */
    boolean canMoveToNextNode(FlowNode currentNodeDef, Long flowInstanceId);
    
    /**
     * 检查并行节点是否全部完成（会签模式）
     * 
     * @param node 节点定义
     * @param flowInstanceId 流程实例ID
     * @return 是否全部完成
     */
    boolean allParallelNodesCompleted(FlowNode node, Long flowInstanceId);
    
    /**
     * 检查并行节点是否任一完成（或签模式）
     * 
     * @param node 节点定义
     * @param flowInstanceId 流程实例ID
     * @return 是否任一完成
     */
    boolean anyParallelNodeCompleted(FlowNode node, Long flowInstanceId);

    /**
     *  流转到下一个节点
     * @param flowInstance
     * @param currentNodeDef
     * @param currentNode
     */
    void moveToNextNode(FlowInstance flowInstance, FlowNode currentNodeDef, FlowNodeInstance currentNode);

    /**
     * 检查流程是否可以完成
     * 
     * @param currentNodeId 当前节点ID（可能为null）
     * @param flowDefId 流程定义ID
     * @param flowInstanceId 流程实例ID
     * @param processVariables 流程变量
     * @return 是否可以完成
     */
    boolean canCompleteFlow(Long currentNodeId, Long flowDefId, Long flowInstanceId, 
                           java.util.Map<String, Object> processVariables);

    /**
     *  回退到指定节点
     * @param flowInstance
     * @param targetNodeId
     * @param operatorId
     * @param reason
     */
    void rollbackToNode(FlowInstance flowInstance, Long targetNodeId, Long operatorId, String reason);
}

