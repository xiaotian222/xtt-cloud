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
import xtt.cloud.oa.document.domain.entity.flow.task.DoneTask;
import xtt.cloud.oa.document.domain.entity.flow.definition.FlowDefinition;
import xtt.cloud.oa.document.domain.entity.flow.FlowInstance;
import xtt.cloud.oa.document.domain.entity.flow.definition.FlowNode;
import xtt.cloud.oa.document.domain.entity.flow.FlowNodeInstance;
import xtt.cloud.oa.document.domain.entity.flow.history.ActivityHistory;
import xtt.cloud.oa.document.application.flow.repository.FlowDefinitionRepository;
import xtt.cloud.oa.document.application.flow.repository.FlowInstanceRepository;
import xtt.cloud.oa.document.application.flow.repository.FlowNodeInstanceRepository;
import xtt.cloud.oa.document.application.flow.repository.FlowNodeRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程引擎核心服务
 * 负责流程启动、节点流转、流程结束等核心逻辑
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Service
public class FlowEngineService {
    
    private static final Logger log = LoggerFactory.getLogger(FlowEngineService.class);
    
    private final FlowDefinitionRepository flowDefinitionRepository;
    
    private final FlowNodeRepository flowNodeRepository;
    
    private final FlowInstanceRepository flowInstanceRepository;
    
    private final FlowNodeInstanceRepository flowNodeInstanceRepository;
    
    private final DocumentService documentService;
    
    private final FlowNodeService flowNodeService;
    
    private final TaskService taskService;
    
    private final FlowHistoryService flowHistoryService;

    public FlowEngineService(
            FlowDefinitionRepository flowDefinitionRepository,
            FlowNodeRepository flowNodeRepository,
            FlowInstanceRepository flowInstanceRepository,
            FlowNodeInstanceRepository flowNodeInstanceRepository,
            DocumentService documentService,
            FlowNodeService flowNodeService,
            TaskService taskService,
            FlowHistoryService flowHistoryService) {
        this.flowDefinitionRepository = flowDefinitionRepository;
        this.flowNodeRepository = flowNodeRepository;
        this.flowInstanceRepository = flowInstanceRepository;
        this.flowNodeInstanceRepository = flowNodeInstanceRepository;
        this.documentService = documentService;
        this.flowNodeService = flowNodeService;
        this.taskService = taskService;
        this.flowHistoryService = flowHistoryService;
    }

    /**
     * 启动流程
     * 1. 创建流程实例
     * 2. 加载节点列表
     * 3. 创建第一个节点实例
     * 4. 分配审批人
     * 5. 生成待办事项
     * 
     * @param document 文档对象
     * @param flowDef 流程定义对象
     * @return 流程实例
     */
    @Transactional
    public FlowInstance startFlow(Document document, FlowDefinition flowDef) {
        log.info("启动流程，公文ID: {}, 流程定义ID: {}", document.getId(), flowDef.getId());
        
        // 1. 验证流程定义状态
        if (flowDef.getStatus() != FlowDefinition.STATUS_ENABLED) {
            throw new BusinessException("流程定义已停用");
        }
        
        // 2. 加载节点列表
        List<FlowNode> nodes = flowNodeRepository.findByFlowDefId(flowDef.getId());
        if (nodes.isEmpty()) {
            throw new BusinessException("流程定义没有配置节点");
        }
        
        // 3. 创建流程实例
        FlowInstance instance = new FlowInstance();
        instance.setDocumentId(document.getId());
        instance.setFlowDefId(flowDef.getId());
        instance.setFlowType(determineFlowType(document.getId()));
        instance.setStatus(FlowInstance.STATUS_PROCESSING);
        // 检查流程定义中是否有允许自由流的节点，如果有则标记为混合流
        boolean hasFreeFlowNode = nodes.stream()
            .anyMatch(node -> node.getAllowFreeFlow() != null && node.getAllowFreeFlow() == 1);
        instance.setFlowMode(hasFreeFlowNode ? FlowInstance.FLOW_MODE_MIXED : FlowInstance.FLOW_MODE_FIXED);
        instance.setStartTime(LocalDateTime.now());
        instance.setCreatedAt(LocalDateTime.now());
        instance.setUpdatedAt(LocalDateTime.now());
        flowInstanceRepository.save(instance);
        log.info("流程实例创建成功，ID: {}, 流程模式: {}", instance.getId(), instance.getFlowMode());
        
        // 4. 创建第一个节点实例
        FlowNode firstNode = nodes.get(0);
        List<FlowNodeInstance> firstNodeInstances = flowNodeService.createNodeInstance(instance, firstNode, document);
        
        // 5. 更新流程实例当前节点
        instance.setCurrentNodeId(firstNode.getId());
        flowInstanceRepository.update(instance);
        
        // 6. 记录流程实例历史
        flowHistoryService.recordFlowInstanceHistory(instance);
        
        // 7. 记录节点创建活动历史
        for (FlowNodeInstance nodeInstance : firstNodeInstances) {
            flowHistoryService.recordActivityHistory(
                instance,
                nodeInstance,
                firstNode,
                ActivityHistory.ACTIVITY_TYPE_CREATE,
                ActivityHistory.OPERATION_CREATE,
                document.getCreatorId(),
                "流程启动，创建第一个节点"
            );
        }
        
        log.info("流程启动成功，流程实例ID: {}, 第一个节点: {}", instance.getId(), firstNode.getNodeName());
        return instance;
    }


