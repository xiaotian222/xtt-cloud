package xtt.cloud.oa.document.application.flow.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.document.domain.entity.flow.*;
import xtt.cloud.oa.document.domain.entity.flow.definition.Document;
import xtt.cloud.oa.document.domain.entity.flow.definition.FlowDefinition;
import xtt.cloud.oa.document.domain.entity.flow.definition.FlowNode;
import xtt.cloud.oa.document.domain.entity.flow.history.ActivityHistory;
import xtt.cloud.oa.document.domain.entity.flow.history.FlowInstanceHistory;
import xtt.cloud.oa.document.domain.entity.flow.history.TaskHistory;
import xtt.cloud.oa.document.application.flow.repository.*;
import xtt.cloud.oa.document.domain.mapper.flow.ActivityHistoryMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FlowInstanceHistoryMapper;
import xtt.cloud.oa.document.domain.mapper.flow.TaskHistoryMapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程历史记录服务
 * 负责记录和查询流程历史信息
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Service
public class FlowHistoryService {
    
    private static final Logger log = LoggerFactory.getLogger(FlowHistoryService.class);
    
    private final FlowInstanceHistoryRepository flowInstanceHistoryRepository;
    private final TaskHistoryRepository taskHistoryRepository;
    private final ActivityHistoryRepository activityHistoryRepository;
    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowDefinitionRepository flowDefinitionRepository;
    private final FlowNodeRepository flowNodeRepository;
    private final FlowNodeInstanceRepository flowNodeInstanceRepository;
    private final DocumentRepository documentRepository;
    
    public FlowHistoryService(
            FlowInstanceHistoryRepository flowInstanceHistoryRepository,
            TaskHistoryRepository taskHistoryRepository,
            ActivityHistoryRepository activityHistoryRepository,
            FlowInstanceRepository flowInstanceRepository,
            FlowDefinitionRepository flowDefinitionRepository,
            FlowNodeRepository flowNodeRepository,
            FlowNodeInstanceRepository flowNodeInstanceRepository,
            DocumentRepository documentRepository) {
        this.flowInstanceHistoryRepository = flowInstanceHistoryRepository;
        this.taskHistoryRepository = taskHistoryRepository;
        this.activityHistoryRepository = activityHistoryRepository;
        this.flowInstanceRepository = flowInstanceRepository;
        this.flowDefinitionRepository = flowDefinitionRepository;
        this.flowNodeRepository = flowNodeRepository;
        this.flowNodeInstanceRepository = flowNodeInstanceRepository;
        this.documentRepository = documentRepository;
    }
    
