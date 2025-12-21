package xtt.cloud.oa.workflow.domain.flow.model.valueobject;

import java.util.Objects;

/**
 * 流程状态值对象
 * 
 * 值对象特点：
 * 1. 不可变（Immutable）
 * 2. 通过值相等性比较
 * 3. 无唯一标识
 * 
 * @author xtt
 */
public class FlowStatus {
    
    public static final FlowStatus PROCESSING = new FlowStatus(0, "进行中");
    public static final FlowStatus COMPLETED = new FlowStatus(1, "已完成");
    public static final FlowStatus TERMINATED = new FlowStatus(2, "已终止");
    public static final FlowStatus SUSPENDED = new FlowStatus(3, "已暂停");
    public static final FlowStatus CANCELLED = new FlowStatus(4, "已取消");
    
    private final Integer value;
    private final String description;
    
    private FlowStatus(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
    
    /**
     * 从整数值创建流程状态
     */
    public static FlowStatus fromValue(Integer value) {
        if (value == null) {
            return PROCESSING;
        }
        switch (value) {
            case 0: return PROCESSING;
            case 1: return COMPLETED;
            case 2: return TERMINATED;
            case 3: return SUSPENDED;
            case 4: return CANCELLED;
            default: throw new IllegalArgumentException("Invalid flow status value: " + value);
        }
    }
    
    public Integer getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 判断是否为进行中状态
     */
    public boolean isProcessing() {
        return this == PROCESSING;
    }
    
    /**
     * 判断是否为已完成状态
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }
    
    /**
     * 判断是否为已终止状态
     */
    public boolean isTerminated() {
        return this == TERMINATED;
    }
    
    /**
     * 判断是否为已暂停状态
     */
    public boolean isSuspended() {
        return this == SUSPENDED;
    }
    
    /**
     * 判断是否为已取消状态
     */
    public boolean isCancelled() {
        return this == CANCELLED;
    }
    
    /**
     * 判断流程是否可以继续执行
     */
    public boolean canProceed() {
        return isProcessing() || isSuspended();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowStatus that = (FlowStatus) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "FlowStatus{" +
                "value=" + value +
                ", description='" + description + '\'' +
                '}';
    }
}

