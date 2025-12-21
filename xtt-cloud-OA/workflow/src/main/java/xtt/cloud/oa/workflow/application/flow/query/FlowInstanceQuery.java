package xtt.cloud.oa.workflow.application.flow.query;

import java.util.List;

/**
 * 流程实例查询对象
 * 
 * @author xtt
 */
public class FlowInstanceQuery {
    
    private Long id;
    private Long documentId;
    private Long flowDefId;
    private Integer flowType;
    private Integer status;
    private Long initiatorId;
    private List<Long> flowInstanceIds;
    private Integer pageNum;
    private Integer pageSize;
    
    public FlowInstanceQuery() {
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getDocumentId() {
        return documentId;
    }
    
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
    
    public Long getFlowDefId() {
        return flowDefId;
    }
    
    public void setFlowDefId(Long flowDefId) {
        this.flowDefId = flowDefId;
    }
    
    public Integer getFlowType() {
        return flowType;
    }
    
    public void setFlowType(Integer flowType) {
        this.flowType = flowType;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Long getInitiatorId() {
        return initiatorId;
    }
    
    public void setInitiatorId(Long initiatorId) {
        this.initiatorId = initiatorId;
    }
    
    public List<Long> getFlowInstanceIds() {
        return flowInstanceIds;
    }
    
    public void setFlowInstanceIds(List<Long> flowInstanceIds) {
        this.flowInstanceIds = flowInstanceIds;
    }
    
    public Integer getPageNum() {
        return pageNum;
    }
    
    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }
    
    public Integer getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}

