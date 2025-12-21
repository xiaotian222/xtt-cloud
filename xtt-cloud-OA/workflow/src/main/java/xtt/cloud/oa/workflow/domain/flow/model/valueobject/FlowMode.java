package xtt.cloud.oa.workflow.domain.flow.model.valueobject;

import java.util.Objects;

/**
 * 流程模式值对象
 * 
 * @author xtt
 */
public class FlowMode {
    
    public static final FlowMode FIXED = new FlowMode(1, "固定流");
    public static final FlowMode FREE = new FlowMode(2, "自由流");
    public static final FlowMode MIXED = new FlowMode(3, "混合流");
    
    private final Integer value;
    private final String description;
    
    private FlowMode(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
    
    /**
     * 从整数值创建流程模式
     */
    public static FlowMode fromValue(Integer value) {
        if (value == null) {
            return FIXED;
        }
        switch (value) {
            case 1: return FIXED;
            case 2: return FREE;
            case 3: return MIXED;
            default: throw new IllegalArgumentException("Invalid flow mode value: " + value);
        }
    }
    
    public Integer getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 判断是否为固定流
     */
    public boolean isFixed() {
        return this == FIXED;
    }
    
    /**
     * 判断是否为自由流
     */
    public boolean isFree() {
        return this == FREE;
    }
    
    /**
     * 判断是否为混合流
     */
    public boolean isMixed() {
        return this == MIXED;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowMode flowMode = (FlowMode) o;
        return Objects.equals(value, flowMode.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "FlowMode{" +
                "value=" + value +
                ", description='" + description + '\'' +
                '}';
    }
}

