package xtt.cloud.oa.workflow.domain.flow.model.valueobject;

import java.util.Objects;

/**
 * 流程实例ID值对象
 * 
 * 用于类型安全地表示流程实例ID
 * 
 * @author xtt
 */
public class FlowInstanceId {
    
    private final Long value;
    
    public FlowInstanceId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Flow instance ID must be a positive number");
        }
        this.value = value;
    }
    
    public static FlowInstanceId of(Long value) {
        return new FlowInstanceId(value);
    }
    
    public Long getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowInstanceId that = (FlowInstanceId) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "FlowInstanceId{" +
                "value=" + value +
                '}';
    }
}

