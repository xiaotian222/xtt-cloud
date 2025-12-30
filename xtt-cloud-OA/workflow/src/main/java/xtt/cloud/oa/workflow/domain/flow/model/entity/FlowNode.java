package xtt.cloud.oa.workflow.domain.flow.model.entity;

import java.time.LocalDateTime;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.GatewayType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.GatewayMode;

/**
 * 流程节点实体（领域实体）
 * 
 * 注意：此实体引用现有的 FlowNode PO
 * 属于 FlowDefinition 聚合
 * 
 * @author xtt
 */
public class FlowNode {

    // 节点类型常量
    public static final int NODE_TYPE_APPROVAL = 1;  // 审批节点（审批人）
    public static final int NODE_TYPE_GATEWAY = 2;   // 网关节点
    public static final int NODE_TYPE_FREE_FLOW = 4; // 自由流节点
    public static final int NODE_TYPE_PRARENT_FLOW = 3;   // 子流程节点
    public static final int NODE_TYPE_OATHER = 5;   // 其他节点
//    public static final int NODE_TYPE_NOTIFY = 2;   // 抄送节点
//    public static final int NODE_TYPE_CONDITION = 3; // 条件节点
//    public static final int NODE_TYPE_AUTO = 4;      // 自动节点

    // 审批人类型常量
    public static final int APPROVER_TYPE_USER = 1;        // 指定人员
    public static final int APPROVER_TYPE_DEPT = 2;         //指定部门
    public static final int APPROVER_TYPE_ROLE = 3;        // 指定角色
    public static final int APPROVER_TYPE_DEPT_ROLE = 4;   //指定 部门&角色的交集
    public static final int APPROVER_TYPE_DEPT_LEADER = 5; // 指定部门领导
    public static final int APPROVER_TYPE_INITIATOR = 6;    // 发起人指定

    // 网关类型常量
    public static final int GATEWAY_TYPE_NONE = 0;           // 非网关
    public static final int GATEWAY_TYPE_PARALLEL_SPLIT = 1; // 并行网关-分支
    public static final int GATEWAY_TYPE_PARALLEL_JOIN = 2;  // 并行网关-汇聚
    public static final int GATEWAY_TYPE_CONDITION_SPLIT = 3; // 条件网关-分支
    public static final int GATEWAY_TYPE_CONDITION_JOIN = 4;  // 条件网关-汇聚

    // 并行网关模式常量
    public static final int GATEWAY_MODE_PARALLEL_ALL = 1;   // 并行会签（所有分支都完成）
    public static final int GATEWAY_MODE_PARALLEL_ANY = 2;   // 并行或签（任一分支完成）

    private final xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode po;
    
    public FlowNode(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode po) {
        this.po = po;
    }


    /**
     * 创建节点（工厂方法）
     * 
     * @param flowDefId 流程定义ID
     * @param nodeName 节点名称
     * @param nodeType 节点类型
     * @param orderNum 顺序号
     * @return 节点实体
     */
    public static FlowNode create(
            Long flowDefId,
            String nodeName,
            Integer nodeType,
            Integer orderNum) {
        
        if (flowDefId == null || flowDefId <= 0) {
            throw new IllegalArgumentException("流程定义ID必须大于0");
        }
        if (nodeName == null || nodeName.trim().isEmpty()) {
            throw new IllegalArgumentException("节点名称不能为空");
        }
        if (nodeType == null) {
            throw new IllegalArgumentException("节点类型不能为空");
        }
        
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode po = 
                new xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode();
        po.setFlowDefId(flowDefId);
        po.setNodeName(nodeName.trim());
        po.setNodeType(nodeType);
        po.setOrderNum(orderNum);
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());
        
        return new FlowNode(po);
    }

    /**
     * 判断是否为网关节点
     */
    public boolean isGateway() {
        return (po.getNodeType() == NODE_TYPE_GATEWAY) && getGatewayType().isGateway();
    }

    /**
     * 获取网关类型
     * 
     * 网关信息冗余存储在节点中，通过此方法获取
     */
    public GatewayType getGatewayType() {
        if (po == null || po.getGatewayType() == null) {
            return GatewayType.NONE;
        }
        return GatewayType.fromValue(po.getGatewayType());
    }

    /**
     * 判断是否为 Split 分支网关
     */
    public boolean isSplitGateway() {
        return getGatewayType().isSplit();
    }
    
    /**
     * 判断是否为 Join 汇聚网关
     */
    public boolean isJoinGateway() {
        return getGatewayType().isJoin();
    }
    
    /**
     * 判断是否为并行网关
     */
    public boolean isParallelGateway() {
        return getGatewayType().isParallel();
    }
    
    /**
     * 判断是否为条件网关
     */
    public boolean isConditionGateway() {
        return getGatewayType().isCondition();
    }
    
    /**
     * 获取并行网关模式（仅用于并行网关）
     */
    public GatewayMode getGatewayMode() {
        if (po == null || po.getGatewayMode() == null) {
            return GatewayMode.PARALLEL_ALL; // 默认会签模式
        }
        return GatewayMode.fromValue(po.getGatewayMode());
    }
    
    /**
     * 获取网关ID（用于关联Split和Join节点）
     * 
     * Split 和 Join 节点通过相同的 gatewayId 关联
     */
    public Long getGatewayId() {
        return po != null ? po.getGatewayId() : null;
    }
    
    /**
     * 获取条件表达式（仅用于条件网关，SpEL格式）
     */
    public String getConditionExpression() {
        return po != null ? po.getConditionExpression() : null;
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
    
    /**
     * 获取持久化对象
     */
    public xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode getPO() {
        return po;
    }
}

