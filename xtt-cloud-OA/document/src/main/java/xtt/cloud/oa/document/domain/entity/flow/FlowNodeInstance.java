package xtt.cloud.oa.document.domain.entity.flow;

import java.time.LocalDateTime;

/**
 * 节点实例实体
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
public class FlowNodeInstance {
    private Long id;
    private Long flowInstanceId;   // 流程实例ID
    private Long nodeId;            // 节点定义ID
    private Long approverId;        // 审批人ID
    private Long approverDeptId;    // 审批人部门ID
    private Integer status;          // 节点状态
    private String comments;         // 审批意见
    private LocalDateTime handledAt; // 处理时间
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 节点状态常量
    public static final int STATUS_PENDING = 0;    // 待处理
    public static final int STATUS_PROCESSING = 1; // 处理中
    public static final int STATUS_COMPLETED = 2;  // 已完成
    public static final int STATUS_REJECTED = 3;    // 已拒绝
    public static final int STATUS_SKIPPED = 4;     // 已跳过
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getFlowInstanceId() { return flowInstanceId; }
    public void setFlowInstanceId(Long flowInstanceId) { this.flowInstanceId = flowInstanceId; }
    
    public Long getNodeId() { return nodeId; }
    public void setNodeId(Long nodeId) { this.nodeId = nodeId; }
    
    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }
    
    public Long getApproverDeptId() { return approverDeptId; }
    public void setApproverDeptId(Long approverDeptId) { this.approverDeptId = approverDeptId; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    
    public LocalDateTime getHandledAt() { return handledAt; }
    public void setHandledAt(LocalDateTime handledAt) { this.handledAt = handledAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}








