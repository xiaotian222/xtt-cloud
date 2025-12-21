package xtt.cloud.oa.workflow.application.flow.assembler;

import xtt.cloud.oa.workflow.application.flow.dto.FlowNodeInstanceDTO;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.NodeStatus;

/**
 * 节点实例组装器
 * 
 * @author xtt
 */
public class FlowNodeInstanceAssembler {
    
    /**
     * 实体转DTO
     */
    public static FlowNodeInstanceDTO toDTO(FlowNodeInstance nodeInstance) {
        if (nodeInstance == null) {
            return null;
        }
        
        FlowNodeInstanceDTO dto = new FlowNodeInstanceDTO();
        dto.setId(nodeInstance.getId());
        dto.setFlowInstanceId(nodeInstance.getFlowInstanceId());
        dto.setNodeId(nodeInstance.getNodeId());
        
        // Approver
        if (nodeInstance.getApprover() != null) {
            dto.setApproverId(nodeInstance.getApprover().getUserId());
            dto.setApproverName(nodeInstance.getApprover().getUserName());
            dto.setApproverDeptId(nodeInstance.getApprover().getDeptId());
            dto.setApproverDeptName(nodeInstance.getApprover().getDeptName());
        }
        
        // NodeStatus
        if (nodeInstance.getStatus() != null) {
            dto.setStatus(nodeInstance.getStatus().getValue());
            dto.setStatusDesc(nodeInstance.getStatus().getDescription());
        }
        
        dto.setComments(nodeInstance.getComments());
        dto.setHandledAt(nodeInstance.getHandledAt());
        dto.setCreatedAt(nodeInstance.getCreatedAt());
        dto.setUpdatedAt(nodeInstance.getUpdatedAt());
        
        return dto;
    }
    
    /**
     * DTO转实体
     */
    public static FlowNodeInstance toEntity(FlowNodeInstanceDTO dto) {
        if (dto == null) {
            return null;
        }
        
        FlowNodeInstance nodeInstance = new FlowNodeInstance();
        nodeInstance.setId(dto.getId());
        nodeInstance.setFlowInstanceId(dto.getFlowInstanceId());
        nodeInstance.setNodeId(dto.getNodeId());
        
        // Approver
        if (dto.getApproverId() != null) {
            Approver approver = new Approver(
                    dto.getApproverId(),
                    dto.getApproverDeptId(),
                    dto.getApproverName(),
                    dto.getApproverDeptName()
            );
            nodeInstance.setApprover(approver);
        }
        
        // NodeStatus
        if (dto.getStatus() != null) {
            nodeInstance.setStatus(NodeStatus.fromValue(dto.getStatus()));
        }
        
        nodeInstance.setComments(dto.getComments());
        nodeInstance.setHandledAt(dto.getHandledAt());
        nodeInstance.setCreatedAt(dto.getCreatedAt());
        nodeInstance.setUpdatedAt(dto.getUpdatedAt());
        
        return nodeInstance;
    }
}