    /**
     * 流转到下一个节点
     */
    public void moveToNextNode(FlowInstance flowInstance, FlowNodeInstance currentNode) {
        log.debug("流转到下一个节点，流程实例ID: {}, 当前节点实例ID: {}", 
                flowInstance.getId(), currentNode.getId());
        
        FlowNode currentNodeDef = flowNodeRepository.findById(currentNode.getNodeId());
        
        // 判断节点类型和并行模式
        if (currentNodeDef.isParallelMode()) {
            // 并行节点：根据模式检查是否满足流转条件
            boolean canMoveNext = false;
            if (currentNodeDef.isParallelAllMode()) {
                // 会签模式：所有节点都完成
                canMoveNext = flowNodeService.allParallelNodesCompleted(flowInstance, currentNodeDef);
                log.debug("会签模式检查，流程实例ID: {}, 节点ID: {}, 是否全部完成: {}", 
                        flowInstance.getId(), currentNodeDef.getId(), canMoveNext);
            } else if (currentNodeDef.isParallelAnyMode()) {
                // 或签模式：任一节点完成
                canMoveNext = flowNodeService.anyParallelNodeCompleted(flowInstance, currentNodeDef);
                log.debug("或签模式检查，流程实例ID: {}, 节点ID: {}, 是否任一完成: {}", 
                        flowInstance.getId(), currentNodeDef.getId(), canMoveNext);
            }
            
            if (canMoveNext) {
                // 满足流转条件，流转到下一个节点（可能是多个，用于并行分支）
                List<FlowNode> nextNodes = flowNodeService.getNextNodes(flowInstance, currentNodeDef);
                if (!nextNodes.isEmpty()) {
                    Document document = documentService.getDocument(flowInstance.getDocumentId());
                    // 为每个下一个节点创建节点实例（支持并行分支）
                    for (FlowNode nextNode : nextNodes) {
                        flowNodeService.createNodeInstance(flowInstance, nextNode, document);
                    }
                    // 如果有多个下一个节点，更新当前节点为第一个（或根据业务规则选择）
                    flowInstance.setCurrentNodeId(nextNodes.get(0).getId());
                    flowInstanceRepository.update(flowInstance);
                } else {
                    // 没有下一个节点，流程结束
                    completeFlow(flowInstance);
                }
            }
        } else {
            // 串行节点：直接流转到下一个节点（可能是多个，用于并行分支）
            List<FlowNode> nextNodes = flowNodeService.getNextNodes(flowInstance, currentNodeDef);
            if (!nextNodes.isEmpty()) {
                Document document = documentService.getDocument(flowInstance.getDocumentId());
                // 为每个下一个节点处理（支持并行分支）
                for (FlowNode nextNode : nextNodes) {
                    // 检查跳过条件
                    if (flowNodeService.shouldSkipNode(nextNode, flowInstance)) {
                        // 跳过当前节点，继续查找下一个
                        FlowNodeInstance skippedNode = flowNodeService.createSkippedNodeInstance(flowInstance, nextNode);
                        moveToNextNode(flowInstance, skippedNode);
                        return;
                    }
                    
                    // 创建节点实例
                    flowNodeService.createNodeInstance(flowInstance, nextNode, document);
                }
                // 如果有多个下一个节点，更新当前节点为第一个（或根据业务规则选择）
                flowInstance.setCurrentNodeId(nextNodes.get(0).getId());
                flowInstanceRepository.update(flowInstance);
            } else {
                // 没有下一个节点，流程结束
                completeFlow(flowInstance);
            }
        }
    }

