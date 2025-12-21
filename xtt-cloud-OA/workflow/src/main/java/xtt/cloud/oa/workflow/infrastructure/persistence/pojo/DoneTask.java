package xtt.cloud.oa.workflow.infrastructure.persistence.pojo;

import java.time.LocalDateTime;

/**
 * 已办任务实体
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
public class DoneTask {
    private Long id;
    private Long documentId;      // 公文ID
    private Long flowInstanceId;  // 流程实例ID
    private Long nodeInstanceId; // 节点实例ID
    private Long handlerId;       // 处理人ID
    private String title;         // 已办标题
    private Integer taskType;     // 任务类型
    private String action;        // 操作类型
    private String comments;      // 处理意见
    private LocalDateTime handledAt; // 处理时间
    private LocalDateTime createdAt;
    
    // 任务类型常量
    public static final int TASK_TYPE_USER = 1;    // 用户任务
    public static final int TASK_TYPE_JAVA = 2;   // JAVA任务
    public static final int TASK_TYPE_OTHER = 3;   // 其他任务
    
    // 操作类型常量
    public static final String ACTION_APPROVE = "approve";  // 同意
    public static final String ACTION_REJECT = "reject";    // 拒绝
    public static final String ACTION_FORWARD = "forward";  // 转发
    public static final String ACTION_RETURN = "return";   // 退回
    public static final String ACTION_DELEGATE = "delegate"; // 委派
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    
    public Long getFlowInstanceId() { return flowInstanceId; }
    public void setFlowInstanceId(Long flowInstanceId) { this.flowInstanceId = flowInstanceId; }
    
    public Long getNodeInstanceId() { return nodeInstanceId; }
    public void setNodeInstanceId(Long nodeInstanceId) { this.nodeInstanceId = nodeInstanceId; }
    
    public Long getHandlerId() { return handlerId; }
    public void setHandlerId(Long handlerId) { this.handlerId = handlerId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public Integer getTaskType() { return taskType; }
    public void setTaskType(Integer taskType) { this.taskType = taskType; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    
    public LocalDateTime getHandledAt() { return handledAt; }
    public void setHandledAt(LocalDateTime handledAt) { this.handledAt = handledAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

