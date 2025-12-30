package xtt.cloud.oa.workflow.domain.flow.model.valueobject.task;

/**
 * 任务状态值对象
 * 
 * 值对象特点：
 * 1. 不可变（Immutable）
 * 2. 封装状态转换规则
 * 3. 类型安全
 * 
 * @author xtt
 */
public enum TaskStatus {
    
    /**
     * 待处理
     */
    PENDING(0, "待处理"),
    
    /**
     * 处理中
     */
    HANDLING(1, "处理中"),
    
    /**
     * 已完成
     */
    COMPLETED(2, "已完成"),
    
    /**
     * 已拒绝
     */
    REJECTED(3, "已拒绝"),
    
    /**
     * 已跳过
     */
    SKIPPED(4, "已跳过"),
    
    /**
     * 已取消
     */
    CANCELLED(5, "已取消");
    
    private final int value;
    private final String description;
    
    TaskStatus(int value, String description) {
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
    public static TaskStatus fromValue(Integer value) {
        if (value == null) {
            return PENDING;
        }
        for (TaskStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的任务状态值: " + value);
    }
    
    /**
     * 判断是否可以处理
     */
    public boolean canHandle() {
        return this == PENDING || this == HANDLING;
    }
    
    /**
     * 判断是否已完成
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }
    
    /**
     * 判断是否已结束（完成、拒绝、跳过、取消）
     */
    public boolean isFinished() {
        return this == COMPLETED || this == REJECTED || this == SKIPPED || this == CANCELLED;
    }
}

