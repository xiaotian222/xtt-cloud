package xtt.cloud.oa.workflow.application.flow.query;

import java.util.List;

/**
 * 待办任务查询对象
 * 
 * @author xtt
 */
public class TodoTaskQuery {
    
    private Long assigneeId;
    private Long flowInstanceId;
    private Long nodeInstanceId;
    private Integer taskType;
    private Integer priority;
    private List<Long> taskIds;
    private Integer pageNum;
    private Integer pageSize;
    
    public TodoTaskQuery() {
    }
    
    public Long getAssigneeId() {
        return assigneeId;
    }
    
    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
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
    
    public Integer getTaskType() {
        return taskType;
    }
    
    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public List<Long> getTaskIds() {
        return taskIds;
    }
    
    public void setTaskIds(List<Long> taskIds) {
        this.taskIds = taskIds;
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

