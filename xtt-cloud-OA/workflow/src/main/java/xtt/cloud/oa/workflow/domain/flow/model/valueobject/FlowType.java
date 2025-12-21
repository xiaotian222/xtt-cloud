package xtt.cloud.oa.workflow.domain.flow.model.valueobject;

import java.util.Objects;

/**
 * 流程类型值对象
 * 
 * @author xtt
 */
public class FlowType {
    
    public static final FlowType ISSUANCE = new FlowType(1, "发文");
    public static final FlowType RECEIPT = new FlowType(2, "收文");
    
    private final Integer value;
    private final String description;
    
    private FlowType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
    
    /**
     * 从整数值创建流程类型
     */
    public static FlowType fromValue(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("Flow type value cannot be null");
        }
        switch (value) {
            case 1: return ISSUANCE;
            case 2: return RECEIPT;
            default: throw new IllegalArgumentException("Invalid flow type value: " + value);
        }
    }
    
    public Integer getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 判断是否为发文类型
     */
    public boolean isIssuance() {
        return this == ISSUANCE;
    }
    
    /**
     * 判断是否为收文类型
     */
    public boolean isReceipt() {
        return this == RECEIPT;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowType flowType = (FlowType) o;
        return Objects.equals(value, flowType.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "FlowType{" +
                "value=" + value +
                ", description='" + description + '\'' +
                '}';
    }
}

