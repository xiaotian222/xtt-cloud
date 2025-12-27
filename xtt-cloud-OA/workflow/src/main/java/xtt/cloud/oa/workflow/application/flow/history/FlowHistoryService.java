package xtt.cloud.oa.workflow.application.flow.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode;
import xtt.cloud.oa.workflow.domain.flow.model.entity.history.FlowInstanceHistory;
import xtt.cloud.oa.workflow.domain.flow.model.entity.history.TaskHistory;
import xtt.cloud.oa.workflow.domain.flow.model.entity.history.ActivityHistory;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskAction;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.history.ActivityType;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowInstanceHistoryRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.TaskHistoryRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.ActivityHistoryRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowInstanceRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowDefinitionRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeInstanceRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.DocumentRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 流程历史应用服务
 * 
 * 负责记录和查询流程历史信息
 * 
 * @author xtt
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
     */
    @Transactional
    public void recordFlowInstanceHistory(FlowInstance flowInstance) {
        if (flowInstance == null) {
            log.warn("流程实例为空，无法记录历史");
            return;
        }
        
        log.debug("记录流程实例历史，流程实例ID: {}", 
                flowInstance.getId() != null ? flowInstance.getId().getValue() : null);
        
        try {
            // 检查是否已存在历史记录
            Optional<FlowInstanceHistory> existingOpt = flowInstanceHistoryRepository.findByFlowInstanceId(
                    flowInstance.getId() != null ? flowInstance.getId().getValue() : null);
            
            FlowInstanceHistory history;
            if (existingOpt.isPresent()) {
                history = existingOpt.get();
            } else {
                // 创建新的历史记录
                history = FlowInstanceHistory.create(
                        flowInstance.getId() != null ? flowInstance.getId().getValue() : null,
                        flowInstance.getDocumentId(),
                        flowInstance.getFlowDefId(),
                        null, // flowDefName 需要从 FlowDefinition 获取
                        flowInstance.getFlowType(),
                        flowInstance.getFlowMode(),
                        flowInstance.getStatus(),
                        null, // initiatorId 需要从 Document 获取
                        flowInstance.getStartTime());
            }
            
            // 填充基本信息
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
                Optional<xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowDefinition> flowDefOpt = 
                        flowDefinitionRepository.findById(
                                xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowDefinitionId.of(
                                        flowInstance.getFlowDefId()));
                if (flowDefOpt.isPresent()) {
                    history.setFlowDefName(flowDefOpt.get().getName());
                }
            }
            
            // 获取发起人信息（从文档获取）
            if (flowInstance.getDocumentId() != null) {
                Optional<xtt.cloud.oa.workflow.domain.flow.model.entity.Document> documentOpt = 
                        documentRepository.findById(flowInstance.getDocumentId());
                if (documentOpt.isPresent()) {
                    xtt.cloud.oa.workflow.domain.flow.model.entity.Document document = documentOpt.get();
                    history.setInitiatorId(document.getCreatorId());
                    history.setInitiatorDeptId(document.getDeptId());
                    // TODO: 从用户服务获取姓名和部门名称
                }
            }
            
            // 统计节点信息
            List<FlowNodeInstance> nodeInstances = flowNodeInstanceRepository.findByFlowInstanceId(
                    flowInstance.getId() != null ? flowInstance.getId().getValue() : null);
            history.setTotalNodes(nodeInstances.size());
            long completedCount = nodeInstances.stream()
                .filter(ni -> ni.getStatus() != null && ni.getStatus().isFinished())
                .count();
            history.setCompletedNodes((int) completedCount);
            
            // 保存或更新
            if (existingOpt.isPresent()) {
                flowInstanceHistoryRepository.update(history);
            } else {
                flowInstanceHistoryRepository.save(history);
            }
            
            log.debug("流程实例历史记录成功，流程实例ID: {}", 
                    flowInstance.getId() != null ? flowInstance.getId().getValue() : null);
        } catch (Exception e) {
            log.error("记录流程实例历史失败，流程实例ID: {}", 
                    flowInstance.getId() != null ? flowInstance.getId().getValue() : null, e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 记录任务历史
     * 当任务处理完成时调用
     */
    @Transactional
    public void recordTaskHistory(FlowNodeInstance nodeInstance, TaskAction action, String comments) {
        if (nodeInstance == null) {
            log.warn("节点实例为空，无法记录历史");
            return;
        }
        
        log.debug("记录任务历史，节点实例ID: {}, 操作: {}", nodeInstance.getId(), action);
        
        try {
            FlowInstance flowInstance = flowInstanceRepository.findById(
                    xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowInstanceId.of(
                            nodeInstance.getFlowInstanceId()))
                    .orElse(null);
            
            if (flowInstance == null) {
                log.warn("流程实例不存在，无法记录历史");
                return;
            }
            
            // 检查是否已存在历史记录
            Optional<TaskHistory> existingOpt = taskHistoryRepository.findByNodeInstanceId(
                    nodeInstance.getId());
            
            TaskHistory history;
            if (existingOpt.isPresent()) {
                history = existingOpt.get();
            } else {
                // 创建新的历史记录
                history = TaskHistory.create(
                        flowInstance.getId() != null ? flowInstance.getId().getValue() : null,
                        nodeInstance.getId(),
                        flowInstance.getDocumentId(),
                        null, // documentTitle 需要从 Document 获取
                        null, // taskName 需要从 FlowNode 获取
                        nodeInstance.getApprover() != null ? nodeInstance.getApprover().getUserId() : null,
                        TaskType.USER,
                        action,
                        comments,
                        nodeInstance.getCreatedAt(),
                        nodeInstance.getHandledAt() != null ? nodeInstance.getHandledAt() : LocalDateTime.now(),
                        nodeInstance.getStatus());
            }
            
            // 更新信息
            history.setAction(action);
            history.setComments(comments);
            history.setEndTime(nodeInstance.getHandledAt() != null 
                ? nodeInstance.getHandledAt() 
                : LocalDateTime.now());
            history.setStatus(nodeInstance.getStatus());
            
            // 计算持续时间
            if (history.getStartTime() != null && history.getEndTime() != null) {
                Duration duration = Duration.between(history.getStartTime(), history.getEndTime());
                history.setDuration(duration.getSeconds());
            }
            
            // 获取节点信息
            Optional<FlowNode> nodeOpt = flowNodeRepository.findById(
                    xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowNodeId.of(nodeInstance.getNodeId()));
            if (nodeOpt.isPresent()) {
                history.setTaskName(nodeOpt.get().getNodeName());
            }
            
            // 获取文档信息
            if (flowInstance.getDocumentId() != null) {
                Optional<xtt.cloud.oa.workflow.domain.flow.model.entity.Document> documentOpt = 
                        documentRepository.findById(flowInstance.getDocumentId());
                if (documentOpt.isPresent()) {
                    history.setDocumentTitle(documentOpt.get().getTitle());
                }
            }
            
            // 设置处理人信息
            if (nodeInstance.getApprover() != null) {
                history.setHandlerId(nodeInstance.getApprover().getUserId());
                history.setHandlerDeptId(nodeInstance.getApprover().getDeptId());
                // TODO: 从用户服务获取处理人姓名和部门名称
            }
            
            // 保存或更新
            if (existingOpt.isPresent()) {
                taskHistoryRepository.update(history);
            } else {
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
     */
    @Transactional
    public void recordActivityHistory(
            FlowInstance flowInstance,
            FlowNodeInstance nodeInstance,
            FlowNode node,
            ActivityType activityType,
            Long operatorId,
            String comments) {
        if (flowInstance == null || node == null) {
            log.warn("流程实例或节点为空，无法记录活动历史");
            return;
        }
        
        log.debug("记录活动历史，流程实例ID: {}, 节点ID: {}, 活动类型: {}", 
                flowInstance.getId() != null ? flowInstance.getId().getValue() : null,
                node.getId(), activityType);
        
        try {
            ActivityHistory history = ActivityHistory.create(
                    flowInstance.getId() != null ? flowInstance.getId().getValue() : null,
                    nodeInstance != null ? nodeInstance.getId() : null,
                    node.getId(),
                    node.getNodeName(),
                    node.getNodeType(),
                    activityType,
                    operatorId,
                    comments);
            
            // TODO: 从用户服务获取操作人姓名
            
            activityHistoryRepository.save(history);
            log.debug("活动历史记录成功，活动类型: {}", activityType);
        } catch (Exception e) {
            log.error("记录活动历史失败，流程实例ID: {}, 节点ID: {}", 
                    flowInstance.getId() != null ? flowInstance.getId().getValue() : null,
                    node.getId(), e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 查询流程实例历史
     */
    @Transactional(readOnly = true)
    public Optional<FlowInstanceHistory> getFlowInstanceHistory(Long flowInstanceId) {
        return flowInstanceHistoryRepository.findByFlowInstanceId(flowInstanceId);
    }
    
    /**
     * 查询任务历史列表
     */
    @Transactional(readOnly = true)
    public List<TaskHistory> getTaskHistoryList(Long flowInstanceId) {
        return taskHistoryRepository.findByFlowInstanceId(flowInstanceId);
    }
    
    /**
     * 查询活动历史列表
     */
    @Transactional(readOnly = true)
    public List<ActivityHistory> getActivityHistoryList(Long flowInstanceId) {
        return activityHistoryRepository.findByFlowInstanceId(flowInstanceId);
    }
    
    /**
     * 根据处理人查询任务历史
     */
    @Transactional(readOnly = true)
    public List<TaskHistory> getTaskHistoryByHandler(Long handlerId) {
        return taskHistoryRepository.findByHandlerId(handlerId);
    }
}
