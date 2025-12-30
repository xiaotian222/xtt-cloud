package xtt.cloud.oa.workflow.domain.flow.model.entity.history;

import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskAction;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.NodeStatus;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 任务历史记录实体
 * 
 * 记录任务的完整历史信息
 * 
 * @author xtt
 */
public class TaskHistory {
    
    private Long id;
    private Long flowInstanceId;        // 流程实例ID
    private Long nodeInstanceId;        // 节点实例ID
    private Long documentId;             // 文档ID
    private String documentTitle;        // 文档标题
    private String taskName;             // 任务名称（节点名称）
    private Long handlerId;              // 处理人ID
    private String handlerName;          // 处理人姓名
    private Long handlerDeptId;          // 处理人部门ID
    private String handlerDeptName;      // 处理人部门名称
    private TaskType taskType;           // 任务类型
    private TaskAction action;           // 操作类型
    private String actionName;           // 操作名称
    private String comments;             // 审批意见
    private LocalDateTime startTime;     // 开始时间
    private LocalDateTime endTime;        // 结束时间
    private Long duration;               // 持续时间（秒）
    private NodeStatus status;           // 任务状态
    private LocalDateTime createdAt;      // 记录创建时间
    
    /**
     * 创建任务历史（工厂方法）
     */
    public static TaskHistory create(
            Long flowInstanceId,
            Long nodeInstanceId,
            Long documentId,
            String documentTitle,
            String taskName,
            Long handlerId,
            TaskType taskType,
            TaskAction action,
            String comments,
            LocalDateTime startTime,
            LocalDateTime endTime,
            NodeStatus status) {
        
        if (flowInstanceId == null || flowInstanceId <= 0) {
            throw new IllegalArgumentException("流程实例ID必须大于0");
        }
        if (nodeInstanceId == null || nodeInstanceId <= 0) {
            throw new IllegalArgumentException("节点实例ID必须大于0");
        }
        if (handlerId == null || handlerId <= 0) {
            throw new IllegalArgumentException("处理人ID必须大于0");
        }
        if (action == null) {
            throw new IllegalArgumentException("操作类型不能为空");
        }
        
        TaskHistory history = new TaskHistory();
        history.flowInstanceId = flowInstanceId;
        history.nodeInstanceId = nodeInstanceId;
        history.documentId = documentId;
        history.documentTitle = documentTitle;
        history.taskName = taskName;
        history.handlerId = handlerId;
        history.taskType = taskType != null ? taskType : TaskType.USER;
        history.action = action;
        history.actionName = action.getName();
        history.comments = comments;
        history.startTime = startTime;
        history.endTime = endTime;
        history.status = status;
        history.createdAt = LocalDateTime.now();
        
        // 计算持续时间
        if (startTime != null && endTime != null) {
            history.duration = java.time.Duration.between(startTime, endTime).getSeconds();
        }
        
        return history;
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public Long getFlowInstanceId() {
        return flowInstanceId;
    }
    
    public Long getNodeInstanceId() {
        return nodeInstanceId;
    }
    
    public Long getDocumentId() {
        return documentId;
    }
    
    public String getDocumentTitle() {
        return documentTitle;
    }
    
    public String getTaskName() {
        return taskName;
    }
    
    public Long getHandlerId() {
        return handlerId;
    }
    
    public String getHandlerName() {
        return handlerName;
    }
    
    public Long getHandlerDeptId() {
        return handlerDeptId;
    }
    
    public String getHandlerDeptName() {
        return handlerDeptName;
    }
    
    public TaskType getTaskType() {
        return taskType;
    }
    
    public TaskAction getAction() {
        return action;
    }
    
    public String getActionName() {
        return actionName;
    }
    
    public String getComments() {
        return comments;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public Long getDuration() {
        return duration;
    }
    
    public NodeStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    // Setters (仅用于持久化层重建对象)
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setFlowInstanceId(Long flowInstanceId) {
        this.flowInstanceId = flowInstanceId;
    }
    
    public void setNodeInstanceId(Long nodeInstanceId) {
        this.nodeInstanceId = nodeInstanceId;
    }
    
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
    
    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }
    
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
    
    public void setHandlerId(Long handlerId) {
        this.handlerId = handlerId;
    }
    
    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }
    
    public void setHandlerDeptId(Long handlerDeptId) {
        this.handlerDeptId = handlerDeptId;
    }
    
    public void setHandlerDeptName(String handlerDeptName) {
        this.handlerDeptName = handlerDeptName;
    }
    
    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }
    
    public void setAction(TaskAction action) {
        this.action = action;
        if (action != null) {
            this.actionName = action.getName();
        }
    }
    
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public void setDuration(Long duration) {
        this.duration = duration;
    }
    
    public void setStatus(NodeStatus status) {
        this.status = status;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskHistory that = (TaskHistory) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "TaskHistory{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", action=" + action +
                ", handlerId=" + handlerId +
                '}';
    }
}

