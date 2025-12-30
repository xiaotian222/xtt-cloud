package xtt.cloud.oa.workflow.domain.flow.model.valueobject.task;

/**
 * 任务操作类型值对象
 * 
 * @author xtt
 */
public enum TaskAction {
    
    /**
     * 同意
     */
    APPROVE("approve", "同意"),
    
    /**
     * 拒绝
     */
    REJECT("reject", "拒绝"),
    
    /**
     * 转发
     */
    FORWARD("forward", "转发"),
    
    /**
     * 退回
     */
    RETURN("return", "退回"),
    
    /**
     * 委派
     */
    DELEGATE("delegate", "委派");
    
    private final String value;
    private final String name;
    
    TaskAction(String value, String name) {
        this.value = value;
        this.name = name;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getName() {
        return name;
    }
    
    public static TaskAction fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        for (TaskAction action : values()) {
            if (action.value.equals(value)) {
                return action;
            }
        }
        throw new IllegalArgumentException("未知的任务操作类型: " + value);
    }
}

