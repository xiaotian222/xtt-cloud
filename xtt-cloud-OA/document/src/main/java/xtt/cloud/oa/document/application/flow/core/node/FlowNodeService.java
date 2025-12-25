package xtt.cloud.oa.document.application.flow.core.node;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import xtt.cloud.oa.common.BusinessException;
import xtt.cloud.oa.document.application.flow.history.FlowHistoryService;
import xtt.cloud.oa.document.application.flow.task.TaskService;
import xtt.cloud.oa.document.domain.entity.flow.definition.Document;
import xtt.cloud.oa.document.domain.entity.flow.FlowInstance;
import xtt.cloud.oa.document.domain.entity.flow.definition.FlowNode;
import xtt.cloud.oa.document.domain.entity.flow.FlowNodeInstance;
import xtt.cloud.oa.document.domain.entity.flow.history.ActivityHistory;
import xtt.cloud.oa.document.application.flow.repository.FlowNodeInstanceRepository;
import xtt.cloud.oa.document.application.flow.repository.FlowNodeRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 流程节点服务
 * 负责节点相关的操作，包括节点实例创建、节点流转、审批人分配等
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Service
public class FlowNodeService {
    
    private static final Logger log = LoggerFactory.getLogger(FlowNodeService.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final FlowNodeRepository flowNodeRepository;
    
    private final FlowNodeInstanceRepository flowNodeInstanceRepository;
    
    private final TaskService taskService;
    
    private final FlowHistoryService flowHistoryService;
    
    public FlowNodeService(
            FlowNodeRepository flowNodeRepository,
            FlowNodeInstanceRepository flowNodeInstanceRepository,
            TaskService taskService,
            FlowHistoryService flowHistoryService) {
        this.flowNodeRepository = flowNodeRepository;
        this.flowNodeInstanceRepository = flowNodeInstanceRepository;
        this.taskService = taskService;
        this.flowHistoryService = flowHistoryService;
    }
    
    /**
     * 创建节点实例
     * 
     * @param flowInstance 流程实例
     * @param node 节点定义
     * @param document 文档
     * @return 创建的节点实例列表
     */
    @Transactional
    public List<FlowNodeInstance> createNodeInstance(FlowInstance flowInstance, FlowNode node, Document document) {
        log.debug("创建节点实例，节点名称: {}, 节点ID: {}", node.getNodeName(), node.getId());

        // 分配审批人
        List<Long> approverIds = assignApprovers(node, flowInstance, document);
        if (approverIds.isEmpty()) {
            throw new BusinessException("节点 " + node.getNodeName() + " 无法分配审批人");
        }

        // 为每个审批人创建节点实例
        List<FlowNodeInstance> nodeInstances = new ArrayList<>();
        for (Long approverId : approverIds) {
            FlowNodeInstance nodeInstance = new FlowNodeInstance();
            nodeInstance.setFlowInstanceId(flowInstance.getId());
            nodeInstance.setNodeId(node.getId());
            nodeInstance.setApproverId(approverId);
            nodeInstance.setStatus(FlowNodeInstance.STATUS_PENDING);
            nodeInstance.setCreatedAt(LocalDateTime.now());
            nodeInstance.setUpdatedAt(LocalDateTime.now());
            flowNodeInstanceRepository.save(nodeInstance);
            nodeInstances.add(nodeInstance);

            // 生成待办任务
            taskService.createTodoTask(nodeInstance, approverId, flowInstance, document);
            
            // 记录节点创建活动历史
            flowHistoryService.recordActivityHistory(
                flowInstance,
                nodeInstance,
                node,
                ActivityHistory.ACTIVITY_TYPE_CREATE,
                ActivityHistory.OPERATION_CREATE,
                null, // 系统创建
                "创建节点实例"
            );
        }

        log.info("节点实例创建成功，节点名称: {}, 创建了 {} 个实例", node.getNodeName(), nodeInstances.size());
        return nodeInstances;
    }
    
    /**
     * 创建已跳过的节点实例
     * 
     * @param flowInstance 流程实例
     * @param node 节点定义
     * @return 创建的节点实例
     */
    @Transactional
    public FlowNodeInstance createSkippedNodeInstance(FlowInstance flowInstance, FlowNode node) {
        FlowNodeInstance nodeInstance = new FlowNodeInstance();
        nodeInstance.setFlowInstanceId(flowInstance.getId());
        nodeInstance.setNodeId(node.getId());
        nodeInstance.setStatus(FlowNodeInstance.STATUS_SKIPPED);
        nodeInstance.setComments("节点已跳过（满足跳过条件）");
        nodeInstance.setHandledAt(LocalDateTime.now());
        nodeInstance.setCreatedAt(LocalDateTime.now());
        nodeInstance.setUpdatedAt(LocalDateTime.now());
        flowNodeInstanceRepository.save(nodeInstance);
        
        // 记录节点跳过活动历史
        flowHistoryService.recordActivityHistory(
            flowInstance,
            nodeInstance,
            node,
            ActivityHistory.ACTIVITY_TYPE_SKIP,
            ActivityHistory.OPERATION_SKIP,
            null, // 系统自动跳过
            "节点已跳过（满足跳过条件）"
        );
        
        return nodeInstance;
    }
    
    /**
     * 获取下一个节点（单个）
     * 优先使用 nextNodeId 或 nextNodeIds（取第一个），如果没有则回退到 orderNum 方式
     * 
     * @param flowInstance 流程实例
     * @param currentNode 当前节点
     * @return 下一个节点，如果不存在则返回 null
     */
    public FlowNode getNextNode(FlowInstance flowInstance, FlowNode currentNode) {
        List<FlowNode> nextNodes = getNextNodes(flowInstance, currentNode);
        return nextNodes.isEmpty() ? null : nextNodes.get(0);
    }
    
    /**
     * 获取下一个节点列表（支持并行分支）- 内部方法
     * 优先级：
     * 1. nextNodeIds（JSON格式，多个下一个节点，用于并行分支）
     * 2. nextNodeId（单个下一个节点，用于串行流程）
     * 3. orderNum + 1（兼容旧数据）
     * 
     * @param flowDefId 流程定义ID
     * @param currentNode 当前节点
     * @return 下一个节点列表
     */
    private List<FlowNode> getNextNodes(Long flowDefId, FlowNode currentNode) {
        List<FlowNode> nextNodes = new ArrayList<>();
        
        // 1. 优先使用 nextNodeIds（多个下一个节点，用于并行分支）
        if (StringUtils.hasText(currentNode.getNextNodeIds())) {
            try {
                List<Long> nextNodeIdList = objectMapper.readValue(
                    currentNode.getNextNodeIds(), 
                    new TypeReference<List<Long>>() {}
                );
                if (nextNodeIdList != null && !nextNodeIdList.isEmpty()) {
                    for (Long nextNodeId : nextNodeIdList) {
                        FlowNode nextNode = flowNodeRepository.findById(nextNodeId);
                        if (nextNode != null && nextNode.getFlowDefId().equals(flowDefId)) {
                            nextNodes.add(nextNode);
                        } else {
                            log.warn("nextNodeIds 中指定的节点不存在或不属于当前流程定义，节点ID: {}", nextNodeId);
                        }
                    }
                    if (!nextNodes.isEmpty()) {
                        log.debug("使用 nextNodeIds 获取下一个节点列表，当前节点ID: {}, 下一个节点数量: {}", 
                                currentNode.getId(), nextNodes.size());
                        return nextNodes;
                    }
                }
            } catch (Exception e) {
                log.error("解析 nextNodeIds 失败，当前节点ID: {}, nextNodeIds: {}", 
                        currentNode.getId(), currentNode.getNextNodeIds(), e);
            }
        }
        
        // 2. 其次使用 nextNodeId（单个下一个节点）
        if (currentNode.getNextNodeId() != null) {
            FlowNode nextNode = flowNodeRepository.findById(currentNode.getNextNodeId());
            if (nextNode != null && nextNode.getFlowDefId().equals(flowDefId)) {
                nextNodes.add(nextNode);
                log.debug("使用 nextNodeId 获取下一个节点，当前节点ID: {}, 下一个节点ID: {}", 
                        currentNode.getId(), currentNode.getNextNodeId());
                return nextNodes;
            } else {
                log.warn("nextNodeId 指定的节点不存在或不属于当前流程定义，当前节点ID: {}, nextNodeId: {}", 
                        currentNode.getId(), currentNode.getNextNodeId());
            }
        }
        
        // 3. 回退到使用 orderNum 方式（兼容旧数据）
        FlowNode nextNode = flowNodeRepository.findByFlowDefIdAndOrderNum(
            flowDefId, currentNode.getOrderNum() + 1);
        
        if (nextNode != null) {
            nextNodes.add(nextNode);
            log.debug("使用 orderNum 获取下一个节点，当前节点ID: {}, 当前 orderNum: {}, 下一个 orderNum: {}", 
                    currentNode.getId(), currentNode.getOrderNum(), nextNode.getOrderNum());
        } else {
            log.debug("未找到下一个节点，当前节点ID: {}, orderNum: {}", currentNode.getId(), currentNode.getOrderNum());
        }
        
        return nextNodes;
    }
    
    /**
     * 判断是否应该跳过节点
     * 
     * @param node 节点定义
     * @param flowInstance 流程实例
     * @return true 如果应该跳过节点
     */
    public boolean shouldSkipNode(FlowNode node, FlowInstance flowInstance) {
        if (!StringUtils.hasText(node.getSkipCondition())) {
            return false;
        }
        
        // TODO: 实现条件表达式解析（使用 SpEL）
        // 暂时返回 false
        return false;
    }
    
    /**
     * 检查并行节点是否全部完成（会签模式）
     * 
     * @param flowInstance 流程实例
     * @param node 节点定义
     * @return true 如果所有并行节点都已完成
     */
    public boolean allParallelNodesCompleted(FlowInstance flowInstance, FlowNode node) {
        List<FlowNodeInstance> nodeInstances = flowNodeInstanceRepository.findByFlowInstanceIdAndNodeId(
            flowInstance.getId(), node.getId());
        
        if (nodeInstances.isEmpty()) {
            return false;
        }
        
        boolean allCompleted = nodeInstances.stream()
            .allMatch(ni -> ni.getStatus() == FlowNodeInstance.STATUS_COMPLETED);
        
        log.debug("会签模式检查，流程实例ID: {}, 节点ID: {}, 总节点数: {}, 已完成数: {}, 是否全部完成: {}", 
                flowInstance.getId(), node.getId(), nodeInstances.size(), 
                nodeInstances.stream().filter(ni -> ni.getStatus() == FlowNodeInstance.STATUS_COMPLETED).count(),
                allCompleted);
        
        return allCompleted;
    }
    
    /**
     * 检查并行节点是否任一完成（或签模式）
     * 
     * @param flowInstance 流程实例
     * @param node 节点定义
     * @return true 如果任一并行节点已完成
     */
    public boolean anyParallelNodeCompleted(FlowInstance flowInstance, FlowNode node) {
        List<FlowNodeInstance> nodeInstances = flowNodeInstanceRepository.findByFlowInstanceIdAndNodeId(
            flowInstance.getId(), node.getId());
        
        if (nodeInstances.isEmpty()) {
            return false;
        }
        
        boolean anyCompleted = nodeInstances.stream()
            .anyMatch(ni -> ni.getStatus() == FlowNodeInstance.STATUS_COMPLETED);
        
        log.debug("或签模式检查，流程实例ID: {}, 节点ID: {}, 总节点数: {}, 已完成数: {}, 是否任一完成: {}", 
                flowInstance.getId(), node.getId(), nodeInstances.size(),
                nodeInstances.stream().filter(ni -> ni.getStatus() == FlowNodeInstance.STATUS_COMPLETED).count(),
                anyCompleted);
        
        return anyCompleted;
    }
    
    /**
     * 获取下一个节点列表（支持并行分支）
     * 优先级：
     * 1. nextNodeIds（JSON格式，多个下一个节点，用于并行分支）
     * 2. nextNodeId（单个下一个节点，用于串行流程）
     * 3. orderNum + 1（兼容旧数据）
     * 
     * @param flowInstance 流程实例
     * @param currentNode 当前节点
     * @return 下一个节点列表
     */
    public List<FlowNode> getNextNodes(FlowInstance flowInstance, FlowNode currentNode) {
        return getNextNodes(flowInstance.getFlowDefId(), currentNode);
    }
    
    /**
     * 分配审批人
     * 
     * @param node 节点定义
     * @param flowInstance 流程实例
     * @param document 文档
     * @return 审批人ID列表
     */
    public List<Long> assignApprovers(FlowNode node, FlowInstance flowInstance, Document document) {
        List<Long> approverIds;
        
        switch (node.getApproverType()) {
            case FlowNode.APPROVER_TYPE_USER:
                // 指定人员：直接解析用户ID列表
                approverIds = parseUserIds(node.getApproverValue());
                break;
            case FlowNode.APPROVER_TYPE_ROLE:
                // 指定角色：查询该角色下的所有用户
                approverIds = getUserIdsByRole(node.getApproverValue());
                break;
            case FlowNode.APPROVER_TYPE_DEPT_LEADER:
                // 指定部门负责人：查询部门负责人
                approverIds = getDeptLeaderIds(node.getApproverValue(), document);
                break;
            case FlowNode.APPROVER_TYPE_INITIATOR:
                // 发起人指定：从流程实例中获取发起人指定的审批人
                approverIds = getApproversFromFlowInstance(flowInstance);
                break;
            default:
                throw new BusinessException("不支持的审批人类型: " + node.getApproverType());
        }
        
        return approverIds;
    }
    
    // ========== 辅助方法（需要集成用户服务） ==========
    
    /**
     * 解析用户ID列表
     */
    private List<Long> parseUserIds(String userIdsStr) {
        if (!StringUtils.hasText(userIdsStr)) {
            return new ArrayList<>();
        }
        // 支持逗号分隔或JSON格式
        try {
            return objectMapper.readValue(userIdsStr, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            // 尝试逗号分隔格式
            List<Long> userIds = new ArrayList<>();
            for (String idStr : userIdsStr.split(",")) {
                try {
                    userIds.add(Long.parseLong(idStr.trim()));
                } catch (NumberFormatException ex) {
                    log.warn("无效的用户ID: {}", idStr);
                }
            }
            return userIds;
        }
    }
    
    /**
     * 根据角色获取用户ID列表（TODO: 集成用户服务）
     */
    private List<Long> getUserIdsByRole(String roleValue) {
        // TODO: 调用用户服务获取角色下的所有用户
        log.warn("getUserIdsByRole 未实现，角色值: {}", roleValue);
        return new ArrayList<>();
    }
    
    /**
     * 获取部门负责人ID列表（TODO: 集成用户服务）
     */
    private List<Long> getDeptLeaderIds(String deptValue, Document document) {
        // TODO: 调用用户服务获取部门负责人
        log.warn("getDeptLeaderIds 未实现，部门值: {}", deptValue);
        return new ArrayList<>();
    }
    
    /**
     * 从流程实例中获取发起人指定的审批人（TODO: 实现）
     */
    private List<Long> getApproversFromFlowInstance(FlowInstance flowInstance) {
        // TODO: 从流程实例的扩展信息中获取发起人指定的审批人
        log.warn("getApproversFromFlowInstance 未实现，流程实例ID: {}", flowInstance.getId());
        return new ArrayList<>();
    }
}

