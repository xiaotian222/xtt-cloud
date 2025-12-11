package xtt.cloud.oa.document.domain.entity.flow.task;

import java.time.LocalDateTime;

/**
 * 待办任务实体
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
public class TodoTask {
    private Long id;
    private Long documentId;      // 公文ID
    private Long flowInstanceId;   // 流程实例ID
    private Long nodeInstanceId;   // 节点实例ID
    private Long assigneeId;       // 处理人ID
    private String title;          // 待办标题
    private String content;        // 待办内容
    private Integer taskType;      // 任务类型
    private Integer priority;      // 优先级
    private Integer status;         // 状态
    private LocalDateTime dueDate;  // 截止时间
    private LocalDateTime handledAt; // 处理时间
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 任务类型常量
    public static final int TASK_TYPE_USER = 1;    // 用户任务
    public static final int TASK_TYPE_JAVA = 2;   // JAVA任务
    public static final int TASK_TYPE_OTHER = 3;   // 其他任务
    
    // 优先级常量
    public static final int PRIORITY_NORMAL = 0;  // 普通
    public static final int PRIORITY_URGENT = 1;  // 紧急
    public static final int PRIORITY_CRITICAL = 2; // 特急
    
    // 状态常量
    public static final int STATUS_PENDING = 0;    // 待处理
    public static final int STATUS_HANDLED = 1;     // 已处理
    public static final int STATUS_CANCELLED = 2;   // 已取消
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    
    public Long getFlowInstanceId() { return flowInstanceId; }
    public void setFlowInstanceId(Long flowInstanceId) { this.flowInstanceId = flowInstanceId; }
    
    public Long getNodeInstanceId() { return nodeInstanceId; }
    public void setNodeInstanceId(Long nodeInstanceId) { this.nodeInstanceId = nodeInstanceId; }
    
    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Integer getTaskType() { return taskType; }
    public void setTaskType(Integer taskType) { this.taskType = taskType; }
    
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    
    public LocalDateTime getHandledAt() { return handledAt; }
    public void setHandledAt(LocalDateTime handledAt) { this.handledAt = handledAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

