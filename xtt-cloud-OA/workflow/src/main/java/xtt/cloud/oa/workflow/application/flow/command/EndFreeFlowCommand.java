package xtt.cloud.oa.workflow.application.flow.command;

/**
 * 结束自由流转命令
 * 
 * @author xtt
 */
public class EndFreeFlowCommand {
    
    /**
     * 流程实例ID
     */
    private Long flowInstanceId;
    
    /**
     * 节点实例ID（结束自由流转的节点）
     */
    private Long nodeInstanceId;
    
    /**
     * 操作人ID
     */
    private Long operatorId;
    
    /**
     * 备注
     */
    private String comments;
    
    // Getters and Setters
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
    
    public Long getOperatorId() {
        return operatorId;
    }
    
    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
}

