package xtt.cloud.oa.document.application.flow.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.common.BusinessException;
import xtt.cloud.oa.document.application.flow.core.node.FlowNodeService;
import xtt.cloud.oa.document.application.flow.history.FlowHistoryService;
import xtt.cloud.oa.document.application.flow.task.TaskService;
import xtt.cloud.oa.document.domain.entity.flow.definition.Document;
import xtt.cloud.oa.document.domain.entity.flow.FlowInstance;
import xtt.cloud.oa.document.domain.entity.flow.definition.FlowNode;
import xtt.cloud.oa.document.domain.entity.flow.FlowNodeInstance;
import xtt.cloud.oa.document.domain.entity.flow.history.ActivityHistory;
import xtt.cloud.oa.document.domain.entity.flow.task.DoneTask;
import xtt.cloud.oa.document.application.flow.repository.FlowNodeInstanceRepository;

import java.time.LocalDateTime;

/**
 * 审批处理服务
 * 提供审批相关的业务逻辑封装
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Service
public class FlowApprovalService {
    
    private static final Logger log = LoggerFactory.getLogger(FlowApprovalService.class);
    
    private final FlowEngineService flowEngineService;
    private final FlowNodeInstanceRepository flowNodeInstanceRepository;
    private final FlowNodeService flowNodeService;
    private final TaskService taskService;
    private final FlowHistoryService flowHistoryService;

    public FlowApprovalService(
            FlowEngineService flowEngineService,
            FlowNodeInstanceRepository flowNodeInstanceRepository,
            FlowNodeService flowNodeService,
            TaskService taskService,
            FlowHistoryService flowHistoryService) {
        this.flowEngineService = flowEngineService;
        this.flowNodeInstanceRepository = flowNodeInstanceRepository;
        this.flowNodeService = flowNodeService;
        this.taskService = taskService;
        this.flowHistoryService = flowHistoryService;
    }

    /**
     * 审批同意
     */
    @Transactional
    public void approve(FlowNodeInstance nodeInstance, FlowInstance flowInstance,
                       Document document, FlowNode nodeDef, String comments, Long approverId) {

        log.info("审批同意，节点实例ID: {}, 审批人ID: {}", nodeInstance.getId(), approverId);
        validateApprovalPermission(nodeInstance, approverId);

        // 1. 更新节点实例状态
        nodeInstance.setStatus(FlowNodeInstance.STATUS_COMPLETED);
        nodeInstance.setComments(comments);
        nodeInstance.setHandledAt(LocalDateTime.now());
        nodeInstance.setUpdatedAt(LocalDateTime.now());
        flowNodeInstanceRepository.update(nodeInstance);

        // 2. 创建已办任务
        taskService.createDoneTask(nodeInstance, DoneTask.ACTION_APPROVE, comments, flowInstance, document);

        // 3. 更新待办任务状态
        taskService.markAsHandled(nodeInstance.getId(), nodeInstance.getApproverId());

        // 4. 记录任务历史
        flowHistoryService.recordTaskHistory(nodeInstance, DoneTask.ACTION_APPROVE, comments);

        // 5. 检查节点是否允许自由流
        if (nodeDef.getAllowFreeFlow() != null && nodeDef.getAllowFreeFlow() == 1) {
            // 节点允许自由流，但不自动流转，需要用户主动执行自由流动作
            log.info("节点允许自由流，节点实例ID: {}，等待用户执行自由流动作", nodeInstance.getId());
            // 记录活动历史
            flowHistoryService.recordActivityHistory(
                    flowInstance,
                    nodeInstance,
                    nodeDef,
                    ActivityHistory.ACTIVITY_TYPE_COMPLETE,
                    ActivityHistory.OPERATION_COMPLETE,
                    nodeInstance.getApproverId(),
                    comments
            );
            return;
        }

        // 6. 记录活动历史（节点完成）
        flowHistoryService.recordActivityHistory(
                flowInstance,
                nodeInstance,
                nodeDef,
                ActivityHistory.ACTIVITY_TYPE_COMPLETE,
                ActivityHistory.OPERATION_COMPLETE,
                nodeInstance.getApproverId(),
                comments
        );

        // 7. 判断是否需要等待并行节点
        if (nodeDef.isParallelMode()) {
            // 并行节点：根据模式检查是否满足流转条件
            boolean canMoveNext = false;
            if (nodeDef.isParallelAllMode()) {
                // 会签模式：所有节点都完成
                canMoveNext = flowNodeService.allParallelNodesCompleted(flowInstance, nodeDef);
                log.debug("会签模式检查，节点实例ID: {}, 是否全部完成: {}", nodeInstance.getId(), canMoveNext);
            } else if (nodeDef.isParallelAnyMode()) {
                // 或签模式：任一节点完成即可
                canMoveNext = flowNodeService.anyParallelNodeCompleted(flowInstance, nodeDef);
                log.debug("或签模式检查，节点实例ID: {}, 是否任一完成: {}", nodeInstance.getId(), canMoveNext);
            }

            if (canMoveNext) {
                flowEngineService.moveToNextNode(flowInstance, nodeInstance);
            } else {
                log.debug("并行节点尚未满足流转条件，等待其他节点完成，节点实例ID: {}", nodeInstance.getId());
            }
        } else {
            // 串行节点：直接流转
            flowEngineService.moveToNextNode(flowInstance, nodeInstance);
        }
    }
    
    /**
     * 审批拒绝
     */
    @Transactional
    public void reject(FlowNodeInstance nodeInstance, FlowInstance flowInstance,
                      Document document, FlowNode nodeDef, String comments, Long approverId) {
        log.info("审批拒绝，节点实例ID: {}, 审批人ID: {}", nodeInstance.getId(), approverId);
        validateApprovalPermission(nodeInstance, approverId);

        // 1. 更新节点实例状态
        nodeInstance.setStatus(FlowNodeInstance.STATUS_REJECTED);
        nodeInstance.setComments(comments);
        nodeInstance.setHandledAt(LocalDateTime.now());
        nodeInstance.setUpdatedAt(LocalDateTime.now());
        flowNodeInstanceRepository.update(nodeInstance);

        // 2. 创建已办任务
        taskService.createDoneTask(nodeInstance, DoneTask.ACTION_REJECT, comments, flowInstance, document);

        // 3. 更新待办任务状态
        taskService.markAsHandled(nodeInstance.getId(), nodeInstance.getApproverId());

        // 4. 记录任务历史
        flowHistoryService.recordTaskHistory(nodeInstance, DoneTask.ACTION_REJECT, comments);

        // 5. 记录活动历史（节点拒绝）
        flowHistoryService.recordActivityHistory(
                flowInstance,
                nodeInstance,
                nodeDef,
                ActivityHistory.ACTIVITY_TYPE_REJECT,
                ActivityHistory.OPERATION_REJECT,
                nodeInstance.getApproverId(),
                comments
        );

        // 6. 终止流程或退回
        if (nodeDef.getRequired() != null && nodeDef.getRequired() == 1) {
            // 必须节点被拒绝，流程终止
            flowEngineService.terminateFlow(flowInstance, "节点被拒绝: " + comments);
        } else {
            // 非必须节点被拒绝，可以跳过或退回
            // 根据业务规则处理
            log.warn("非必须节点被拒绝，节点实例ID: {}, 意见: {}", nodeInstance.getId(), comments);
        }

    }
    
    /**
     * 审批转发
     */
    @Transactional
    public void forward(FlowNodeInstance nodeInstance, FlowInstance flowInstance,
                       Document document, FlowNode nodeDef, String comments, Long approverId) {
        log.info("审批转发，节点实例ID: {}, 审批人ID: {}", nodeInstance.getId(), approverId);
        validateApprovalPermission(nodeInstance, approverId);

        // 记录任务历史
        flowHistoryService.recordTaskHistory(nodeInstance, DoneTask.ACTION_FORWARD, comments);

        // 记录活动历史
        flowHistoryService.recordActivityHistory(
                flowInstance,
                nodeInstance,
                nodeDef,
                ActivityHistory.ACTIVITY_TYPE_COMPLETE,
                ActivityHistory.OPERATION_COMPLETE,
                nodeInstance.getApproverId(),
                "转发: " + comments
        );

        // TODO: 实现转发逻辑
        log.warn("转发功能暂未实现，节点实例ID: {}", nodeInstance.getId());

    }
    
    /**
     * 审批退回
     */
    @Transactional
    public void returnBack(FlowNodeInstance nodeInstance, FlowInstance flowInstance,
                          Document document, FlowNode nodeDef, String comments, Long approverId) {
        log.info("审批退回，节点实例ID: {}, 审批人ID: {}", nodeInstance.getId(), approverId);
        validateApprovalPermission(nodeInstance, approverId);

        // 记录任务历史
        flowHistoryService.recordTaskHistory(nodeInstance, DoneTask.ACTION_RETURN, comments);

        // 记录活动历史
        flowHistoryService.recordActivityHistory(
                flowInstance,
                nodeInstance,
                nodeDef,
                ActivityHistory.ACTIVITY_TYPE_COMPLETE,
                ActivityHistory.OPERATION_COMPLETE,
                nodeInstance.getApproverId(),
                "退回: " + comments
        );

        // TODO: 实现退回逻辑
        log.warn("退回功能暂未实现，节点实例ID: {}", nodeInstance.getId());

    }

    /**
     * 验证审批权限
     */
    private void validateApprovalPermission(FlowNodeInstance nodeInstance, Long approverId) {
        if (nodeInstance == null) {
            throw new BusinessException("节点实例不存在");
        }
        
        if (!nodeInstance.getApproverId().equals(approverId)) {
            throw new BusinessException("无权审批此节点");
        }
        
        if (nodeInstance.getStatus() != FlowNodeInstance.STATUS_PENDING) {
            throw new BusinessException("节点状态不正确，当前状态: " + nodeInstance.getStatus());
        }
    }
}

