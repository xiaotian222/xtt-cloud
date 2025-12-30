package xtt.cloud.oa.workflow.domain.flow.model.valueobject.task;

/**
 * 任务优先级值对象
 * 
 * @author xtt
 */
public enum TaskPriority {
    
    /**
     * 普通
     */
    NORMAL(0, "普通"),
    
    /**
     * 紧急
     */
    URGENT(1, "紧急"),
    
    /**
     * 特急
     */
    CRITICAL(2, "特急");
    
    private final int value;
    private final String description;
    
    TaskPriority(int value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static TaskPriority fromValue(Integer value) {
        if (value == null) {
            return NORMAL;
        }
        for (TaskPriority priority : values()) {
            if (priority.value == value) {
                return priority;
            }
        }
        throw new IllegalArgumentException("未知的任务优先级值: " + value);
    }
}

