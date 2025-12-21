package xtt.cloud.oa.workflow.infrastructure.persistence.pojo;

import java.time.LocalDateTime;

/**
 * 承办记录实体
 */
public class Handling {
    private Long id;
    private Long flowInstanceId;
    private Long nodeInstanceId;
    private Long handlerId;
    private Long handlerDeptId;
    private String handlingContent;
    private String handlingResult;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status; // 0:进行中,1:已完成,2:已退回
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 承办状态常量
    public static final int STATUS_PROCESSING = 0;  // 进行中
    public static final int STATUS_COMPLETED = 1;    // 已完成
    public static final int STATUS_REJECTED = 2;     // 已退回
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getFlowInstanceId() { return flowInstanceId; }
    public void setFlowInstanceId(Long flowInstanceId) { this.flowInstanceId = flowInstanceId; }
    
    public Long getNodeInstanceId() { return nodeInstanceId; }
    public void setNodeInstanceId(Long nodeInstanceId) { this.nodeInstanceId = nodeInstanceId; }
    
    public Long getHandlerId() { return handlerId; }
    public void setHandlerId(Long handlerId) { this.handlerId = handlerId; }
    
    public Long getHandlerDeptId() { return handlerDeptId; }
    public void setHandlerDeptId(Long handlerDeptId) { this.handlerDeptId = handlerDeptId; }
    
    public String getHandlingContent() { return handlingContent; }
    public void setHandlingContent(String handlingContent) { this.handlingContent = handlingContent; }
    
    public String getHandlingResult() { return handlingResult; }
    public void setHandlingResult(String handlingResult) { this.handlingResult = handlingResult; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}