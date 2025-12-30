package xtt.cloud.oa.workflow.application.flow.assembler;

import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowAction;
import xtt.cloud.oa.workflow.interfaces.rest.flow.dto.FreeFlowActionDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 自由流组装器
 * 
 * @author xtt
 */
public class FreeFlowAssembler {
    
    /**
     * 将 FlowAction 转换为 DTO
     */
    public static FreeFlowActionDTO toDTO(FlowAction action) {
        if (action == null) {
            return null;
        }
        
        FreeFlowActionDTO dto = new FreeFlowActionDTO();
        dto.setId(action.getId());
        dto.setActionCode(action.getActionCode());
        dto.setActionName(action.getActionName());
        dto.setActionType(action.getActionType());
        dto.setDescription(action.getDescription());
        dto.setIcon(action.getIcon());
        
        return dto;
    }
    
    /**
     * 将 FlowAction 列表转换为 DTO 列表
     */
    public static List<FreeFlowActionDTO> toDTOList(List<FlowAction> actions) {
        if (actions == null) {
            return List.of();
        }
        
        return actions.stream()
                .map(FreeFlowAssembler::toDTO)
                .collect(Collectors.toList());
    }
}

