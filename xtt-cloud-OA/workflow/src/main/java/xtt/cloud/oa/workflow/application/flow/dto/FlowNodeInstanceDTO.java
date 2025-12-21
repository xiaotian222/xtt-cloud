package xtt.cloud.oa.workflow.application.flow.dto;

import java.time.LocalDateTime;

/**
 * 节点实例DTO
 * 
 * @author xtt
 */
public class FlowNodeInstanceDTO {
    
    private Long id;
    private Long flowInstanceId;
    private Long nodeId;
    private Long approverId;
    private String approverName;
    private Long approverDeptId;
    private String approverDeptName;
    private Integer status;
    private String statusDesc;
    private String comments;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public FlowNodeInstanceDTO() {
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getFlowInstanceId() {
        return flowInstanceId;
    }
    
    public void setFlowInstanceId(Long flowInstanceId) {
        this.flowInstanceId = flowInstanceId;
    }
    
    public Long getNodeId() {
        return nodeId;
    }
    
    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }
    
    public Long getApproverId() {
        return approverId;
    }
    
    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }
    
    public String getApproverName() {
        return approverName;
    }
    
    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }
    
    public Long getApproverDeptId() {
        return approverDeptId;
    }
    
    public void setApproverDeptId(Long approverDeptId) {
        this.approverDeptId = approverDeptId;
    }
    
    public String getApproverDeptName() {
        return approverDeptName;
    }
    
    public void setApproverDeptName(String approverDeptName) {
        this.approverDeptName = approverDeptName;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getStatusDesc() {
        return statusDesc;
    }
    
    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public LocalDateTime getHandledAt() {
        return handledAt;
    }
    
    public void setHandledAt(LocalDateTime handledAt) {
        this.handledAt = handledAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

