package xtt.cloud.oa.workflow.domain.flow.model.valueobject;

/**
 * 流程定义状态值对象
 * 
 * 值对象特点：
 * 1. 不可变（Immutable）
 * 2. 封装状态转换规则
 * 3. 类型安全
 * 
 * @author xtt
 */
public enum FlowDefinitionStatus {
    
    /**
     * 停用
     */
    DISABLED(0, "停用"),
    
    /**
     * 启用
     */
    ENABLED(1, "启用");
    
    private final int value;
    private final String description;
    
    FlowDefinitionStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }
    
    /**
     * 获取状态值
     */
    public int getValue() {
        return value;
    }
    
    /**
     * 获取描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据值获取状态
     */
    public static FlowDefinitionStatus fromValue(Integer value) {
        if (value == null) {
            return DISABLED;
        }
        for (FlowDefinitionStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的流程定义状态值: " + value);
    }
    
    /**
     * 判断是否启用
     */
    public boolean isEnabled() {
        return this == ENABLED;
    }
    
    /**
     * 判断是否停用
     */
    public boolean isDisabled() {
        return this == DISABLED;
    }
    
    /**
     * 启用流程定义
     */
    public FlowDefinitionStatus enable() {
        if (this == ENABLED) {
            throw new IllegalStateException("流程定义已经启用");
        }
        return ENABLED;
    }
    
    /**
     * 停用流程定义
     */
    public FlowDefinitionStatus disable() {
        if (this == DISABLED) {
            throw new IllegalStateException("流程定义已经停用");
        }
        return DISABLED;
    }
}

