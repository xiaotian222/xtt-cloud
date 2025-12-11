package xtt.cloud.oa.document.domain.entity.flow;

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
    private String skipCondition;  // 跳过条件
    private Integer required;       // 是否必须(0:可跳过,1:必须)
    private Integer parallelMode;   // 并行模式(0:串行,1:并行-会签,2:并行-或签)
    private Integer isFreeFlow;     // 是否为自由流节点(0:否,1:是)
    private Integer allowFreeFlow; // 是否允许在此节点使用自由流(0:不允许,1:允许)
    private Integer isLastNode;     // 是否为最后一个节点(0:否,1:是)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 节点类型常量
    public static final int NODE_TYPE_APPROVAL = 1;  // 审批节点
    public static final int NODE_TYPE_NOTIFY = 2;   // 抄送节点
    public static final int NODE_TYPE_CONDITION = 3; // 条件节点
    public static final int NODE_TYPE_AUTO = 4;      // 自动节点
    public static final int NODE_TYPE_FREE_FLOW = 5; // 自由流节点
    
    // 审批人类型常量
    public static final int APPROVER_TYPE_USER = 1;        // 指定人员
    public static final int APPROVER_TYPE_ROLE = 2;        // 指定角色
    public static final int APPROVER_TYPE_DEPT_LEADER = 3; // 指定部门负责人
    public static final int APPROVER_TYPE_INITIATOR = 4;    // 发起人指定
    
    // 并行模式常量
    public static final int PARALLEL_MODE_SERIAL = 0;      // 串行
    public static final int PARALLEL_MODE_PARALLEL_ALL = 1; // 并行-会签（所有节点都完成）
    public static final int PARALLEL_MODE_PARALLEL_ANY = 2; // 并行-或签（任一节点完成）
    
    /**
     * 判断是否为并行模式（会签或或签）
     */
    public boolean isParallelMode() {
        return parallelMode != null && 
               (parallelMode == PARALLEL_MODE_PARALLEL_ALL || parallelMode == PARALLEL_MODE_PARALLEL_ANY);
    }
    
    /**
     * 判断是否为会签模式
     */
    public boolean isParallelAllMode() {
        return parallelMode != null && parallelMode == PARALLEL_MODE_PARALLEL_ALL;
    }
    
    /**
     * 判断是否为或签模式
     */
    public boolean isParallelAnyMode() {
        return parallelMode != null && parallelMode == PARALLEL_MODE_PARALLEL_ANY;
    }
    
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
    
    public String getSkipCondition() { return skipCondition; }
    public void setSkipCondition(String skipCondition) { this.skipCondition = skipCondition; }
    
    public Integer getRequired() { return required; }
    public void setRequired(Integer required) { this.required = required; }
    
    public Integer getParallelMode() { return parallelMode; }
    public void setParallelMode(Integer parallelMode) { this.parallelMode = parallelMode; }
    
    public Integer getIsFreeFlow() { return isFreeFlow; }
    public void setIsFreeFlow(Integer isFreeFlow) { this.isFreeFlow = isFreeFlow; }
    
    public Integer getAllowFreeFlow() { return allowFreeFlow; }
    public void setAllowFreeFlow(Integer allowFreeFlow) { this.allowFreeFlow = allowFreeFlow; }
    
    public Integer getIsLastNode() { return isLastNode; }
    public void setIsLastNode(Integer isLastNode) { this.isLastNode = isLastNode; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}


