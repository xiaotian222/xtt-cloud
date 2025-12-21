package xtt.cloud.oa.workflow.application.flow.assembler;

import xtt.cloud.oa.workflow.application.flow.dto.FlowInstanceDTO;
import xtt.cloud.oa.workflow.application.flow.dto.FlowNodeInstanceDTO;
import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowInstanceId;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowMode;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowStatus;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.ProcessVariables;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程实例组装器
 * 
 * 负责聚合根和DTO之间的转换
 * 
 * @author xtt
 */
public class FlowInstanceAssembler {
    
    /**
     * 聚合根转DTO
     */
    public static FlowInstanceDTO toDTO(FlowInstance flowInstance) {
        if (flowInstance == null) {
            return null;
        }
        
        FlowInstanceDTO dto = new FlowInstanceDTO();
        dto.setId(flowInstance.getId() != null ? flowInstance.getId().getValue() : null);
        dto.setDocumentId(flowInstance.getDocumentId());
        dto.setFlowDefId(flowInstance.getFlowDefId());
        
        // FlowType
        if (flowInstance.getFlowType() != null) {
            dto.setFlowType(flowInstance.getFlowType().getValue());
            dto.setFlowTypeDesc(flowInstance.getFlowType().getDescription());
        }
        
        // FlowStatus
        if (flowInstance.getStatus() != null) {
            dto.setStatus(flowInstance.getStatus().getValue());
            dto.setStatusDesc(flowInstance.getStatus().getDescription());
        }
        
        // FlowMode
        if (flowInstance.getFlowMode() != null) {
            dto.setFlowMode(flowInstance.getFlowMode().getValue());
            dto.setFlowModeDesc(flowInstance.getFlowMode().getDescription());
        }
        
        dto.setCurrentNodeId(flowInstance.getCurrentNodeId());
        dto.setParentFlowInstanceId(flowInstance.getParentFlowInstanceId());
        dto.setStartTime(flowInstance.getStartTime());
        dto.setEndTime(flowInstance.getEndTime());
        dto.setCreatedAt(flowInstance.getCreatedAt());
        dto.setUpdatedAt(flowInstance.getUpdatedAt());
        
        // ProcessVariables
        if (flowInstance.getProcessVariables() != null) {
            dto.setProcessVariables(flowInstance.getProcessVariables().getAllVariables());
        }
        
        // NodeInstances
        if (flowInstance.getNodeInstances() != null) {
            List<FlowNodeInstanceDTO> nodeInstanceDTOs = flowInstance.getNodeInstances().stream()
                    .map(FlowNodeInstanceAssembler::toDTO)
                    .collect(Collectors.toList());
            dto.setNodeInstances(nodeInstanceDTOs);
        }
        
        return dto;
    }
    
    /**
     * DTO转聚合根（用于查询场景，重建聚合）
     */
    public static FlowInstance toAggregate(FlowInstanceDTO dto) {
        if (dto == null) {
            return null;
        }
        
        FlowInstance flowInstance = FlowInstance.reconstruct();
        
        if (dto.getId() != null) {
            flowInstance.setId(FlowInstanceId.of(dto.getId()));
        }
        flowInstance.setDocumentId(dto.getDocumentId());
        flowInstance.setFlowDefId(dto.getFlowDefId());
        
        if (dto.getFlowType() != null) {
            flowInstance.setFlowType(FlowType.fromValue(dto.getFlowType()));
        }
        
        if (dto.getStatus() != null) {
            flowInstance.setStatus(FlowStatus.fromValue(dto.getStatus()));
        }
        
        if (dto.getFlowMode() != null) {
            flowInstance.setFlowMode(FlowMode.fromValue(dto.getFlowMode()));
        }
        
        flowInstance.setCurrentNodeId(dto.getCurrentNodeId());
        flowInstance.setParentFlowInstanceId(dto.getParentFlowInstanceId());
        flowInstance.setStartTime(dto.getStartTime());
        flowInstance.setEndTime(dto.getEndTime());
        flowInstance.setCreatedAt(dto.getCreatedAt());
        flowInstance.setUpdatedAt(dto.getUpdatedAt());
        
        if (dto.getProcessVariables() != null) {
            flowInstance.setProcessVariables(new ProcessVariables(dto.getProcessVariables()));
        }
        
        if (dto.getNodeInstances() != null) {
            List<FlowNodeInstance> nodeInstances = dto.getNodeInstances().stream()
                    .map(FlowNodeInstanceAssembler::toEntity)
                    .collect(Collectors.toList());
            flowInstance.setNodeInstances(nodeInstances);
        }
        
        return flowInstance;
    }
}