    /**
     * 回退到指定节点
     * 将流程回退到之前的某个节点，重新审批
     *
     * @param flowInstance 流程实例
     * @param targetNodeId 目标节点ID
     * @param operatorId 操作人ID
     * @param reason 回退原因
     */
    @Transactional
    public void rollbackToNode(FlowInstance flowInstance, Long targetNodeId,Long operatorId, String reason) {
        // 1. 验证回退权限
        // 2. 验证目标节点是否存在且在当前节点之前
        // 3. 取消当前节点及之后的所有待办任务
        // 4. 更新流程实例当前节点
        // 5. 创建目标节点的节点实例
        // 6. 分配审批人
        // 7. 生成待办任务
        // 8. 记录活动历史
    }

    /**
     * 判断流程是否结束
     */
    private boolean isFlowCompleted(FlowInstance flowInstance) {
        FlowNode currentNode = flowNodeRepository.findById(flowInstance.getCurrentNodeId());
        
        if (currentNode != null && currentNode.getIsLastNode() != null && currentNode.getIsLastNode() == 1) {
            // 检查最后一个节点的所有实例是否完成
            if (currentNode.isParallelAllMode()) {
                // 会签模式：所有节点都完成
                return flowNodeService.allParallelNodesCompleted(flowInstance, currentNode);
            } else if (currentNode.isParallelAnyMode()) {
                // 或签模式：任一节点完成
                return flowNodeService.anyParallelNodeCompleted(flowInstance, currentNode);
            } else {
                // 串行模式：检查是否有节点实例且已完成
                List<FlowNodeInstance> nodeInstances = flowNodeInstanceRepository.findByFlowInstanceIdAndNodeId(
                    flowInstance.getId(), currentNode.getId());
                return !nodeInstances.isEmpty() && 
                       nodeInstances.stream().anyMatch(ni -> ni.getStatus() == FlowNodeInstance.STATUS_COMPLETED);
            }
        }
        
        return false;
    }
    
    /**
     * 完成流程
     */
    private void completeFlow(FlowInstance flowInstance) {
        log.info("完成流程，流程实例ID: {}", flowInstance.getId());
        
        flowInstance.setStatus(FlowInstance.STATUS_COMPLETED);
        flowInstance.setEndTime(LocalDateTime.now());
        flowInstance.setUpdatedAt(LocalDateTime.now());
        flowInstanceRepository.update(flowInstance);
        
        // 更新公文状态为已发布
        Document document = documentService.getDocument(flowInstance.getDocumentId());
        if (document != null) {
            document.setStatus(Document.STATUS_PUBLISHED);
            document.setPublishTime(LocalDateTime.now());
            documentService.updateDocument(document.getId(), document);
        }
        
        // 更新流程实例历史
        flowHistoryService.recordFlowInstanceHistory(flowInstance);
        
        log.info("流程完成，流程实例ID: {}", flowInstance.getId());
    }
    
    /**
     * 终止流程
     */
    public void terminateFlow(FlowInstance flowInstance, String reason) {
        log.info("终止流程，流程实例ID: {}, 原因: {}", flowInstance.getId(), reason);
        
        flowInstance.setStatus(FlowInstance.STATUS_TERMINATED);
        flowInstance.setEndTime(LocalDateTime.now());
        flowInstance.setUpdatedAt(LocalDateTime.now());
        flowInstanceRepository.update(flowInstance);
        
        // 更新流程实例历史
        flowHistoryService.recordFlowInstanceHistory(flowInstance);
    }
    
    /**
     * 确定流程类型
     */
    private Integer determineFlowType(Long documentId) {
        // TODO: 根据公文类型或其他条件确定流程类型
        // 暂时返回发文流程
        return FlowInstance.TYPE_ISSUANCE;
    }

    /**
     *
     * 撤回流程
     * 发起人可以撤回已提交但尚未完成的流程
     *
     * @param flowInstanceId 流程实例ID
     * @param initiatorId 发起人ID
     * @param reason 撤回原因
     */
    @Transactional
    public void withdrawFlow(Long flowInstanceId, Long initiatorId, String reason) {
        // 1. 验证发起人权限
        // 2. 检查流程状态（只能撤回进行中的流程）
        // 3. 检查当前节点是否允许撤回
        // 4. 取消所有待办任务
        // 5. 更新流程状态为已撤回
        // 6. 记录活动历史
    }

}

