package xtt.cloud.oa.workflow.domain.flow.model.valueobject;

/**
 * 网关类型值对象
 * 
 * 网关用于控制流程的分支和汇聚
 * 
 * @author xtt
 */
public enum GatewayType {
    
    /**
     * 并行网关 - Split 分支（并行分支开始）
     */
    PARALLEL_SPLIT(1, "并行网关-分支"),
    
    /**
     * 并行网关 - Join 汇聚（并行分支合并）
     */
    PARALLEL_JOIN(2, "并行网关-汇聚"),
    
    /**
     * 条件网关 - Split 分支（条件分支开始）
     */
    CONDITION_SPLIT(3, "条件网关-分支"),
    
    /**
     * 条件网关 - Join 汇聚（条件分支合并）
     */
    CONDITION_JOIN(4, "条件网关-汇聚"),
    
    /**
     * 非网关节点（普通节点）
     */
    NONE(0, "非网关");
    
    private final int value;
    private final String description;
    
    GatewayType(int value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static GatewayType fromValue(Integer value) {
        if (value == null) {
            return NONE;
        }
        for (GatewayType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的网关类型值: " + value);
    }
    
    /**
     * 判断是否为网关节点
     */
    public boolean isGateway() {
        return this != NONE;
    }
    
    /**
     * 判断是否为 Split 分支
     */
    public boolean isSplit() {
        return this == PARALLEL_SPLIT || this == CONDITION_SPLIT;
    }
    
    /**
     * 判断是否为 Join 汇聚
     */
    public boolean isJoin() {
        return this == PARALLEL_JOIN || this == CONDITION_JOIN;
    }
    
    /**
     * 判断是否为并行网关
     */
    public boolean isParallel() {
        return this == PARALLEL_SPLIT || this == PARALLEL_JOIN;
    }
    
    /**
     * 判断是否为条件网关
     */
    public boolean isCondition() {
        return this == CONDITION_SPLIT || this == CONDITION_JOIN;
    }
    
    /**
     * 获取对应的 Join 类型
     * 
     * @return 如果是 Split，返回对应的 Join；否则返回 null
     */
    public GatewayType getCorrespondingJoin() {
        if (this == PARALLEL_SPLIT) {
            return PARALLEL_JOIN;
        }
        if (this == CONDITION_SPLIT) {
            return CONDITION_JOIN;
        }
        return null;
    }
    
    /**
     * 获取对应的 Split 类型
     * 
     * @return 如果是 Join，返回对应的 Split；否则返回 null
     */
    public GatewayType getCorrespondingSplit() {
        if (this == PARALLEL_JOIN) {
            return PARALLEL_SPLIT;
        }
        if (this == CONDITION_JOIN) {
            return CONDITION_SPLIT;
        }
        return null;
    }
}

