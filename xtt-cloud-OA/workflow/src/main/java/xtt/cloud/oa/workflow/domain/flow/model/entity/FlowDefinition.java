package xtt.cloud.oa.workflow.domain.flow.model.entity;

/**
 * 流程定义实体（领域实体）
 * 
 * 注意：此实体引用现有的 FlowDefinition PO
 * 
 * @author xtt
 */
@Deprecated
public class FlowDefinition {
    
    private final xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowDefinition po;
    
    public FlowDefinition(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowDefinition po) {
        this.po = po;
    }
    
    /**
     * 判断流程定义是否启用
     */
    public boolean isEnabled() {
        return po != null && po.getStatus() != null 
                && po.getStatus() == xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowDefinition.STATUS_ENABLED;
    }
    
    /**
     * 判断流程定义是否停用
     */
    public boolean isDisabled() {
        return !isEnabled();
    }
    
    // 委托方法
    public Long getId() {
        return po != null ? po.getId() : null;
    }
    
    public String getName() {
        return po != null ? po.getName() : null;
    }
    
    public String getCode() {
        return po != null ? po.getCode() : null;
    }
    
    public Long getDocTypeId() {
        return po != null ? po.getDocTypeId() : null;
    }
    
    public String getDescription() {
        return po != null ? po.getDescription() : null;
    }
    
    public Integer getVersion() {
        return po != null ? po.getVersion() : null;
    }
    
    public Integer getStatus() {
        return po != null ? po.getStatus() : null;
    }
    
    public Long getCreatorId() {
        return po != null ? po.getCreatorId() : null;
    }
    
    /**
     * 获取持久化对象
     */
    public xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowDefinition getPO() {
        return po;
    }
}

