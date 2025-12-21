package xtt.cloud.oa.workflow.infrastructure.persistence.pojo;

import java.time.LocalDateTime;

/**
 * 活动历史记录实体
 * 记录每个节点的执行记录
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
public class ActivityHistory {
    
    private Long id;
    private Long flowInstanceId;        // 流程实例ID
    private Long nodeInstanceId;        // 节点实例ID
    private Long nodeId;                 // 节点定义ID
    private String nodeName;            // 节点名称
    private Integer nodeType;            // 节点类型（1:审批,2:抄送,3:条件,4:自动,5:自由流）
    private Integer activityType;        // 活动类型（1:节点创建,2:节点开始,3:节点完成,4:节点拒绝,5:节点跳过）
    private Long operatorId;             // 操作人ID
    private String operatorName;         // 操作人姓名
    private String operation;            // 操作（create,start,complete,reject,skip）
    private String comments;             // 备注
    private LocalDateTime activityTime;  // 活动时间
    private String extraInfo;            // 扩展信息（JSON格式）
    private LocalDateTime createdAt;     // 记录创建时间
    
    // 活动类型常量
    public static final int ACTIVITY_TYPE_CREATE = 1;    // 节点创建
    public static final int ACTIVITY_TYPE_START = 2;     // 节点开始
    public static final int ACTIVITY_TYPE_COMPLETE = 3;  // 节点完成
    public static final int ACTIVITY_TYPE_REJECT = 4;    // 节点拒绝
    public static final int ACTIVITY_TYPE_SKIP = 5;      // 节点跳过
    
    // 操作常量
    public static final String OPERATION_CREATE = "create";
    public static final String OPERATION_START = "start";
    public static final String OPERATION_COMPLETE = "complete";
    public static final String OPERATION_REJECT = "reject";
    public static final String OPERATION_SKIP = "skip";
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getFlowInstanceId() { return flowInstanceId; }
    public void setFlowInstanceId(Long flowInstanceId) { this.flowInstanceId = flowInstanceId; }
    
    public Long getNodeInstanceId() { return nodeInstanceId; }
    public void setNodeInstanceId(Long nodeInstanceId) { this.nodeInstanceId = nodeInstanceId; }
    
    public Long getNodeId() { return nodeId; }
    public void setNodeId(Long nodeId) { this.nodeId = nodeId; }
    
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    
    public Integer getNodeType() { return nodeType; }
    public void setNodeType(Integer nodeType) { this.nodeType = nodeType; }
    
    public Integer getActivityType() { return activityType; }
    public void setActivityType(Integer activityType) { this.activityType = activityType; }
    
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    
    public LocalDateTime getActivityTime() { return activityTime; }
    public void setActivityTime(LocalDateTime activityTime) { this.activityTime = activityTime; }
    
    public String getExtraInfo() { return extraInfo; }
    public void setExtraInfo(String extraInfo) { this.extraInfo = extraInfo; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

