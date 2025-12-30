package xtt.cloud.oa.workflow.domain.flow.model.entity.task;

import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskStatus;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskPriority;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 待办任务实体
 * 
 * 实体特点：
 * 1. 有唯一标识（ID）
 * 2. 可变（Mutable）
 * 3. 通过ID相等性比较
 * 
 * @author xtt
 */
public class TodoTask {
    
    private Long id;
    private Long documentId;      // 公文ID
    private Long flowInstanceId;  // 流程实例ID
    private Long nodeInstanceId;  // 节点实例ID
    private Long assigneeId;      // 处理人ID
    private String title;         // 待办标题
    private String content;       // 待办内容
    private TaskType taskType;    // 任务类型 
    private TaskPriority priority; // 优先级 ?
    private TaskStatus status;    // 状态
    private LocalDateTime dueDate; // 截止时间
    private LocalDateTime handledAt; // 处理时间
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 创建待办任务（工厂方法）
     */
    public static TodoTask create(
            Long documentId,
            Long flowInstanceId,
            Long nodeInstanceId,
            Long assigneeId,
            String title,
            String content,
            TaskType taskType,
            TaskPriority priority,
            LocalDateTime dueDate) {
        
        if (documentId == null || documentId <= 0) {
            throw new IllegalArgumentException("文档ID必须大于0");
        }
        if (flowInstanceId == null || flowInstanceId <= 0) {
            throw new IllegalArgumentException("流程实例ID必须大于0");
        }
        if (nodeInstanceId == null || nodeInstanceId <= 0) {
            throw new IllegalArgumentException("节点实例ID必须大于0");
        }
        if (assigneeId == null || assigneeId <= 0) {
            throw new IllegalArgumentException("处理人ID必须大于0");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("待办标题不能为空");
        }
        
        TodoTask task = new TodoTask();
        task.documentId = documentId;
        task.flowInstanceId = flowInstanceId;
        task.nodeInstanceId = nodeInstanceId;
        task.assigneeId = assigneeId;
        task.title = title.trim();
        task.content = content != null ? content.trim() : null;
        task.taskType = taskType != null ? taskType : TaskType.USER;
        task.priority = priority != null ? priority : TaskPriority.NORMAL;
        task.status = TaskStatus.PENDING;
        task.dueDate = dueDate;
        task.createdAt = LocalDateTime.now();
        task.updatedAt = LocalDateTime.now();
        
        return task;
    }
    
    /**
     * 标记为已处理
     */
    public void markAsHandled() {
        if (!status.canHandle()) {
            throw new IllegalStateException("任务状态不允许处理，当前状态: " + status);
        }
        this.status = TaskStatus.COMPLETED;
        this.handledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 取消任务
     */
    public void cancel() {
        if (status.isFinished()) {
            throw new IllegalStateException("已完成的任务不能取消");
        }
        this.status = TaskStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 判断是否已过期
     */
    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate) && !status.isFinished();
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public Long getDocumentId() {
        return documentId;
    }
    
    public Long getFlowInstanceId() {
        return flowInstanceId;
    }
    
    public Long getNodeInstanceId() {
        return nodeInstanceId;
    }
    
    public Long getAssigneeId() {
        return assigneeId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getContent() {
        return content;
    }
    
    public TaskType getTaskType() {
        return taskType;
    }
    
    public TaskPriority getPriority() {
        return priority;
    }
    
    public TaskStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public LocalDateTime getHandledAt() {
        return handledAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    // Setters (仅用于持久化层重建对象)
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
    
    public void setFlowInstanceId(Long flowInstanceId) {
        this.flowInstanceId = flowInstanceId;
    }
    
    public void setNodeInstanceId(Long nodeInstanceId) {
        this.nodeInstanceId = nodeInstanceId;
    }
    
    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }
    
    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }
    
    public void setStatus(TaskStatus status) {
        this.status = status;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    
    public void setHandledAt(LocalDateTime handledAt) {
        this.handledAt = handledAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TodoTask todoTask = (TodoTask) o;
        return Objects.equals(id, todoTask.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "TodoTask{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", assigneeId=" + assigneeId +
                '}';
    }
}

