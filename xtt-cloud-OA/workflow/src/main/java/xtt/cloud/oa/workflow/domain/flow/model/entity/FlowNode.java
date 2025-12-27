package xtt.cloud.oa.workflow.domain.flow.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 流程节点实体（领域实体）
 * 
 * 注意：此实体引用现有的 FlowNode PO
 * 属于 FlowDefinition 聚合
 * 
 * @author xtt
 */
public class FlowNode {
    
    // 审批人类型常量（委托给 PO）
    public static final int APPROVER_TYPE_USER = xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode.APPROVER_TYPE_USER;
    public static final int APPROVER_TYPE_ROLE = xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode.APPROVER_TYPE_ROLE;
    public static final int APPROVER_TYPE_DEPT_LEADER = xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode.APPROVER_TYPE_DEPT_LEADER;
    public static final int APPROVER_TYPE_INITIATOR = xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode.APPROVER_TYPE_INITIATOR;
    
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
     * 判断是否为并行模式（会签或或签）
     */
    public boolean isParallelMode() {
        return po != null && po.isParallelMode();
    }
    
    /**
     * 判断是否为会签模式
     */
    public boolean isParallelAllMode() {
        return po != null && po.isParallelAllMode();
    }
    
    /**
     * 判断是否为或签模式
     */
    public boolean isParallelAnyMode() {
        return po != null && po.isParallelAnyMode();
    }
    
    /**
     * 判断是否为条件节点
     */
    public boolean isConditionNode() {
        return po != null && po.getNodeType() != null 
                && po.getNodeType() == xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode.NODE_TYPE_CONDITION;
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
    
    public Integer getParallelMode() {
        return po != null ? po.getParallelMode() : null;
    }
    
    /**
     * 获取 FlowAction ID 列表
     * 
     * @return FlowAction ID 的字符串集合，如果为空则返回空列表
     */
    public List<String> getFlowActionIds() {
        if (po == null || po.getFlowActionIds() == null || po.getFlowActionIds().trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> actionIds = objectMapper.readValue(
                    po.getFlowActionIds(), 
                    new TypeReference<List<String>>() {}
            );
            return actionIds != null ? actionIds : Collections.emptyList();
        } catch (Exception e) {
            // 如果解析失败，尝试按逗号分隔（兼容旧格式）
            String[] ids = po.getFlowActionIds().split(",");
            List<String> result = new ArrayList<>();
            for (String id : ids) {
                String trimmed = id.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
            return result;
        }
    }
    
    /**
     * 设置 FlowAction ID 列表
     * 
     * @param flowActionIds FlowAction ID 的字符串集合
     */
    public void setFlowActionIds(List<String> flowActionIds) {
        if (po == null) {
            return;
        }
        
        if (flowActionIds == null || flowActionIds.isEmpty()) {
            po.setFlowActionIds(null);
            return;
        }
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(flowActionIds);
            po.setFlowActionIds(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize FlowActionIds to JSON", e);
        }
    }
    
    /**
     * 判断是否包含指定的 FlowAction ID
     * 
     * @param actionId FlowAction ID
     * @return 如果包含则返回 true
     */
    public boolean containsFlowActionId(String actionId) {
        if (actionId == null || actionId.trim().isEmpty()) {
            return false;
        }
        return getFlowActionIds().contains(actionId.trim());
    }
    
    /**
     * 获取持久化对象
     */
    public xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode getPO() {
        return po;
    }
}

