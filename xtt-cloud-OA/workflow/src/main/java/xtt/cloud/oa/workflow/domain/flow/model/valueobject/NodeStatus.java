package xtt.cloud.oa.workflow.domain.flow.model.valueobject;

import java.util.Objects;

/**
 * 节点状态值对象
 * 
 * @author xtt
 */
public class NodeStatus {
    
    public static final NodeStatus PENDING = new NodeStatus(0, "待处理");
    public static final NodeStatus PROCESSING = new NodeStatus(1, "处理中");
    public static final NodeStatus COMPLETED = new NodeStatus(2, "已完成");
    public static final NodeStatus REJECTED = new NodeStatus(3, "已拒绝");
    public static final NodeStatus SKIPPED = new NodeStatus(4, "已跳过");
    
    private final Integer value;
    private final String description;
    
    private NodeStatus(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
    
    /**
     * 从整数值创建节点状态
     */
    public static NodeStatus fromValue(Integer value) {
        if (value == null) {
            return PENDING;
        }
        switch (value) {
            case 0: return PENDING;
            case 1: return PROCESSING;
            case 2: return COMPLETED;
            case 3: return REJECTED;
            case 4: return SKIPPED;
            default: throw new IllegalArgumentException("Invalid node status value: " + value);
        }
    }
    
    public Integer getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 判断是否为待处理状态
     */
    public boolean isPending() {
        return this == PENDING;
    }
    
    /**
     * 判断是否为处理中状态
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
     * 判断是否为已拒绝状态
     */
    public boolean isRejected() {
        return this == REJECTED;
    }
    
    /**
     * 判断是否为已跳过状态
     */
    public boolean isSkipped() {
        return this == SKIPPED;
    }
    
    /**
     * 判断节点是否已完成（包括已完成和已跳过）
     */
    public boolean isFinished() {
        return isCompleted() || isSkipped();
    }
    
    /**
     * 判断节点是否可以处理
     */
    public boolean canHandle() {
        return isPending() || isProcessing();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeStatus that = (NodeStatus) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "NodeStatus{" +
                "value=" + value +
                ", description='" + description + '\'' +
                '}';
    }
}

