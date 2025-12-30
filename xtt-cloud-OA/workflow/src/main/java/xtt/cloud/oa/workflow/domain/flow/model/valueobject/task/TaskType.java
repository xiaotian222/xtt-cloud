package xtt.cloud.oa.workflow.domain.flow.model.valueobject.task;

/**
 * 任务类型值对象
 * 
 * @author xtt
 */
public enum TaskType {
    
    /**
     * 用户任务
     */
    USER(1, "用户任务"),
    
    /**
     * JAVA任务
     */
    JAVA(2, "JAVA任务"),
    
    /**
     * 其他任务
     */
    OTHER(3, "其他任务");
    
    private final int value;
    private final String description;
    
    TaskType(int value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static TaskType fromValue(Integer value) {
        if (value == null) {
            return USER;
        }
        for (TaskType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的任务类型值: " + value);
    }
}

