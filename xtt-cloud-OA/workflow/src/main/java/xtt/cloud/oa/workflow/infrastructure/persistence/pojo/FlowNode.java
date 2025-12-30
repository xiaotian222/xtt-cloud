package xtt.cloud.oa.workflow.infrastructure.persistence.pojo;

import java.time.LocalDateTime;

/**
 * 流程节点定义实体
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
public class FlowNode {
    private Long id;
    private Long flowDefId;        // 流程定义ID
    private String nodeName;       // 节点名称
    private Integer nodeType;      // 节点类型
    private Integer approverType;   // 审批人类型
    private String approverValue;   // 审批人值
    private Integer orderNum;       // 节点顺序
    private Long previousNodeId;    // 上一个节点ID（用于反向查找）
    private Long nextNodeId;        // 下一个节点ID（单个，用于串行流程）
    private String nextNodeIds;     // 下一个节点ID列表（JSON格式，用于并行分支）
    private String skipCondition;  // 跳过条件
    private Integer required;       // 是否必须(0:可跳过,1:必须)

//    private Integer parallelMode;   // 并行模式(0:串行,1:并行-会签,2:并行-或签) - 已废弃，使用网关模式
    private Integer gatewayType;    // 网关类型(0:非网关,1:并行Split,2:并行Join,3:条件Split,4:条件Join)
    private Integer gatewayMode;    // 并行网关模式(1:会签,2:或签) - 仅用于并行网关
    private Long gatewayId;         // 网关ID（用于关联Split和Join）
    private String conditionExpression; // 条件表达式（SpEL格式，仅用于条件网关）

    private Integer isFreeFlow;     // 是否为自由流节点(0:否,1:是)
    private Integer allowFreeFlow; // 是否允许在此节点使用自由流(0:不允许,1:允许)
    private Integer isLastNode;     // 是否为最后一个节点(0:否,1:是)
    private String flowActionIds;   // FlowAction ID列表（JSON格式字符串，如：["1","2","3"]）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getFlowDefId() { return flowDefId; }
    public void setFlowDefId(Long flowDefId) { this.flowDefId = flowDefId; }
    
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    
    public Integer getNodeType() { return nodeType; }
    public void setNodeType(Integer nodeType) { this.nodeType = nodeType; }
    
    public Integer getApproverType() { return approverType; }
    public void setApproverType(Integer approverType) { this.approverType = approverType; }
    
    public String getApproverValue() { return approverValue; }
    public void setApproverValue(String approverValue) { this.approverValue = approverValue; }
    
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    
    public Long getPreviousNodeId() { return previousNodeId; }
    public void setPreviousNodeId(Long previousNodeId) { this.previousNodeId = previousNodeId; }
    
    public Long getNextNodeId() { return nextNodeId; }
    public void setNextNodeId(Long nextNodeId) { this.nextNodeId = nextNodeId; }
    
    public String getNextNodeIds() { return nextNodeIds; }
    public void setNextNodeIds(String nextNodeIds) { this.nextNodeIds = nextNodeIds; }
    
    public String getSkipCondition() { return skipCondition; }
    public void setSkipCondition(String skipCondition) { this.skipCondition = skipCondition; }
    
    public Integer getRequired() { return required; }
    public void setRequired(Integer required) { this.required = required; }

    public Integer getIsFreeFlow() { return isFreeFlow; }
    public void setIsFreeFlow(Integer isFreeFlow) { this.isFreeFlow = isFreeFlow; }
    
    public Integer getAllowFreeFlow() { return allowFreeFlow; }
    public void setAllowFreeFlow(Integer allowFreeFlow) { this.allowFreeFlow = allowFreeFlow; }
    
    public Integer getIsLastNode() { return isLastNode; }
    public void setIsLastNode(Integer isLastNode) { this.isLastNode = isLastNode; }
    
    public String getFlowActionIds() { return flowActionIds; }
    public void setFlowActionIds(String flowActionIds) { this.flowActionIds = flowActionIds; }
    
    public Integer getGatewayType() { return gatewayType; }
    public void setGatewayType(Integer gatewayType) { this.gatewayType = gatewayType; }
    
    public Integer getGatewayMode() { return gatewayMode; }
    public void setGatewayMode(Integer gatewayMode) { this.gatewayMode = gatewayMode; }
    
    public Long getGatewayId() { return gatewayId; }
    public void setGatewayId(Long gatewayId) { this.gatewayId = gatewayId; }
    
    public String getConditionExpression() { return conditionExpression; }
    public void setConditionExpression(String conditionExpression) { this.conditionExpression = conditionExpression; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}


