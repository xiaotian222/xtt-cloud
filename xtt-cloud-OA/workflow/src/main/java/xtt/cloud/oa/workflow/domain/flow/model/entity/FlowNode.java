package xtt.cloud.oa.workflow.domain.flow.model.entity;

/**
 * 流程节点实体（领域实体）
 * 
 * 注意：此实体引用现有的 FlowNode PO
 * 
 * @author xtt
 */
public class FlowNode {
    
    private final xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode po;
    
    public FlowNode(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode po) {
        this.po = po;
    }
    
    /**
     * 判断是否为并行模式（会签或或签）
     */
    public boolean isParallelMode() {
        return po != null && po.isParallelMode();
    }
    
    /**
     * 判断是否为会签模式
     */
    public boolean isParallelAllMode() {
        return po != null && po.isParallelAllMode();
    }
    
    /**
     * 判断是否为或签模式
     */
    public boolean isParallelAnyMode() {
        return po != null && po.isParallelAnyMode();
    }
    
    /**
     * 判断是否为条件节点
     */
    public boolean isConditionNode() {
        return po != null && po.getNodeType() != null 
                && po.getNodeType() == xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode.NODE_TYPE_CONDITION;
    }
    
    // 委托方法
    public Long getId() {
        return po != null ? po.getId() : null;
    }
    
    public Long getFlowDefId() {
        return po != null ? po.getFlowDefId() : null;
    }
    
    public String getNodeName() {
        return po != null ? po.getNodeName() : null;
    }
    
    public Integer getNodeType() {
        return po != null ? po.getNodeType() : null;
    }
    
    public Integer getApproverType() {
        return po != null ? po.getApproverType() : null;
    }
    
    public String getApproverValue() {
        return po != null ? po.getApproverValue() : null;
    }
    
    public Integer getOrderNum() {
        return po != null ? po.getOrderNum() : null;
    }
    
    public Long getNextNodeId() {
        return po != null ? po.getNextNodeId() : null;
    }
    
    public String getNextNodeIds() {
        return po != null ? po.getNextNodeIds() : null;
    }
    
    public String getSkipCondition() {
        return po != null ? po.getSkipCondition() : null;
    }
    
    public Integer getParallelMode() {
        return po != null ? po.getParallelMode() : null;
    }
    
    /**
     * 获取持久化对象
     */
    public xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode getPO() {
        return po;
    }
}

