package xtt.cloud.oa.workflow.application.flow.query;

/**
 * 流程历史查询对象
 * 
 * @author xtt
 */
public class FlowHistoryQuery {
    
    private Long flowInstanceId;
    private Long documentId;
    private Long userId;
    private Integer activityType;
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    
    public Long getFlowInstanceId() {
        return flowInstanceId;
    }
    
    public void setFlowInstanceId(Long flowInstanceId) {
        this.flowInstanceId = flowInstanceId;
    }
    
    public Long getDocumentId() {
        return documentId;
    }
    
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Integer getActivityType() {
        return activityType;
    }
    
    public void setActivityType(Integer activityType) {
        this.activityType = activityType;
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

