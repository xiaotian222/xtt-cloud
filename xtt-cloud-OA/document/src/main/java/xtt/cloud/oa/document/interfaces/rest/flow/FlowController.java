package xtt.cloud.oa.document.interfaces.rest.flow;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import xtt.cloud.oa.common.Result;
import xtt.cloud.oa.document.application.flow.FlowService;
import xtt.cloud.oa.document.application.flow.task.TaskService;
import xtt.cloud.oa.document.domain.entity.flow.FlowInstance;
import xtt.cloud.oa.document.domain.entity.flow.FlowNodeInstance;
import xtt.cloud.oa.document.domain.entity.flow.Handling;
import xtt.cloud.oa.document.domain.entity.flow.task.DoneTask;
import xtt.cloud.oa.document.domain.entity.flow.task.TodoTask;
import xtt.cloud.oa.document.domain.entity.flow.history.ActivityHistory;
import xtt.cloud.oa.document.domain.entity.flow.history.FlowInstanceHistory;
import xtt.cloud.oa.document.domain.entity.flow.history.TaskHistory;

import java.util.List;

/**
 * 流程管理控制器
 * 统一通过 FlowService 操作流程引擎
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@RestController
@RequestMapping("/api/document/flows")
public class FlowController {
    
    private static final Logger log = LoggerFactory.getLogger(FlowController.class);
    
    private final FlowService flowService;
    private final TaskService taskService;

    public FlowController(FlowService flowService, TaskService taskService) {
        this.flowService = flowService;
        this.taskService = taskService;
    }

    /**
     * 创建流程实例
     */
    @PostMapping
    public Result<FlowInstance> createFlow(@RequestBody FlowInstance flowInstance) {
        log.info("收到创建流程实例请求，公文ID: {}, 流程类型: {}", 
                flowInstance.getDocumentId(), flowInstance.getFlowType());
        try {
            FlowInstance created = flowService.createFlowInstance(flowInstance);
            log.info("创建流程实例成功，ID: {}, 公文ID: {}", created.getId(), created.getDocumentId());
            return Result.success(created);
        } catch (Exception e) {
            log.error("创建流程实例失败，公文ID: {}", flowInstance.getDocumentId(), e);
            return Result.error("创建流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 创建承办记录
     */
    @PostMapping("/handlings")
    public Result<Handling> createHandling(@RequestBody Handling handling) {
        try {
            Handling created = flowService.createHandling(handling);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("创建承办记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新承办记录
     */
    @PutMapping("/handlings/{id}")
    public Result<Handling> updateHandling(
            @PathVariable Long id,
            @RequestParam String result,
            @RequestParam Integer status) {
        log.info("收到更新承办记录请求，ID: {}, 状态: {}", id, status);
        try {
            Handling updated = flowService.updateHandling(id, result, status);
            log.info("更新承办记录成功，ID: {}", id);
            return Result.success(updated);
        } catch (Exception e) {
            log.error("更新承办记录失败，ID: {}", id, e);
            return Result.error("更新承办记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取流程实例详情
     */
    @GetMapping("/{id}")
    public Result<FlowInstance> getFlowInstance(@PathVariable Long id) {
        log.debug("收到查询流程实例详情请求，ID: {}", id);
        try {
            FlowInstance flowInstance = flowService.getFlowInstance(id);
            if (flowInstance == null) {
                log.warn("流程实例不存在，ID: {}", id);
                return Result.error("流程实例不存在");
            }
            return Result.success(flowInstance);
        } catch (Exception e) {
            log.error("获取流程实例失败，ID: {}", id, e);
            return Result.error("获取流程实例失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据公文ID获取流程实例
     */
    @GetMapping("/document/{documentId}")
    public Result<FlowInstance> getFlowInstanceByDocumentId(@PathVariable Long documentId) {
        log.debug("收到根据公文ID查询流程实例请求，公文ID: {}", documentId);
        try {
            FlowInstance flowInstance = flowService.getFlowInstanceByDocumentId(documentId);
            if (flowInstance == null) {
                log.warn("未找到公文对应的流程实例，公文ID: {}", documentId);
                return Result.error("流程实例不存在");
            }
            return Result.success(flowInstance);
        } catch (Exception e) {
            log.error("根据公文ID获取流程实例失败，公文ID: {}", documentId, e);
            return Result.error("获取流程实例失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取承办记录列表
     */
    @GetMapping("/{id}/handlings")
    public Result<List<Handling>> getHandlings(@PathVariable Long id) {
        try {
            List<Handling> handlings = flowService.getHandlingsByFlowId(id);
            return Result.success(handlings);
        } catch (Exception e) {
            return Result.error("获取承办记录失败: " + e.getMessage());
        }
    }
    
    // ========== 流程引擎核心接口 ==========
    
    /**
     * 启动流程
     */
    @PostMapping("/start")
    public Result<FlowInstance> startFlow(
            @RequestParam Long documentId,
            @RequestParam Long flowDefId) {
        log.info("收到启动流程请求，文档ID: {}, 流程定义ID: {}", documentId, flowDefId);
        try {
            FlowInstance flowInstance = flowService.startFlow(documentId, flowDefId);
            log.info("启动流程成功，流程实例ID: {}", flowInstance.getId());
            return Result.success(flowInstance);
        } catch (Exception e) {
            log.error("启动流程失败，文档ID: {}, 流程定义ID: {}", documentId, flowDefId, e);
            return Result.error("启动流程失败: " + e.getMessage());
        }
    }
    
    /**
     * 审批同意
     */
    @PostMapping("/approve")
    public Result<String> approve(
            @RequestParam Long nodeInstanceId,
            @RequestParam(required = false) String comments,
            @RequestParam Long approverId) {
        log.info("收到审批同意请求，节点实例ID: {}, 审批人ID: {}", nodeInstanceId, approverId);
        try {
            flowService.approve(nodeInstanceId, comments, approverId);
            return Result.success("审批成功");
        } catch (Exception e) {
            log.error("审批失败，节点实例ID: {}", nodeInstanceId, e);
            return Result.error("审批失败: " + e.getMessage());
        }
    }
    
    /**
     * 审批拒绝
     */
    @PostMapping("/reject")
    public Result<String> reject(
            @RequestParam Long nodeInstanceId,
            @RequestParam(required = false) String comments,
            @RequestParam Long approverId) {
        log.info("收到审批拒绝请求，节点实例ID: {}, 审批人ID: {}", nodeInstanceId, approverId);
        try {
            flowService.reject(nodeInstanceId, comments, approverId);
            return Result.success("已拒绝");
        } catch (Exception e) {
            log.error("拒绝失败，节点实例ID: {}", nodeInstanceId, e);
            return Result.error("拒绝失败: " + e.getMessage());
        }
    }
    
    /**
     * 审批转发
     */
    @PostMapping("/forward")
    public Result<String> forward(
            @RequestParam Long nodeInstanceId,
            @RequestParam(required = false) String comments,
            @RequestParam Long approverId) {
        log.info("收到审批转发请求，节点实例ID: {}, 审批人ID: {}", nodeInstanceId, approverId);
        try {
            flowService.forward(nodeInstanceId, comments, approverId);
            return Result.success("转发成功");
        } catch (Exception e) {
            log.error("转发失败，节点实例ID: {}", nodeInstanceId, e);
            return Result.error("转发失败: " + e.getMessage());
        }
    }
    
    /**
     * 审批退回
     */
    @PostMapping("/return")
    public Result<String> returnBack(
            @RequestParam Long nodeInstanceId,
            @RequestParam(required = false) String comments,
            @RequestParam Long approverId) {
        log.info("收到审批退回请求，节点实例ID: {}, 审批人ID: {}", nodeInstanceId, approverId);
        try {
            flowService.returnBack(nodeInstanceId, comments, approverId);
            return Result.success("退回成功");
        } catch (Exception e) {
            log.error("退回失败，节点实例ID: {}", nodeInstanceId, e);
            return Result.error("退回失败: " + e.getMessage());
        }
    }
    
    // ========== 待办已办管理 ==========
    
    // ========== 待办已办管理 ==========
    
    /**
     * 获取待办任务列表
     */
    @GetMapping("/todos")
    public Result<IPage<TodoTask>> getTodoTasks(
            @RequestParam Long assigneeId,
            @RequestParam(required = false) Integer taskType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.debug("收到查询待办任务请求，审批人ID: {}, 任务类型: {}, 页码: {}, 大小: {}", 
                assigneeId, taskType, pageNum, pageSize);
        try {
            IPage<TodoTask> todos;
            if (taskType != null) {
                todos = taskService.getTodoTasksByType(taskType, assigneeId, pageNum, pageSize);
            } else {
                todos = taskService.getTodoTasksByAssignee(assigneeId, pageNum, pageSize);
            }
            return Result.success(todos);
        } catch (Exception e) {
            log.error("查询待办任务失败，审批人ID: {}", assigneeId, e);
            return Result.error("查询待办任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取已办任务列表
     */
    @GetMapping("/dones")
    public Result<IPage<DoneTask>> getDoneTasks(
            @RequestParam Long handlerId,
            @RequestParam(required = false) Integer taskType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.debug("收到查询已办任务请求，处理人ID: {}, 任务类型: {}, 页码: {}, 大小: {}", 
                handlerId, taskType, pageNum, pageSize);
        try {
            IPage<DoneTask> dones;
            if (taskType != null) {
                dones = taskService.getDoneTasksByType(taskType, handlerId, pageNum, pageSize);
            } else {
                dones = taskService.getDoneTasksByHandler(handlerId, pageNum, pageSize);
            }
            return Result.success(dones);
        } catch (Exception e) {
            log.error("查询已办任务失败，处理人ID: {}", handlerId, e);
            return Result.error("查询已办任务失败: " + e.getMessage());
        }
    }
    
    // ========== 固定流和自由流整合接口 ==========
    
    /**
     * 在固定流程节点中执行自由流转
     * 当节点 allowFreeFlow=1 时，可以使用此接口执行自由流转
     */
    @PostMapping("/fixed-node/free-flow")
    public Result<FlowNodeInstance> executeFreeFlowInFixedNode(
            @RequestParam Long nodeInstanceId,
            @RequestParam Long actionId,
            @RequestParam(required = false) List<Long> selectedDeptIds,
            @RequestParam(required = false) List<Long> selectedUserIds,
            @RequestParam(required = false) String comment,
            @RequestParam Long operatorId) {
        log.info("收到在固定流程节点中执行自由流转请求，节点实例ID: {}, 动作ID: {}, 操作人ID: {}", 
                nodeInstanceId, actionId, operatorId);
        try {
            FlowNodeInstance newNodeInstance = flowService.executeFreeFlowInFixedNode(
                nodeInstanceId, actionId, selectedDeptIds, selectedUserIds, comment, operatorId);
            log.info("在固定流程节点中执行自由流转成功，新节点实例ID: {}", newNodeInstance.getId());
            return Result.success(newNodeInstance);
        } catch (Exception e) {
            log.error("在固定流程节点中执行自由流转失败，节点实例ID: {}, 动作ID: {}", 
                    nodeInstanceId, actionId, e);
            return Result.error("执行自由流转失败: " + e.getMessage());
        }
    }
    
    /**
     * 在自由流中启动固定子流程
     * 当需要在自由流中执行一个固定的审批流程时，可以使用此接口
     */
    @PostMapping("/free-flow/fixed-sub-flow")
    public Result<FlowInstance> startFixedSubFlowInFreeFlow(
            @RequestParam Long parentFlowInstanceId,
            @RequestParam Long documentId,
            @RequestParam Long flowDefId) {
        log.info("收到在自由流中启动固定子流程请求，父流程实例ID: {}, 公文ID: {}, 流程定义ID: {}", 
                parentFlowInstanceId, documentId, flowDefId);
        try {
            FlowInstance subFlowInstance = flowService.startFixedSubFlowInFreeFlow(
                parentFlowInstanceId, documentId, flowDefId);
            log.info("在自由流中启动固定子流程成功，子流程实例ID: {}", subFlowInstance.getId());
            return Result.success(subFlowInstance);
        } catch (Exception e) {
            log.error("在自由流中启动固定子流程失败，父流程实例ID: {}, 流程定义ID: {}", 
                    parentFlowInstanceId, flowDefId, e);
            return Result.error("启动固定子流程失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查子流程是否完成并继续父流程
     * 当固定子流程完成时，需要调用此接口通知父流程继续
     */
    @PostMapping("/sub-flow/continue-parent")
    public Result<String> checkAndContinueParentFlow(@RequestParam Long subFlowInstanceId) {
        log.info("收到检查子流程并继续父流程请求，子流程实例ID: {}", subFlowInstanceId);
        try {
            flowService.checkAndContinueParentFlow(subFlowInstanceId);
            log.info("检查子流程并继续父流程成功，子流程实例ID: {}", subFlowInstanceId);
            return Result.success("操作成功");
        } catch (Exception e) {
            log.error("检查子流程并继续父流程失败，子流程实例ID: {}", subFlowInstanceId, e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }
    
    // ========== 历史记录查询 ==========
    
    /**
     * 获取流程实例历史
     */
    @GetMapping("/{flowInstanceId}/history/instance")
    public Result<FlowInstanceHistory> getFlowInstanceHistory(@PathVariable Long flowInstanceId) {
        log.debug("收到查询流程实例历史请求，流程实例ID: {}", flowInstanceId);
        try {
            FlowInstanceHistory history = flowService.getFlowInstanceHistory(flowInstanceId);
            if (history == null) {
                return Result.error("流程实例历史不存在");
            }
            return Result.success(history);
        } catch (Exception e) {
            log.error("查询流程实例历史失败，流程实例ID: {}", flowInstanceId, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取任务历史列表
     */
    @GetMapping("/{flowInstanceId}/history/tasks")
    public Result<List<TaskHistory>> getTaskHistoryList(@PathVariable Long flowInstanceId) {
        log.debug("收到查询任务历史列表请求，流程实例ID: {}", flowInstanceId);
        try {
            List<TaskHistory> historyList = flowService.getTaskHistoryList(flowInstanceId);
            return Result.success(historyList);
        } catch (Exception e) {
            log.error("查询任务历史列表失败，流程实例ID: {}", flowInstanceId, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取活动历史列表
     */
    @GetMapping("/{flowInstanceId}/history/activities")
    public Result<List<ActivityHistory>> getActivityHistoryList(@PathVariable Long flowInstanceId) {
        log.debug("收到查询活动历史列表请求，流程实例ID: {}", flowInstanceId);
        try {
            List<ActivityHistory> historyList = flowService.getActivityHistoryList(flowInstanceId);
            return Result.success(historyList);
        } catch (Exception e) {
            log.error("查询活动历史列表失败，流程实例ID: {}", flowInstanceId, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据处理人查询任务历史
     */
    @GetMapping("/history/tasks/by-handler")
    public Result<List<TaskHistory>> getTaskHistoryByHandler(@RequestParam Long handlerId) {
        log.debug("收到根据处理人查询任务历史请求，处理人ID: {}", handlerId);
        try {
            List<TaskHistory> historyList = flowService.getTaskHistoryByHandler(handlerId);
            return Result.success(historyList);
        } catch (Exception e) {
            log.error("根据处理人查询任务历史失败，处理人ID: {}", handlerId, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}