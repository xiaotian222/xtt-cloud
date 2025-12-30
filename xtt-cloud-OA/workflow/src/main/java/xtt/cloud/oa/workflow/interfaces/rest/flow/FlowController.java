package xtt.cloud.oa.workflow.interfaces.rest.flow;

import xtt.cloud.oa.workflow.application.flow.FlowApplicationService;
import xtt.cloud.oa.workflow.application.flow.command.*;
import xtt.cloud.oa.workflow.application.flow.dto.FlowInstanceDTO;
import xtt.cloud.oa.workflow.application.flow.query.FlowInstanceQuery;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowMode;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowType;
import xtt.cloud.oa.workflow.interfaces.rest.flow.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程 REST 控制器
 * 
 * @author xtt
 */
@RestController
@RequestMapping("/api/workflow/flows")
public class FlowController {
    
    private final FlowApplicationService flowApplicationService;
    
    public FlowController(FlowApplicationService flowApplicationService) {
        this.flowApplicationService = flowApplicationService;
    }

    /**
     * todo
     * 流程录入部分未实现，包含流程绘制和保存。
     *
     *
     */

    /**
     * 启动流程
     */
    @PostMapping("/start")
    public ResponseEntity<FlowInstanceDTO> startFlow(@RequestBody StartFlowRequest request) {
        StartFlowCommand command = new StartFlowCommand();
        command.setDocumentId(request.getDocumentId());
        command.setFlowDefId(request.getFlowDefId());
        command.setFlowType(FlowType.fromValue(request.getFlowType()));
        command.setFlowMode(FlowMode.fromValue(request.getFlowMode()));
        command.setInitiatorId(request.getInitiatorId());
        command.setProcessVariables(request.getProcessVariables());
        
        FlowInstanceDTO dto = flowApplicationService.startFlow(command);
        return ResponseEntity.ok(dto);
    }
    
    /**
     * 审批通过
     */
    @PostMapping("/approve")
    public ResponseEntity<Void> approve(@RequestBody ApproveRequest request) {
        ApproveCommand command = new ApproveCommand();
        command.setFlowInstanceId(request.getFlowInstanceId());
        command.setNodeInstanceId(request.getNodeInstanceId());
        command.setApproverId(request.getApproverId());
        command.setComments(request.getComments());
        command.setForward(request.getForward());
        
        flowApplicationService.approve(command);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 审批拒绝
     */
    @PostMapping("/reject")
    public ResponseEntity<Void> reject(@RequestBody RejectRequest request) {
        RejectCommand command = new RejectCommand();
        command.setFlowInstanceId(request.getFlowInstanceId());
        command.setNodeInstanceId(request.getNodeInstanceId());
        command.setApproverId(request.getApproverId());
        command.setComments(request.getComments());
        command.setRollbackToNodeId(request.getRollbackToNodeId());
        
        flowApplicationService.reject(command);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 撤回流程
     */
    @PostMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@RequestBody WithdrawRequest request) {
        WithdrawCommand command = new WithdrawCommand();
        command.setFlowInstanceId(request.getFlowInstanceId());
        command.setInitiatorId(request.getInitiatorId());
        command.setReason(request.getReason());
        
        flowApplicationService.withdraw(command);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 回退流程
     */
    @PostMapping("/rollback")
    public ResponseEntity<Void> rollback(@RequestBody RollbackRequest request) {
        RollbackCommand command = new RollbackCommand();
        command.setFlowInstanceId(request.getFlowInstanceId());
        command.setCurrentNodeInstanceId(request.getCurrentNodeInstanceId());
        command.setTargetNodeId(request.getTargetNodeId());
        command.setApproverId(request.getApproverId());
        command.setReason(request.getReason());
        
        flowApplicationService.rollback(command);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 暂停流程
     */
    @PostMapping("/{flowInstanceId}/suspend")
    public ResponseEntity<Void> suspend(@PathVariable Long flowInstanceId) {
        flowApplicationService.suspend(flowInstanceId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 恢复流程
     */
    @PostMapping("/{flowInstanceId}/resume")
    public ResponseEntity<Void> resume(@PathVariable Long flowInstanceId) {
        flowApplicationService.resume(flowInstanceId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 查询流程实例
     */
    @GetMapping("/{flowInstanceId}")
    public ResponseEntity<FlowInstanceDTO> getFlowInstance(@PathVariable Long flowInstanceId) {
        FlowInstanceDTO dto = flowApplicationService.getFlowInstance(flowInstanceId);
        return ResponseEntity.ok(dto);
    }
    
    /**
     * 查询流程实例列表
     */
    @GetMapping
    public ResponseEntity<List<FlowInstanceDTO>> queryFlowInstances(FlowInstanceQuery query) {
        List<FlowInstanceDTO> dtos = flowApplicationService.queryFlowInstances(query);
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * 设置流程变量
     */
    @PutMapping("/{flowInstanceId}/variables/{key}")
    public ResponseEntity<Void> setProcessVariable(
            @PathVariable Long flowInstanceId,
            @PathVariable String key,
            @RequestBody Object value) {
        flowApplicationService.setProcessVariable(flowInstanceId, key, value);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取流程变量
     */
    @GetMapping("/{flowInstanceId}/variables/{key}")
    public ResponseEntity<Object> getProcessVariable(
            @PathVariable Long flowInstanceId,
            @PathVariable String key) {
        Object value = flowApplicationService.getProcessVariable(flowInstanceId, key);
        return ResponseEntity.ok(value);
    }
    
    /**
     * 开启自由流转
     */
    @PostMapping("/free-flow/start")
    public ResponseEntity<Void> startFreeFlow(@RequestBody StartFreeFlowRequest request) {
        StartFreeFlowCommand command = new StartFreeFlowCommand();
        command.setFlowInstanceId(request.getFlowInstanceId());
        command.setNodeInstanceId(request.getNodeInstanceId());
        command.setOperatorId(request.getOperatorId());
        command.setComments(request.getComments());
        
        flowApplicationService.startFreeFlow(command);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 结束自由流转
     */
    @PostMapping("/free-flow/end")
    public ResponseEntity<Void> endFreeFlow(@RequestBody EndFreeFlowRequest request) {
        EndFreeFlowCommand command = new EndFreeFlowCommand();
        command.setFlowInstanceId(request.getFlowInstanceId());
        command.setNodeInstanceId(request.getNodeInstanceId());
        command.setOperatorId(request.getOperatorId());
        command.setComments(request.getComments());
        
        flowApplicationService.endFreeFlow(command);
        return ResponseEntity.ok().build();
    }
}

