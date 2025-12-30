package xtt.cloud.oa.workflow.domain.flow.model.valueobject.history;

/**
 * 活动类型值对象
 * 
 * @author xtt
 */
public enum ActivityType {
    
    /**
     * 节点创建
     */
    CREATE(1, "节点创建"),
    
    /**
     * 节点开始
     */
    START(2, "节点开始"),
    
    /**
     * 节点完成
     */
    COMPLETE(3, "节点完成"),
    
    /**
     * 节点拒绝
     */
    REJECT(4, "节点拒绝"),
    
    /**
     * 节点跳过
     */
    SKIP(5, "节点跳过");
    
    private final int value;
    private final String description;
    
    ActivityType(int value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static ActivityType fromValue(Integer value) {
        if (value == null) {
            return CREATE;
        }
        for (ActivityType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的活动类型值: " + value);
    }
    
    /**
     * 获取操作字符串
     */
    public String getOperation() {
        return switch (this) {
            case CREATE -> "create";
            case START -> "start";
            case COMPLETE -> "complete";
            case REJECT -> "reject";
            case SKIP -> "skip";
        };
    }
}

