package xtt.cloud.oa.workflow.domain.flow.service;

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
}

