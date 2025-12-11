package xtt.cloud.oa.document.interfaces.rest.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xtt.cloud.oa.common.Result;
import xtt.cloud.oa.document.application.flow.core.FreeFlowEngineService;
import xtt.cloud.oa.document.domain.entity.flow.FlowInstance;
import xtt.cloud.oa.document.domain.entity.flow.ApproverScope;
import xtt.cloud.oa.document.domain.entity.flow.FlowAction;
import xtt.cloud.oa.document.domain.entity.flow.FlowNodeInstance;
import xtt.cloud.oa.document.domain.mapper.flow.FlowInstanceMapper;
import xtt.cloud.oa.document.interfaces.rest.dto.ExecuteActionRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自由流控制器
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@RestController
@RequestMapping("/api/document/flows")
public class FreeFlowController {
    
    private static final Logger log = LoggerFactory.getLogger(FreeFlowController.class);
    
    private final FreeFlowEngineService freeFlowEngineService;
    
    private final FlowInstanceMapper flowInstanceMapper;

    public FreeFlowController(FreeFlowEngineService freeFlowEngineService, FlowInstanceMapper flowInstanceMapper) {
        this.freeFlowEngineService = freeFlowEngineService;
        this.flowInstanceMapper = flowInstanceMapper;
    }

    /**
     * 获取当前用户可用的发送动作（通过流程实例ID）
     * 
     * @param flowInstanceId 流程实例ID
     * @param userId 用户ID（从请求头获取）
     */
    @GetMapping("/{flowInstanceId}/available-actions")
    public Result<List<FlowAction>> getAvailableActions(
            @PathVariable Long flowInstanceId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        log.debug("收到获取可用发送动作请求，流程实例ID: {}, 用户ID: {}", flowInstanceId, userId);
        
        try {
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            // 从流程实例获取文档ID
            FlowInstance flowInstance = flowInstanceMapper.selectById(flowInstanceId);
            if (flowInstance == null) {
                return Result.error("流程实例不存在");
            }
            
            List<FlowAction> actions = freeFlowEngineService.getAvailableActions(flowInstance.getDocumentId(), userId);
            log.debug("找到 {} 个可用发送动作，流程实例ID: {}", actions.size(), flowInstanceId);
            return Result.success(actions);
        } catch (Exception e) {
            log.error("获取可用发送动作失败，流程实例ID: {}", flowInstanceId, e);
            return Result.error("获取可用发送动作失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户可用的发送动作（通过文档ID）
     * 
     * @param documentId 文档ID
     * @param userId 用户ID（从请求头获取）
     */
    @GetMapping("/documents/{documentId}/available-actions")
    public Result<List<FlowAction>> getAvailableActionsByDocument(
            @PathVariable Long documentId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        log.debug("收到获取可用发送动作请求，文档ID: {}, 用户ID: {}", documentId, userId);
        
        try {
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            List<FlowAction> actions = freeFlowEngineService.getAvailableActions(documentId, userId);
            log.debug("找到 {} 个可用发送动作，文档ID: {}", actions.size(), documentId);
            return Result.success(actions);
        } catch (Exception e) {
            log.error("获取可用发送动作失败，文档ID: {}", documentId, e);
            return Result.error("获取可用发送动作失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取发送动作对应的审批人选择范围
     * 
     * @param actionId 动作ID
     */
    @GetMapping("/actions/{actionId}/approver-scope")
    public Result<Map<String, Object>> getApproverScope(@PathVariable Long actionId) {
        log.debug("收到获取审批人选择范围请求，动作ID: {}", actionId);
        
        try {
            ApproverScope scope = freeFlowEngineService.getApproverScope(actionId);
            
            // 构建响应数据
            Map<String, Object> result = new HashMap<>();
            result.put("actionId", scope.getActionId());
            result.put("scopeType", scope.getScopeType());
            result.put("deptIds", scope.getDeptIds());
            result.put("userIds", scope.getUserIds());
            result.put("roleCodes", scope.getRoleCodes());
            result.put("allowCustom", scope.getAllowCustom());
            
            // TODO: 如果需要返回部门和人员列表，需要调用用户服务
            // result.put("deptList", ...);
            // result.put("userList", ...);
            
            log.debug("获取审批人选择范围成功，动作ID: {}", actionId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取审批人选择范围失败，动作ID: {}", actionId, e);
            return Result.error("获取审批人选择范围失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行发送动作
     * 
     * @param nodeInstanceId 当前节点实例ID
     * @param request 执行动作请求
     * @param userId 用户ID（从请求头获取）
     */
    @PostMapping("/node-instances/{nodeInstanceId}/execute-action")
    public Result<Map<String, Object>> executeAction(
            @PathVariable Long nodeInstanceId,
            @RequestBody ExecuteActionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        log.info("收到执行发送动作请求，节点实例ID: {}, 动作ID: {}, 用户ID: {}", 
                nodeInstanceId, request.getActionId(), userId);
        
        try {
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            if (request.getActionId() == null) {
                return Result.error("动作ID不能为空");
            }
            
            FlowNodeInstance nodeInstance = freeFlowEngineService.executeAction(
                nodeInstanceId,
                request.getActionId(),
                request.getSelectedDeptIds(),
                request.getSelectedUserIds(),
                request.getComment(),
                userId
            );
            
            // 构建响应数据
            Map<String, Object> result = new HashMap<>();
            result.put("nodeInstanceId", nodeInstance.getId());
            result.put("flowInstanceId", nodeInstance.getFlowInstanceId());
            result.put("approverId", nodeInstance.getApproverId());
            result.put("status", nodeInstance.getStatus());
            
            log.info("执行发送动作成功，节点实例ID: {}", nodeInstance.getId());
            return Result.success(result);
        } catch (Exception e) {
            log.error("执行发送动作失败，节点实例ID: {}", nodeInstanceId, e);
            return Result.error("执行发送动作失败: " + e.getMessage());
        }
    }
}

