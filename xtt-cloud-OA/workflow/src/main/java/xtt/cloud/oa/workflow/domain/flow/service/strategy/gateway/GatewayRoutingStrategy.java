package xtt.cloud.oa.workflow.domain.flow.service.strategy.gateway;

import xtt.cloud.oa.workflow.domain.flow.model.valueobject.GatewayType;

import java.util.List;
import java.util.Map;

/**
 * 网关路由策略接口
 * 
 * 策略模式：将不同的网关路由逻辑封装为独立策略
 * 
 * @author xtt
 */
public interface GatewayRoutingStrategy {
    
    /**
     * 获取下一个节点列表
     * 
     * @param gatewayNodeId 网关节点ID
     * @param flowInstanceId 流程实例ID
     * @param processVariables 流程变量
     * @return 下一个节点ID列表
     */
    List<Long> getNextNodes(Long gatewayNodeId, Long flowInstanceId, 
                           Map<String, Object> processVariables);
    
    /**
     * 判断是否可以汇聚
     * 
     * @param joinNodeId 汇聚节点ID
     * @param flowInstanceId 流程实例ID
     * @return 是否可以汇聚
     */
    boolean canConverge(Long joinNodeId, Long flowInstanceId);
    
    /**
     * 判断是否支持该网关类型
     * 
     * @param gatewayType 网关类型
     * @return 是否支持
     */
    boolean supports(GatewayType gatewayType);
}

