package xtt.cloud.oa.workflow.domain.flow.model.entity.task;

import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskAction;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 已办任务实体
 * 
 * 实体特点：
 * 1. 有唯一标识（ID）
 * 2. 不可变（Immutable）- 已办任务创建后不再修改
 * 3. 通过ID相等性比较
 * 
 * @author xtt
 */
public class DoneTask {
    
    private Long id;
    private Long documentId;      // 公文ID
    private Long flowInstanceId;  // 流程实例ID
    private Long nodeInstanceId; // 节点实例ID
    private Long handlerId;      // 处理人ID
    private String title;         // 已办标题
    private TaskType taskType;   // 任务类型
    private TaskAction action;   // 操作类型
    private String comments;     // 处理意见
    private LocalDateTime handledAt; // 处理时间
    private LocalDateTime createdAt;
    
    /**
     * 创建已办任务（工厂方法）
     */
    public static DoneTask create(
            Long documentId,
            Long flowInstanceId,
            Long nodeInstanceId,
            Long handlerId,
            String title,
            TaskType taskType,
            TaskAction action,
            String comments) {
        
        if (documentId == null || documentId <= 0) {
            throw new IllegalArgumentException("文档ID必须大于0");
        }
        if (flowInstanceId == null || flowInstanceId <= 0) {
            throw new IllegalArgumentException("流程实例ID必须大于0");
        }
        if (nodeInstanceId == null || nodeInstanceId <= 0) {
            throw new IllegalArgumentException("节点实例ID必须大于0");
        }
        if (handlerId == null || handlerId <= 0) {
            throw new IllegalArgumentException("处理人ID必须大于0");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("已办标题不能为空");
        }
        if (action == null) {
            throw new IllegalArgumentException("操作类型不能为空");
        }
        
        DoneTask task = new DoneTask();
        task.documentId = documentId;
        task.flowInstanceId = flowInstanceId;
        task.nodeInstanceId = nodeInstanceId;
        task.handlerId = handlerId;
        task.title = title.trim();
        task.taskType = taskType != null ? taskType : TaskType.USER;
        task.action = action;
        task.comments = comments != null ? comments.trim() : null;
        task.handledAt = LocalDateTime.now();
        task.createdAt = LocalDateTime.now();
        
        return task;
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
    
    public Long getHandlerId() {
        return handlerId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public TaskType getTaskType() {
        return taskType;
    }
    
    public TaskAction getAction() {
        return action;
    }
    
    public String getComments() {
        return comments;
    }
    
    public LocalDateTime getHandledAt() {
        return handledAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
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
    
    public void setHandlerId(Long handlerId) {
        this.handlerId = handlerId;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }
    
    public void setAction(TaskAction action) {
        this.action = action;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public void setHandledAt(LocalDateTime handledAt) {
        this.handledAt = handledAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoneTask doneTask = (DoneTask) o;
        return Objects.equals(id, doneTask.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "DoneTask{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", action=" + action +
                ", handlerId=" + handlerId +
                '}';
    }
}

