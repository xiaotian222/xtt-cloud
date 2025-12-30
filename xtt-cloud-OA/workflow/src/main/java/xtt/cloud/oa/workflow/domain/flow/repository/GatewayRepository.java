package xtt.cloud.oa.workflow.domain.flow.repository;

import xtt.cloud.oa.workflow.domain.flow.model.entity.Gateway;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.GatewayType;

import java.util.List;
import java.util.Optional;

/**
 * 网关仓储接口
 * 
 * @author xtt
 */
public interface GatewayRepository {
    
    /**
     * 根据ID查找网关
     */
    Optional<Gateway> findById(Long id);
    
    /**
     * 根据流程定义ID查找所有网关
     */
    List<Gateway> findByFlowDefId(Long flowDefId);
    
    /**
     * 根据流程定义ID和网关类型查找网关
     */
    List<Gateway> findByFlowDefIdAndGatewayType(Long flowDefId, GatewayType gatewayType);
    
    /**
     * 根据Split节点ID查找对应的Join网关
     */
    Optional<Gateway> findJoinBySplitNodeId(Long splitNodeId);
    
    /**
     * 根据Join节点ID查找对应的Split网关
     */
    Optional<Gateway> findSplitByJoinNodeId(Long joinNodeId);
    
    /**
     * 根据网关ID查找网关
     */
    Optional<Gateway> findByGatewayId(Long gatewayId);
    
    /**
     * 保存网关
     */
    Gateway save(Gateway gateway);
    
    /**
     * 更新网关
     */
    void update(Gateway gateway);
    
    /**
     * 删除网关
     */
    void delete(Long id);
}

