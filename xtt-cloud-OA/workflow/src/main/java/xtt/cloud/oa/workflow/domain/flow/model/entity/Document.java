package xtt.cloud.oa.workflow.domain.flow.model.entity;

/**
 * 文档实体（领域实体）
 * 
 * 注意：此实体引用现有的 Document PO
 * 
 * @author xtt
 */
public class Document {
    
    private final xtt.cloud.oa.workflow.infrastructure.persistence.pojo.Document po;
    
    public Document(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.Document po) {
        this.po = po;
    }
    
    /**
     * 判断文档是否为草稿状态
     */
    public boolean isDraft() {
        return po != null && po.getStatus() != null 
                && po.getStatus() == xtt.cloud.oa.workflow.infrastructure.persistence.pojo.Document.STATUS_DRAFT;
    }
    
    /**
     * 判断文档是否在审核中
     */
    public boolean isReviewing() {
        return po != null && po.getStatus() != null 
                && po.getStatus() == xtt.cloud.oa.workflow.infrastructure.persistence.pojo.Document.STATUS_REVIEWING;
    }
    
    // 委托方法
    public Long getId() {
        return po != null ? po.getId() : null;
    }
    
    public String getTitle() {
        return po != null ? po.getTitle() : null;
    }
    
    public String getDocNumber() {
        return po != null ? po.getDocNumber() : null;
    }
    
    public Long getDocTypeId() {
        return po != null ? po.getDocTypeId() : null;
    }
    
    public Integer getStatus() {
        return po != null ? po.getStatus() : null;
    }
    
    public Long getCreatorId() {
        return po != null ? po.getCreatorId() : null;
    }
    
    public Long getDeptId() {
        return po != null ? po.getDeptId() : null;
    }
    
    /**
     * 获取持久化对象
     */
    public xtt.cloud.oa.workflow.infrastructure.persistence.pojo.Document getPO() {
        return po;
    }
}

