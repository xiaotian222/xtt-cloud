package xtt.cloud.oa.workflow.domain.flow.model.entity.history;

import xtt.cloud.oa.workflow.domain.flow.model.valueobject.history.ActivityType;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 活动历史记录实体
 * 
 * 记录每个节点的执行记录
 * 
 * @author xtt
 */
public class ActivityHistory {
    
    private Long id;
    private Long flowInstanceId;        // 流程实例ID
    private Long nodeInstanceId;        // 节点实例ID
    private Long nodeId;                 // 节点定义ID
    private String nodeName;            // 节点名称
    private Integer nodeType;            // 节点类型（1:审批,2:抄送,3:条件,4:自动,5:自由流）
    private ActivityType activityType;  // 活动类型
    private Long operatorId;             // 操作人ID
    private String operatorName;         // 操作人姓名
    private String operation;            // 操作（create,start,complete,reject,skip）
    private String comments;             // 备注
    private LocalDateTime activityTime;  // 活动时间
    private String extraInfo;            // 扩展信息（JSON格式）
    private LocalDateTime createdAt;      // 记录创建时间
    
    /**
     * 创建活动历史（工厂方法）
     */
    public static ActivityHistory create(
            Long flowInstanceId,
            Long nodeInstanceId,
            Long nodeId,
            String nodeName,
            Integer nodeType,
            ActivityType activityType,
            Long operatorId,
            String comments) {
        
        if (flowInstanceId == null || flowInstanceId <= 0) {
            throw new IllegalArgumentException("流程实例ID必须大于0");
        }
        if (nodeId == null || nodeId <= 0) {
            throw new IllegalArgumentException("节点ID必须大于0");
        }
        if (activityType == null) {
            throw new IllegalArgumentException("活动类型不能为空");
        }
        
        ActivityHistory history = new ActivityHistory();
        history.flowInstanceId = flowInstanceId;
        history.nodeInstanceId = nodeInstanceId;
        history.nodeId = nodeId;
        history.nodeName = nodeName;
        history.nodeType = nodeType;
        history.activityType = activityType;
        history.operatorId = operatorId;
        history.operation = activityType.getOperation();
        history.comments = comments;
        history.activityTime = LocalDateTime.now();
        history.createdAt = LocalDateTime.now();
        
        return history;
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public Long getFlowInstanceId() {
        return flowInstanceId;
    }
    
    public Long getNodeInstanceId() {
        return nodeInstanceId;
    }
    
    public Long getNodeId() {
        return nodeId;
    }
    
    public String getNodeName() {
        return nodeName;
    }
    
    public Integer getNodeType() {
        return nodeType;
    }
    
    public ActivityType getActivityType() {
        return activityType;
    }
    
    public Long getOperatorId() {
        return operatorId;
    }
    
    public String getOperatorName() {
        return operatorName;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public String getComments() {
        return comments;
    }
    
    public LocalDateTime getActivityTime() {
        return activityTime;
    }
    
    public String getExtraInfo() {
        return extraInfo;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    // Setters (仅用于持久化层重建对象)
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setFlowInstanceId(Long flowInstanceId) {
        this.flowInstanceId = flowInstanceId;
    }
    
    public void setNodeInstanceId(Long nodeInstanceId) {
        this.nodeInstanceId = nodeInstanceId;
    }
    
    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }
    
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
    
    public void setNodeType(Integer nodeType) {
        this.nodeType = nodeType;
    }
    
    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
        if (activityType != null) {
            this.operation = activityType.getOperation();
        }
    }
    
    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }
    
    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }
    
    public void setOperation(String operation) {
        this.operation = operation;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public void setActivityTime(LocalDateTime activityTime) {
        this.activityTime = activityTime;
    }
    
    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityHistory that = (ActivityHistory) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "ActivityHistory{" +
                "id=" + id +
                ", nodeName='" + nodeName + '\'' +
                ", activityType=" + activityType +
                ", operation='" + operation + '\'' +
                '}';
    }
}