    /**
     * 记录流程实例历史
     * 当流程启动或结束时调用
     * 
     * @param flowInstance 流程实例
     */
    @Transactional
    public void recordFlowInstanceHistory(FlowInstance flowInstance) {
        if (flowInstance == null) {
            log.warn("流程实例为空，无法记录历史");
            return;
        }
        
        log.debug("记录流程实例历史，流程实例ID: {}", flowInstance.getId());
        
        try {
            // 检查是否已存在历史记录
            FlowInstanceHistory existing = flowInstanceHistoryRepository.findByFlowInstanceId(flowInstance.getId());
            
            FlowInstanceHistory history;
            if (existing != null) {
                history = existing;
            } else {
                history = new FlowInstanceHistory();
                history.setFlowInstanceId(flowInstance.getId());
                history.setCreatedAt(LocalDateTime.now());
            }
            
            // 填充基本信息
            history.setDocumentId(flowInstance.getDocumentId());
            history.setFlowDefId(flowInstance.getFlowDefId());
            history.setFlowType(flowInstance.getFlowType());
            history.setFlowMode(flowInstance.getFlowMode());
            history.setStatus(flowInstance.getStatus());
            history.setStartTime(flowInstance.getStartTime());
            history.setEndTime(flowInstance.getEndTime());
            
            // 计算持续时间
            if (flowInstance.getStartTime() != null) {
                LocalDateTime endTime = flowInstance.getEndTime() != null 
                    ? flowInstance.getEndTime() 
                    : LocalDateTime.now();
                Duration duration = Duration.between(flowInstance.getStartTime(), endTime);
                history.setDuration(duration.getSeconds());
            }
            
            // 获取流程定义名称
            if (flowInstance.getFlowDefId() != null) {
                FlowDefinition flowDef = flowDefinitionRepository.findById(flowInstance.getFlowDefId());
                if (flowDef != null) {
                    history.setFlowDefName(flowDef.getName());
                }
            }
            
            // 获取发起人信息（从文档获取）
            if (flowInstance.getDocumentId() != null) {
                Document document = documentRepository.findById(flowInstance.getDocumentId());
                if (document != null) {
                    history.setInitiatorId(document.getCreatorId());
                    history.setInitiatorDeptId(document.getDeptId());
                    // TODO: 从用户服务获取姓名和部门名称
                }
            }
            
            // 统计节点信息
            List<FlowNodeInstance> nodeInstances = flowNodeInstanceRepository.findByFlowInstanceId(flowInstance.getId());
            history.setTotalNodes(nodeInstances.size());
            long completedCount = nodeInstances.stream()
                .filter(ni -> ni.getStatus() == FlowNodeInstance.STATUS_COMPLETED)
                .count();
            history.setCompletedNodes((int) completedCount);
            
            // 保存或更新
            if (existing != null) {
                flowInstanceHistoryRepository.update(history);
            } else {
                flowInstanceHistoryRepository.save(history);
            }
            
            log.debug("流程实例历史记录成功，流程实例ID: {}", flowInstance.getId());
        } catch (Exception e) {
            log.error("记录流程实例历史失败，流程实例ID: {}", flowInstance.getId(), e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 记录任务历史
     * 当任务处理完成时调用
     * 
     * @param nodeInstance 节点实例
     * @param action 操作类型
     * @param comments 审批意见
     */
    @Transactional
    public void recordTaskHistory(FlowNodeInstance nodeInstance, String action, String comments) {
        if (nodeInstance == null) {
            log.warn("节点实例为空，无法记录历史");
            return;
        }
        
        log.debug("记录任务历史，节点实例ID: {}, 操作: {}", nodeInstance.getId(), action);
        
        try {
            FlowInstance flowInstance = flowInstanceRepository.findById(nodeInstance.getFlowInstanceId());
            if (flowInstance == null) {
                log.warn("流程实例不存在，无法记录历史");
                return;
            }
            
            // 检查是否已存在历史记录
            TaskHistory existing = taskHistoryRepository.findByNodeInstanceId(nodeInstance.getId());
            
            TaskHistory history;
            if (existing != null) {
                history = existing;
            } else {
                history = new TaskHistory();
                history.setNodeInstanceId(nodeInstance.getId());
                history.setStartTime(nodeInstance.getCreatedAt());
            }
            
            // 填充基本信息
            history.setFlowInstanceId(flowInstance.getId());
            history.setDocumentId(flowInstance.getDocumentId());
            history.setHandlerId(nodeInstance.getApproverId());
            history.setHandlerDeptId(nodeInstance.getApproverDeptId());
            history.setTaskType(1); // 默认用户任务
            history.setAction(action);
            history.setComments(comments);
            history.setStatus(nodeInstance.getStatus());
            history.setEndTime(nodeInstance.getHandledAt() != null 
                ? nodeInstance.getHandledAt() 
                : LocalDateTime.now());
            
            // 计算持续时间
            if (history.getStartTime() != null && history.getEndTime() != null) {
                Duration duration = Duration.between(history.getStartTime(), history.getEndTime());
                history.setDuration(duration.getSeconds());
            }
            
            // 获取节点信息
            FlowNode node = flowNodeRepository.findById(nodeInstance.getNodeId());
            if (node != null) {
                history.setTaskName(node.getNodeName());
            }
            
            // 获取文档信息
            if (flowInstance.getDocumentId() != null) {
                Document document = documentRepository.findById(flowInstance.getDocumentId());
                if (document != null) {
                    history.setDocumentTitle(document.getTitle());
                }
            }
            
            // 设置操作名称
            history.setActionName(getActionName(action));
            
            // TODO: 从用户服务获取处理人姓名和部门名称
            
            // 保存或更新
            if (existing != null) {
                taskHistoryRepository.update(history);
            } else {
                history.setCreatedAt(LocalDateTime.now());
                taskHistoryRepository.save(history);
            }
            
            log.debug("任务历史记录成功，节点实例ID: {}", nodeInstance.getId());
        } catch (Exception e) {
            log.error("记录任务历史失败，节点实例ID: {}", nodeInstance.getId(), e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 记录活动历史
     * 记录节点的各种活动（创建、开始、完成、拒绝、跳过）
     * 
     * @param flowInstance 流程实例
     * @param nodeInstance 节点实例（可为空）
     * @param node 节点定义
     * @param activityType 活动类型
     * @param operation 操作
     * @param operatorId 操作人ID
     * @param comments 备注
     */
    @Transactional
    public void recordActivityHistory(
            FlowInstance flowInstance,
            FlowNodeInstance nodeInstance,
            FlowNode node,
            Integer activityType,
            String operation,
            Long operatorId,
            String comments) {
        if (flowInstance == null || node == null) {
            log.warn("流程实例或节点为空，无法记录活动历史");
            return;
        }
        
        log.debug("记录活动历史，流程实例ID: {}, 节点ID: {}, 活动类型: {}", 
                flowInstance.getId(), node.getId(), activityType);
        
        try {
            ActivityHistory history = new ActivityHistory();
            history.setFlowInstanceId(flowInstance.getId());
            history.setNodeInstanceId(nodeInstance != null ? nodeInstance.getId() : null);
            history.setNodeId(node.getId());
            history.setActivityType(activityType);
            history.setOperation(operation);
            history.setOperatorId(operatorId);
            history.setComments(comments);
            history.setActivityTime(LocalDateTime.now());
            history.setCreatedAt(LocalDateTime.now());
            
            // 设置节点信息
            history.setNodeName(node.getNodeName());
            history.setNodeType(node.getNodeType());
            
            // TODO: 从用户服务获取操作人姓名
            
            activityHistoryRepository.save(history);
            log.debug("活动历史记录成功，活动类型: {}", activityType);
        } catch (Exception e) {
            log.error("记录活动历史失败，流程实例ID: {}, 节点ID: {}", 
                    flowInstance.getId(), node.getId(), e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 查询流程实例历史
     */
    public FlowInstanceHistory getFlowInstanceHistory(Long flowInstanceId) {
        return flowInstanceHistoryRepository.findByFlowInstanceId(flowInstanceId);
    }
    
    /**
     * 查询任务历史列表
     */
    public List<TaskHistory> getTaskHistoryList(Long flowInstanceId) {
        return taskHistoryRepository.findByFlowInstanceId(flowInstanceId);
    }
    
    /**
     * 查询活动历史列表
     */
    public List<ActivityHistory> getActivityHistoryList(Long flowInstanceId) {
        return activityHistoryRepository.findByFlowInstanceId(flowInstanceId);
    }
    
    /**
     * 根据处理人查询任务历史
     */
    public List<TaskHistory> getTaskHistoryByHandler(Long handlerId) {
        return taskHistoryRepository.findByHandlerId(handlerId);
    }
    
    /**
     * 获取操作名称
     */
    private String getActionName(String action) {
        if (action == null) {
            return "未知操作";
        }
        switch (action) {
            case "approve":
                return "同意";
            case "reject":
                return "拒绝";
            case "forward":
                return "转发";
            case "return":
                return "退回";
            case "delegate":
                return "委派";
            default:
                return action;
        }
    }
}

