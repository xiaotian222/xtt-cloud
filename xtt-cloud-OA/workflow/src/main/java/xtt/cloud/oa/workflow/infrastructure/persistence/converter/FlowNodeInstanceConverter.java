package xtt.cloud.oa.workflow.infrastructure.persistence.converter;

import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.NodeStatus;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;

/**
 * 节点实例转换器
 * 
 * 负责领域实体（Domain Entity）和持久化对象（PO）之间的转换
 * 
 * @author xtt
 */
public class FlowNodeInstanceConverter {
    
    /**
     * 领域实体转PO（用于保存）
     */
    public xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNodeInstance toPO(FlowNodeInstance entity) {
        if (entity == null) {
            return null;
        }
        
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNodeInstance po = 
                new xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNodeInstance();
        po.setId(entity.getId());
        po.setFlowInstanceId(entity.getFlowInstanceId());
        po.setNodeId(entity.getNodeId());
        
        // 转换审批人信息
        if (entity.getApprover() != null) {
            po.setApproverId(entity.getApprover().getUserId());
            po.setApproverDeptId(entity.getApprover().getDeptId());
            po.setApproverName(entity.getApprover().getUserName());
        }
        
        // 转换状态
        if (entity.getStatus() != null) {
            po.setStatus(entity.getStatus().getValue());
        }
        
        po.setComments(entity.getComments());
        po.setHandledAt(entity.getHandledAt());
        po.setCreatedAt(entity.getCreatedAt());
        po.setUpdatedAt(entity.getUpdatedAt());
        
        return po;
    }
    
    /**
     * PO转领域实体（用于查询）
     */
    public FlowNodeInstance toEntity(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNodeInstance po) {
        if (po == null) {
            return null;
        }
        
        FlowNodeInstance entity = new FlowNodeInstance();
        entity.setId(po.getId());
        entity.setFlowInstanceId(po.getFlowInstanceId());
        entity.setNodeId(po.getNodeId());
        
        // 转换审批人信息
        if (po.getApproverId() != null) {
            Approver approver = new Approver(
                    po.getApproverId(),
                    po.getApproverDeptId(),
                    po.getApproverName(),
                    null  // deptName 需要从其他地方获取
            );
            entity.setApprover(approver);
        }
        
        // 转换状态
        if (po.getStatus() != null) {
            entity.setStatus(NodeStatus.fromValue(po.getStatus()));
        }
        
        entity.setComments(po.getComments());
        entity.setHandledAt(po.getHandledAt());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        
        return entity;
    }
}

