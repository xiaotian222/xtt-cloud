package xtt.cloud.oa.workflow.domain.flow.model.valueobject;

/**
 * 并行网关模式值对象
 * 
 * 用于并行网关的汇聚策略
 * 
 * @author xtt
 */
public enum GatewayMode {
    
    /**
     * 并行会签（所有分支都完成才汇聚）
     */
    PARALLEL_ALL(1, "并行会签"),
    
    /**
     * 并行或签（任一分支完成即可汇聚）
     */
    PARALLEL_ANY(2, "并行或签");
    
    private final int value;
    private final String description;
    
    GatewayMode(int value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static GatewayMode fromValue(Integer value) {
        if (value == null) {
            return PARALLEL_ALL; // 默认会签模式
        }
        for (GatewayMode mode : values()) {
            if (mode.value == value) {
                return mode;
            }
        }
        throw new IllegalArgumentException("未知的网关模式值: " + value);
    }
    
    /**
     * 判断是否为会签模式
     */
    public boolean isAll() {
        return this == PARALLEL_ALL;
    }
    
    /**
     * 判断是否为或签模式
     */
    public boolean isAny() {
        return this == PARALLEL_ANY;
    }
}

