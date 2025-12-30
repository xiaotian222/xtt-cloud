package xtt.cloud.oa.workflow.domain.flow.model.valueobject;

import java.util.Objects;

/**
 * 流程节点ID值对象
 * 
 * 值对象特点：
 * 1. 不可变（Immutable）
 * 2. 通过值相等性比较
 * 3. 类型安全
 * 
 * @author xtt
 */
public class FlowNodeId {
    
    private final Long value;
    
    private FlowNodeId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("流程节点ID必须大于0");
        }
        this.value = value;
    }
    
    /**
     * 创建流程节点ID
     */
    public static FlowNodeId of(Long value) {
        return new FlowNodeId(value);
    }
    
    /**
     * 从可能为null的值创建（用于查询场景）
     */
    public static FlowNodeId ofNullable(Long value) {
        return value != null && value > 0 ? new FlowNodeId(value) : null;
    }
    
    /**
     * 获取值
     */
    public Long getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowNodeId that = (FlowNodeId) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "FlowNodeId{" + "value=" + value + '}';
    }
}

