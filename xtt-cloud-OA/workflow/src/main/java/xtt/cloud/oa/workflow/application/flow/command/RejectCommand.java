package xtt.cloud.oa.workflow.application.flow.command;

/**
 * 审批拒绝命令
 * 
 * @author xtt
 */
public class RejectCommand {
    
    private Long flowInstanceId;
    private Long nodeInstanceId;
    private Long approverId;
    private String comments;
    private Long rollbackToNodeId; // 回退到的节点ID（可选）
    
    public RejectCommand() {
    }
    
    public RejectCommand(Long flowInstanceId, Long nodeInstanceId, 
                        Long approverId, String comments) {
        this.flowInstanceId = flowInstanceId;
        this.nodeInstanceId = nodeInstanceId;
        this.approverId = approverId;
        this.comments = comments;
    }
    
    public Long getFlowInstanceId() {
        return flowInstanceId;
    }
    
    public void setFlowInstanceId(Long flowInstanceId) {
        this.flowInstanceId = flowInstanceId;
    }
    
    public Long getNodeInstanceId() {
        return nodeInstanceId;
    }
    
    public void setNodeInstanceId(Long nodeInstanceId) {
        this.nodeInstanceId = nodeInstanceId;
    }
    
    public Long getApproverId() {
        return approverId;
    }
    
    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public Long getRollbackToNodeId() {
        return rollbackToNodeId;
    }
    
    public void setRollbackToNodeId(Long rollbackToNodeId) {
        this.rollbackToNodeId = rollbackToNodeId;
    }
    
    public boolean hasRollback() {
        return rollbackToNodeId != null;
    }
}

