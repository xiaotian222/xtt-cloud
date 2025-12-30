package xtt.cloud.oa.workflow.interfaces.rest.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xtt.cloud.oa.workflow.application.flow.FreeFlowApplicationService;
import xtt.cloud.oa.workflow.application.flow.command.SendFreeFlowCommand;
import xtt.cloud.oa.workflow.application.flow.assembler.FreeFlowAssembler;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowAction;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;
import xtt.cloud.oa.workflow.interfaces.rest.flow.dto.FreeFlowActionDTO;
import xtt.cloud.oa.workflow.domain.flow.service.FreeFlowService;

import java.util.List;
import java.util.Map;

/**
 * 自由流控制器
 * 
 * @author xtt
 */
@RestController
@RequestMapping("/api/workflow/free-flow")
public class FreeFlowController {
    
    private static final Logger log = LoggerFactory.getLogger(FreeFlowController.class);
    
    private final FreeFlowService freeFlowService;
    private final FreeFlowApplicationService freeFlowApplicationService;
    
    public FreeFlowController(
            FreeFlowService freeFlowService,
            FreeFlowApplicationService freeFlowApplicationService) {
        this.freeFlowService = freeFlowService;
        this.freeFlowApplicationService = freeFlowApplicationService;
    }
    
    /**
     * 获取可用动作
     * 
     * @param documentStatus 文档状态（0:草稿,1:审核中,2:已发布,3:已归档）
     * @param userRoles 用户角色列表（逗号分隔）
     * @param deptId 用户部门ID
     * @return 可用动作列表
     */
    @GetMapping("/actions")
    public ResponseEntity<List<FreeFlowActionDTO>> getAvailableActions(
            @RequestParam("documentStatus") Integer documentStatus,
            @RequestParam("userRoles") String userRoles,
            @RequestParam(value = "deptId", required = false) Long deptId) {
        try {
            List<String> roleList = userRoles != null && !userRoles.isEmpty() 
                    ? List.of(userRoles.split(",")) 
                    : List.of();
            
            List<FlowAction> actions = freeFlowService.getAvailableActions(
                    documentStatus, roleList, deptId);
            List<FreeFlowActionDTO> dtos = FreeFlowAssembler.toDTOList(actions);
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("获取可用动作失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取可选审批人
     * 
     * @param actionId 动作ID
     * @param userRoles 用户角色列表（逗号分隔）
     * @param deptId 用户部门ID
     * @param processVariables 流程变量（可选，JSON格式）
     * @return 可选审批人列表
     */
    @GetMapping("/approvers")
    public ResponseEntity<List<Approver>> getAvailableApprovers(
            @RequestParam("actionId") Long actionId,
            @RequestParam("userRoles") String userRoles,
            @RequestParam(value = "deptId", required = false) Long deptId,
            @RequestParam(value = "processVariables", required = false) Map<String, Object> processVariables) {
        try {
            List<String> roleList = userRoles != null && !userRoles.isEmpty() 
                    ? List.of(userRoles.split(",")) 
                    : List.of();
            
            List<Approver> approvers = freeFlowService.getAvailableApprovers(
                    actionId, roleList, deptId, processVariables);
            return ResponseEntity.ok(approvers);
        } catch (Exception e) {
            log.error("获取可选审批人失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 发送自由流
     * 
     * @param command 发送命令
     * @return 操作结果
     */
    @PostMapping("/send")
    public ResponseEntity<Void> sendFreeFlow(@RequestBody SendFreeFlowCommand command) {
        try {
            freeFlowApplicationService.sendFreeFlow(command);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("发送自由流参数错误", e);
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.error("发送自由流状态错误", e);
            return ResponseEntity.badRequest().build();
        } catch (SecurityException e) {
            log.error("发送自由流权限错误", e);
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error("发送自由流失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

