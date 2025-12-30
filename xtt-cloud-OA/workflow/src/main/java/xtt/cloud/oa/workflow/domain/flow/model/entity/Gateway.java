package xtt.cloud.oa.workflow.domain.flow.model.entity;

import xtt.cloud.oa.workflow.domain.flow.model.valueobject.GatewayType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.GatewayMode;

import java.util.Objects;

/**
 * 网关实体
 * 
 * 网关用于控制流程的分支和汇聚
 * - Split 分支：将流程分成多个分支
 * - Join 汇聚：将多个分支汇聚成一个
 * 
 * @author xtt
 */
public class Gateway {
    
    private Long id;
    private Long flowDefId;              // 流程定义ID
    private String gatewayName;          // 网关名称
    private GatewayType gatewayType;     // 网关类型
    private GatewayMode gatewayMode;     // 并行网关模式（仅用于并行网关）
    private Long splitNodeId;            // Split 节点ID（用于 Join 节点关联 Split）
    private Long joinNodeId;             // Join 节点ID（用于 Split 节点关联 Join）
    private String conditionExpression;  // 条件表达式（仅用于条件网关，SpEL格式）
    
    /**
     * 创建并行网关 Split（工厂方法）
     */
    public static Gateway createParallelSplit(
            Long flowDefId,
            String gatewayName,
            GatewayMode gatewayMode) {
        
        if (flowDefId == null || flowDefId <= 0) {
            throw new IllegalArgumentException("流程定义ID必须大于0");
        }
        if (gatewayName == null || gatewayName.trim().isEmpty()) {
            throw new IllegalArgumentException("网关名称不能为空");
        }
        if (gatewayMode == null) {
            throw new IllegalArgumentException("并行网关模式不能为空");
        }
        
        Gateway gateway = new Gateway();
        gateway.flowDefId = flowDefId;
        gateway.gatewayName = gatewayName.trim();
        gateway.gatewayType = GatewayType.PARALLEL_SPLIT;
        gateway.gatewayMode = gatewayMode;
        
        return gateway;
    }
    
    /**
     * 创建并行网关 Join（工厂方法）
     */
    public static Gateway createParallelJoin(
            Long flowDefId,
            String gatewayName,
            GatewayMode gatewayMode,
            Long splitNodeId) {
        
        if (flowDefId == null || flowDefId <= 0) {
            throw new IllegalArgumentException("流程定义ID必须大于0");
        }
        if (gatewayName == null || gatewayName.trim().isEmpty()) {
            throw new IllegalArgumentException("网关名称不能为空");
        }
        if (gatewayMode == null) {
            throw new IllegalArgumentException("并行网关模式不能为空");
        }
        if (splitNodeId == null || splitNodeId <= 0) {
            throw new IllegalArgumentException("Split节点ID必须大于0");
        }
        
        Gateway gateway = new Gateway();
        gateway.flowDefId = flowDefId;
        gateway.gatewayName = gatewayName.trim();
        gateway.gatewayType = GatewayType.PARALLEL_JOIN;
        gateway.gatewayMode = gatewayMode;
        gateway.splitNodeId = splitNodeId;
        
        return gateway;
    }
    
    /**
     * 创建条件网关 Split（工厂方法）
     */
    public static Gateway createConditionSplit(
            Long flowDefId,
            String gatewayName,
            String conditionExpression) {
        
        if (flowDefId == null || flowDefId <= 0) {
            throw new IllegalArgumentException("流程定义ID必须大于0");
        }
        if (gatewayName == null || gatewayName.trim().isEmpty()) {
            throw new IllegalArgumentException("网关名称不能为空");
        }
        
        Gateway gateway = new Gateway();
        gateway.flowDefId = flowDefId;
        gateway.gatewayName = gatewayName.trim();
        gateway.gatewayType = GatewayType.CONDITION_SPLIT;
        gateway.conditionExpression = conditionExpression != null ? conditionExpression.trim() : null;
        
        return gateway;
    }
    
    /**
     * 创建条件网关 Join（工厂方法）
     */
    public static Gateway createConditionJoin(
            Long flowDefId,
            String gatewayName,
            Long splitNodeId) {
        
        if (flowDefId == null || flowDefId <= 0) {
            throw new IllegalArgumentException("流程定义ID必须大于0");
        }
        if (gatewayName == null || gatewayName.trim().isEmpty()) {
            throw new IllegalArgumentException("网关名称不能为空");
        }
        if (splitNodeId == null || splitNodeId <= 0) {
            throw new IllegalArgumentException("Split节点ID必须大于0");
        }
        
        Gateway gateway = new Gateway();
        gateway.flowDefId = flowDefId;
        gateway.gatewayName = gatewayName.trim();
        gateway.gatewayType = GatewayType.CONDITION_JOIN;
        gateway.splitNodeId = splitNodeId;
        
        return gateway;
    }
    
    /**
     * 关联 Split 和 Join 节点
     */
    public void linkWithJoin(Long joinNodeId) {
        if (this.gatewayType != GatewayType.PARALLEL_SPLIT && 
            this.gatewayType != GatewayType.CONDITION_SPLIT) {
            throw new IllegalStateException("只有 Split 网关可以关联 Join 节点");
        }
        this.joinNodeId = joinNodeId;
    }
    
    /**
     * 关联 Split 节点（用于 Join 节点）
     */
    public void linkWithSplit(Long splitNodeId) {
        if (this.gatewayType != GatewayType.PARALLEL_JOIN && 
            this.gatewayType != GatewayType.CONDITION_JOIN) {
            throw new IllegalStateException("只有 Join 网关可以关联 Split 节点");
        }
        this.splitNodeId = splitNodeId;
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public Long getFlowDefId() {
        return flowDefId;
    }
    
    public String getGatewayName() {
        return gatewayName;
    }
    
    public GatewayType getGatewayType() {
        return gatewayType;
    }
    
    public GatewayMode getGatewayMode() {
        return gatewayMode;
    }
    
    public Long getSplitNodeId() {
        return splitNodeId;
    }
    
    public Long getJoinNodeId() {
        return joinNodeId;
    }
    
    public String getConditionExpression() {
        return conditionExpression;
    }
    
    // Setters (仅用于持久化层重建对象)
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setFlowDefId(Long flowDefId) {
        this.flowDefId = flowDefId;
    }
    
    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }
    
    public void setGatewayType(GatewayType gatewayType) {
        this.gatewayType = gatewayType;
    }
    
    public void setGatewayMode(GatewayMode gatewayMode) {
        this.gatewayMode = gatewayMode;
    }
    
    public void setSplitNodeId(Long splitNodeId) {
        this.splitNodeId = splitNodeId;
    }
    
    public void setJoinNodeId(Long joinNodeId) {
        this.joinNodeId = joinNodeId;
    }
    
    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gateway gateway = (Gateway) o;
        return Objects.equals(id, gateway.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Gateway{" +
                "id=" + id +
                ", gatewayName='" + gatewayName + '\'' +
                ", gatewayType=" + gatewayType +
                ", gatewayMode=" + gatewayMode +
                '}';
    }
}

