package xtt.cloud.oa.workflow.domain.flow.model.valueobject;

import java.util.Objects;

/**
 * 流程定义ID值对象
 * 
 * 值对象特点：
 * 1. 不可变（Immutable）
 * 2. 通过值相等性比较
 * 3. 类型安全
 * 
 * @author xtt
 */
public class FlowDefinitionId {
    
    private final Long value;
    
    private FlowDefinitionId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("流程定义ID必须大于0");
        }
        this.value = value;
    }
    
    /**
     * 创建流程定义ID
     */
    public static FlowDefinitionId of(Long value) {
        return new FlowDefinitionId(value);
    }
    
    /**
     * 从可能为null的值创建（用于查询场景）
     */
    public static FlowDefinitionId ofNullable(Long value) {
        return value != null && value > 0 ? new FlowDefinitionId(value) : null;
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
        FlowDefinitionId that = (FlowDefinitionId) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "FlowDefinitionId{" + "value=" + value + '}';
    }
}

