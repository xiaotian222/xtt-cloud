package xtt.cloud.oa.workflow.infrastructure.persistence.pojo;

import java.time.LocalDateTime;

/**
 * 任务历史记录实体
 * 记录任务的完整历史信息
 * 
 * @author xtt
 * @since 2023.0.3.3
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
    private Long handlerDeptId;           // 处理人部门ID
    private String handlerDeptName;      // 处理人部门名称
    private Integer taskType;            // 任务类型（1:用户任务,2:JAVA任务,3:其他任务）
    private String action;               // 操作类型（approve,reject,forward,return,delegate）
    private String actionName;           // 操作名称
    private String comments;             // 审批意见
    private LocalDateTime startTime;     // 开始时间
    private LocalDateTime endTime;       // 结束时间
    private Long duration;               // 持续时间（秒）
    private Integer status;              // 任务状态（0:待处理,1:处理中,2:已完成,3:已拒绝,4:已跳过）
    private LocalDateTime createdAt;     // 记录创建时间
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getFlowInstanceId() { return flowInstanceId; }
    public void setFlowInstanceId(Long flowInstanceId) { this.flowInstanceId = flowInstanceId; }
    
    public Long getNodeInstanceId() { return nodeInstanceId; }
    public void setNodeInstanceId(Long nodeInstanceId) { this.nodeInstanceId = nodeInstanceId; }
    
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    
    public String getDocumentTitle() { return documentTitle; }
    public void setDocumentTitle(String documentTitle) { this.documentTitle = documentTitle; }
    
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    
    public Long getHandlerId() { return handlerId; }
    public void setHandlerId(Long handlerId) { this.handlerId = handlerId; }
    
    public String getHandlerName() { return handlerName; }
    public void setHandlerName(String handlerName) { this.handlerName = handlerName; }
    
    public Long getHandlerDeptId() { return handlerDeptId; }
    public void setHandlerDeptId(Long handlerDeptId) { this.handlerDeptId = handlerDeptId; }
    
    public String getHandlerDeptName() { return handlerDeptName; }
    public void setHandlerDeptName(String handlerDeptName) { this.handlerDeptName = handlerDeptName; }
    
    public Integer getTaskType() { return taskType; }
    public void setTaskType(Integer taskType) { this.taskType = taskType; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

