package xtt.cloud.oa.document.application.flow.core;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import xtt.cloud.oa.common.BusinessException;
import xtt.cloud.oa.document.domain.entity.flow.Document;
import xtt.cloud.oa.document.domain.entity.flow.task.DoneTask;
import xtt.cloud.oa.document.domain.entity.flow.FlowDefinition;
import xtt.cloud.oa.document.domain.entity.flow.FlowInstance;
import xtt.cloud.oa.document.domain.entity.flow.FlowNode;
import xtt.cloud.oa.document.domain.entity.flow.FlowNodeInstance;
import xtt.cloud.oa.document.domain.mapper.flow.FlowDefinitionMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FlowInstanceMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FlowNodeInstanceMapper;
import xtt.cloud.oa.document.domain.mapper.flow.FlowNodeMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    
    private final FlowDefinitionMapper flowDefinitionMapper;
    
    private final FlowNodeMapper flowNodeMapper;
    
    private final FlowInstanceMapper flowInstanceMapper;
    
    private final FlowNodeInstanceMapper flowNodeInstanceMapper;
    
    private final DocumentService documentService;
    
    private final TaskService taskService;

    public FlowEngineService(FlowDefinitionMapper flowDefinitionMapper, FlowNodeMapper flowNodeMapper, FlowInstanceMapper flowInstanceMapper, FlowNodeInstanceMapper flowNodeInstanceMapper, DocumentService documentService, TaskService taskService) {
        this.flowDefinitionMapper = flowDefinitionMapper;
        this.flowNodeMapper = flowNodeMapper;
        this.flowInstanceMapper = flowInstanceMapper;
        this.flowNodeInstanceMapper = flowNodeInstanceMapper;
        this.documentService = documentService;
        this.taskService = taskService;
    }

    /**
     * 启动流程
     * 1. 创建流程实例
     * 2. 加载流程定义和节点列表
     * 3. 创建第一个节点实例
     * 4. 分配审批人
     * 5. 生成待办事项
     */
    @Transactional
    public FlowInstance startFlow(Long documentId, Long flowDefId) {
        log.info("启动流程，公文ID: {}, 流程定义ID: {}", documentId, flowDefId);
        
        // 1. 验证公文存在
        Document document = documentService.getDocument(documentId);
        if (document == null) {
            throw new BusinessException("公文不存在");
        }
        
        // 2. 加载流程定义
        FlowDefinition flowDef = flowDefinitionMapper.selectById(flowDefId);
        if (flowDef == null) {
            throw new BusinessException("流程定义不存在");
        }
        if (flowDef.getStatus() != FlowDefinition.STATUS_ENABLED) {
            throw new BusinessException("流程定义已停用");
        }
        
        // 3. 加载节点列表
        List<FlowNode> nodes = flowNodeMapper.selectList(
            new LambdaQueryWrapper<FlowNode>()
                .eq(FlowNode::getFlowDefId, flowDefId)
                .orderByAsc(FlowNode::getOrderNum)
        );
        if (nodes.isEmpty()) {
            throw new BusinessException("流程定义没有配置节点");
        }
        
        // 4. 创建流程实例
        FlowInstance instance = new FlowInstance();
        instance.setDocumentId(documentId);
        instance.setFlowDefId(flowDefId);
        instance.setFlowType(determineFlowType(documentId));
        instance.setStatus(FlowInstance.STATUS_PROCESSING);
        // 检查流程定义中是否有允许自由流的节点，如果有则标记为混合流
        boolean hasFreeFlowNode = nodes.stream()
            .anyMatch(node -> node.getAllowFreeFlow() != null && node.getAllowFreeFlow() == 1);
        instance.setFlowMode(hasFreeFlowNode ? FlowInstance.FLOW_MODE_MIXED : FlowInstance.FLOW_MODE_FIXED);
        instance.setStartTime(LocalDateTime.now());
        instance.setCreatedAt(LocalDateTime.now());
        instance.setUpdatedAt(LocalDateTime.now());
        flowInstanceMapper.insert(instance);
        log.info("流程实例创建成功，ID: {}, 流程模式: {}", instance.getId(), instance.getFlowMode());
        
        // 5. 创建第一个节点实例
        FlowNode firstNode = nodes.get(0);
        List<FlowNodeInstance> firstNodeInstances = createNodeInstance(instance, firstNode, document);
        
        // 6. 更新流程实例当前节点
        instance.setCurrentNodeId(firstNode.getId());
        flowInstanceMapper.updateById(instance);
        
        log.info("流程启动成功，流程实例ID: {}, 第一个节点: {}", instance.getId(), firstNode.getNodeName());
        return instance;
    }

    /**
     * 创建节点实例
     */
    private List<FlowNodeInstance> createNodeInstance(FlowInstance flowInstance, FlowNode node, Document document) {
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
            flowNodeInstanceMapper.insert(nodeInstance);
            nodeInstances.add(nodeInstance);

            // 生成待办任务
            taskService.createTodoTask(nodeInstance, approverId, flowInstance, document);
        }

        log.info("节点实例创建成功，节点名称: {}, 创建了 {} 个实例", node.getNodeName(), nodeInstances.size());
        return nodeInstances; // 返回所有节点实例
    }

    /**
     * 处理节点审批
     * 1. 验证审批权限
     * 2. 更新节点实例状态
     * 3. 创建已办事项
     * 4. 判断是否流转到下一个节点
     */
    @Transactional
    public void processNodeApproval(Long nodeInstanceId, String action, String comments, Long approverId) {
        log.info("处理节点审批，节点实例ID: {}, 操作: {}, 审批人ID: {}", nodeInstanceId, action, approverId);
        
        // 1. 验证权限和状态
        FlowNodeInstance nodeInstance = validateApprovalPermission(nodeInstanceId, approverId);
        
        // 2. 根据操作类型处理
        switch (action) {
            case DoneTask.ACTION_APPROVE:
                handleApprove(nodeInstance, comments);
                break;
            case DoneTask.ACTION_REJECT:
                handleReject(nodeInstance, comments);
                break;
            case DoneTask.ACTION_FORWARD:
                handleForward(nodeInstance, comments);
                break;
            case DoneTask.ACTION_RETURN:
                handleReturn(nodeInstance, comments);
                break;
            default:
                throw new BusinessException("不支持的操作类型: " + action);
        }
    }
    
    /**
     * 流转到下一个节点
     */
    private void moveToNextNode(Long flowInstanceId, Long currentNodeInstanceId) {
        log.debug("流转到下一个节点，流程实例ID: {}, 当前节点实例ID: {}", flowInstanceId, currentNodeInstanceId);
        
        FlowInstance flowInstance = flowInstanceMapper.selectById(flowInstanceId);
        FlowNodeInstance currentNode = flowNodeInstanceMapper.selectById(currentNodeInstanceId);
        FlowNode currentNodeDef = flowNodeMapper.selectById(currentNode.getNodeId());
        
        // 判断节点类型和并行模式
        if (currentNodeDef.isParallelMode()) {
            // 并行节点：根据模式检查是否满足流转条件
            boolean canMoveNext = false;
            if (currentNodeDef.isParallelAllMode()) {
                // 会签模式：所有节点都完成
                canMoveNext = allParallelNodesCompleted(flowInstanceId, currentNodeDef.getId());
                log.debug("会签模式检查，流程实例ID: {}, 节点ID: {}, 是否全部完成: {}", 
                        flowInstanceId, currentNodeDef.getId(), canMoveNext);
            } else if (currentNodeDef.isParallelAnyMode()) {
                // 或签模式：任一节点完成
                canMoveNext = anyParallelNodeCompleted(flowInstanceId, currentNodeDef.getId());
                log.debug("或签模式检查，流程实例ID: {}, 节点ID: {}, 是否任一完成: {}", 
                        flowInstanceId, currentNodeDef.getId(), canMoveNext);
            }
            
            if (canMoveNext) {
                // 满足流转条件，流转到下一个节点
                FlowNode nextNode = getNextNode(flowInstance.getFlowDefId(), currentNodeDef);
                if (nextNode != null) {
                    Document document = documentService.getDocument(flowInstance.getDocumentId());
                    List<FlowNodeInstance> nextNodeInstances = createNodeInstance(flowInstance, nextNode, document);
                    flowInstance.setCurrentNodeId(nextNode.getId());
                    flowInstanceMapper.updateById(flowInstance);
                } else {
                    // 没有下一个节点，流程结束
                    completeFlow(flowInstanceId);
                }
            }
        } else {
            // 串行节点：直接流转到下一个节点
            FlowNode nextNode = getNextNode(flowInstance.getFlowDefId(), currentNodeDef);
            if (nextNode != null) {
                // 检查跳过条件
                if (shouldSkipNode(nextNode, flowInstance)) {
                    // 跳过当前节点，继续查找下一个
                    FlowNodeInstance skippedNode = createSkippedNodeInstance(flowInstance, nextNode);
                    moveToNextNode(flowInstanceId, skippedNode.getId());
                    return;
                }
                
                Document document = documentService.getDocument(flowInstance.getDocumentId());
                List<FlowNodeInstance> nextNodeInstances = createNodeInstance(flowInstance, nextNode, document);
                flowInstance.setCurrentNodeId(nextNode.getId());
                flowInstanceMapper.updateById(flowInstance);
            } else {
                // 没有下一个节点，流程结束
                completeFlow(flowInstanceId);
            }
        }
    }
    
    /**
     * 创建已跳过的节点实例
     */
    private FlowNodeInstance createSkippedNodeInstance(FlowInstance flowInstance, FlowNode node) {
        FlowNodeInstance nodeInstance = new FlowNodeInstance();
        nodeInstance.setFlowInstanceId(flowInstance.getId());
        nodeInstance.setNodeId(node.getId());
        nodeInstance.setStatus(FlowNodeInstance.STATUS_SKIPPED);
        nodeInstance.setComments("节点已跳过（满足跳过条件）");
        nodeInstance.setHandledAt(LocalDateTime.now());
        nodeInstance.setCreatedAt(LocalDateTime.now());
        nodeInstance.setUpdatedAt(LocalDateTime.now());
        flowNodeInstanceMapper.insert(nodeInstance);
        return nodeInstance;
    }
    
    /**
     * 分配审批人
     */
    private List<Long> assignApprovers(FlowNode node, FlowInstance flowInstance, Document document) {
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
    
    /**
     * 获取下一个节点
     */
    private FlowNode getNextNode(Long flowDefId, FlowNode currentNode) {
        // 查找下一个顺序号的节点
        FlowNode nextNode = flowNodeMapper.selectOne(
            new LambdaQueryWrapper<FlowNode>()
                .eq(FlowNode::getFlowDefId, flowDefId)
                .eq(FlowNode::getOrderNum, currentNode.getOrderNum() + 1)
                .orderByAsc(FlowNode::getOrderNum)
                .last("LIMIT 1")
        );
        return nextNode;
    }
    
    /**
     * 判断是否应该跳过节点
     */
    private boolean shouldSkipNode(FlowNode node, FlowInstance flowInstance) {
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
     * @param flowInstanceId 流程实例ID
     * @param nodeId 节点ID
     * @return true 如果所有并行节点都已完成
     */
    private boolean allParallelNodesCompleted(Long flowInstanceId, Long nodeId) {
        List<FlowNodeInstance> nodeInstances = flowNodeInstanceMapper.selectList(
            new LambdaQueryWrapper<FlowNodeInstance>()
                .eq(FlowNodeInstance::getFlowInstanceId, flowInstanceId)
                .eq(FlowNodeInstance::getNodeId, nodeId)
        );
        
        if (nodeInstances.isEmpty()) {
            return false;
        }
        
        boolean allCompleted = nodeInstances.stream()
            .allMatch(ni -> ni.getStatus() == FlowNodeInstance.STATUS_COMPLETED);
        
        log.debug("会签模式检查，流程实例ID: {}, 节点ID: {}, 总节点数: {}, 已完成数: {}, 是否全部完成: {}", 
                flowInstanceId, nodeId, nodeInstances.size(), 
                nodeInstances.stream().filter(ni -> ni.getStatus() == FlowNodeInstance.STATUS_COMPLETED).count(),
                allCompleted);
        
        return allCompleted;
    }
    
    /**
     * 检查并行节点是否任一完成（或签模式）
     * 
     * @param flowInstanceId 流程实例ID
     * @param nodeId 节点ID
     * @return true 如果任一并行节点已完成
     */
    private boolean anyParallelNodeCompleted(Long flowInstanceId, Long nodeId) {
        List<FlowNodeInstance> nodeInstances = flowNodeInstanceMapper.selectList(
            new LambdaQueryWrapper<FlowNodeInstance>()
                .eq(FlowNodeInstance::getFlowInstanceId, flowInstanceId)
                .eq(FlowNodeInstance::getNodeId, nodeId)
        );
        
        if (nodeInstances.isEmpty()) {
            return false;
        }
        
        boolean anyCompleted = nodeInstances.stream()
            .anyMatch(ni -> ni.getStatus() == FlowNodeInstance.STATUS_COMPLETED);
        
        log.debug("或签模式检查，流程实例ID: {}, 节点ID: {}, 总节点数: {}, 已完成数: {}, 是否任一完成: {}", 
                flowInstanceId, nodeId, nodeInstances.size(), 
                nodeInstances.stream().filter(ni -> ni.getStatus() == FlowNodeInstance.STATUS_COMPLETED).count(),
                anyCompleted);
        
        return anyCompleted;
    }
    
    /**
     * 判断流程是否结束
     */
    private boolean isFlowCompleted(Long flowInstanceId) {
        FlowInstance flowInstance = flowInstanceMapper.selectById(flowInstanceId);
        FlowNode currentNode = flowNodeMapper.selectById(flowInstance.getCurrentNodeId());
        
        if (currentNode != null && currentNode.getIsLastNode() != null && currentNode.getIsLastNode() == 1) {
            // 检查最后一个节点的所有实例是否完成
            if (currentNode.isParallelAllMode()) {
                // 会签模式：所有节点都完成
                return allParallelNodesCompleted(flowInstanceId, currentNode.getId());
            } else if (currentNode.isParallelAnyMode()) {
                // 或签模式：任一节点完成
                return anyParallelNodeCompleted(flowInstanceId, currentNode.getId());
            } else {
                // 串行模式：检查是否有节点实例且已完成
                List<FlowNodeInstance> nodeInstances = flowNodeInstanceMapper.selectList(
                    new LambdaQueryWrapper<FlowNodeInstance>()
                        .eq(FlowNodeInstance::getFlowInstanceId, flowInstanceId)
                        .eq(FlowNodeInstance::getNodeId, currentNode.getId())
                );
                return !nodeInstances.isEmpty() && 
                       nodeInstances.stream().anyMatch(ni -> ni.getStatus() == FlowNodeInstance.STATUS_COMPLETED);
            }
        }
        
        return false;
    }
    
    /**
     * 完成流程
     */
    private void completeFlow(Long flowInstanceId) {
        log.info("完成流程，流程实例ID: {}", flowInstanceId);
        
        FlowInstance flowInstance = flowInstanceMapper.selectById(flowInstanceId);
        flowInstance.setStatus(FlowInstance.STATUS_COMPLETED);
        flowInstance.setEndTime(LocalDateTime.now());
        flowInstance.setUpdatedAt(LocalDateTime.now());
        flowInstanceMapper.updateById(flowInstance);
        
        // 更新公文状态为已发布
        Document document = documentService.getDocument(flowInstance.getDocumentId());
        if (document != null) {
            document.setStatus(Document.STATUS_PUBLISHED);
            document.setPublishTime(LocalDateTime.now());
            documentService.updateDocument(document.getId(), document);
        }
        
        log.info("流程完成，流程实例ID: {}", flowInstanceId);
    }
    
    /**
     * 处理同意操作
     */
    private void handleApprove(FlowNodeInstance nodeInstance, String comments) {
        // 1. 更新节点实例状态
        nodeInstance.setStatus(FlowNodeInstance.STATUS_COMPLETED);
        nodeInstance.setComments(comments);
        nodeInstance.setHandledAt(LocalDateTime.now());
        nodeInstance.setUpdatedAt(LocalDateTime.now());
        flowNodeInstanceMapper.updateById(nodeInstance);
        
        // 2. 创建已办任务
        FlowInstance flowInstance = flowInstanceMapper.selectById(nodeInstance.getFlowInstanceId());
        Document document = documentService.getDocument(flowInstance.getDocumentId());
        taskService.createDoneTask(nodeInstance, DoneTask.ACTION_APPROVE, comments, flowInstance, document);
        
        // 3. 更新待办任务状态
        taskService.markAsHandled(nodeInstance.getId(), nodeInstance.getApproverId());
        
        // 4. 检查节点是否允许自由流
        FlowNode nodeDef = flowNodeMapper.selectById(nodeInstance.getNodeId());
        if (nodeDef.getAllowFreeFlow() != null && nodeDef.getAllowFreeFlow() == 1) {
            // 节点允许自由流，但不自动流转，需要用户主动执行自由流动作
            log.info("节点允许自由流，节点实例ID: {}，等待用户执行自由流动作", nodeInstance.getId());
            return;
        }
        
        // 5. 判断是否需要等待并行节点
        if (nodeDef.isParallelMode()) {
            // 并行节点：根据模式检查是否满足流转条件
            boolean canMoveNext = false;
            if (nodeDef.isParallelAllMode()) {
                // 会签模式：所有节点都完成
                canMoveNext = allParallelNodesCompleted(nodeInstance.getFlowInstanceId(), nodeDef.getId());
                log.debug("会签模式检查，节点实例ID: {}, 是否全部完成: {}", nodeInstance.getId(), canMoveNext);
            } else if (nodeDef.isParallelAnyMode()) {
                // 或签模式：任一节点完成即可
                canMoveNext = anyParallelNodeCompleted(nodeInstance.getFlowInstanceId(), nodeDef.getId());
                log.debug("或签模式检查，节点实例ID: {}, 是否任一完成: {}", nodeInstance.getId(), canMoveNext);
            }
            
            if (canMoveNext) {
                moveToNextNode(nodeInstance.getFlowInstanceId(), nodeInstance.getId());
            } else {
                log.debug("并行节点尚未满足流转条件，等待其他节点完成，节点实例ID: {}", nodeInstance.getId());
            }
        } else {
            // 串行节点：直接流转
            moveToNextNode(nodeInstance.getFlowInstanceId(), nodeInstance.getId());
        }
    }
    
    /**
     * 处理拒绝操作
     */
    private void handleReject(FlowNodeInstance nodeInstance, String comments) {
        // 1. 更新节点实例状态
        nodeInstance.setStatus(FlowNodeInstance.STATUS_REJECTED);
        nodeInstance.setComments(comments);
        nodeInstance.setHandledAt(LocalDateTime.now());
        nodeInstance.setUpdatedAt(LocalDateTime.now());
        flowNodeInstanceMapper.updateById(nodeInstance);
        
        // 2. 创建已办任务
        FlowInstance flowInstance = flowInstanceMapper.selectById(nodeInstance.getFlowInstanceId());
        Document document = documentService.getDocument(flowInstance.getDocumentId());
        taskService.createDoneTask(nodeInstance, DoneTask.ACTION_REJECT, comments, flowInstance, document);
        
        // 3. 更新待办任务状态
        taskService.markAsHandled(nodeInstance.getId(), nodeInstance.getApproverId());
        
        // 4. 终止流程或退回
        FlowNode nodeDef = flowNodeMapper.selectById(nodeInstance.getNodeId());
        if (nodeDef.getRequired() != null && nodeDef.getRequired() == 1) {
            // 必须节点被拒绝，流程终止
            terminateFlow(nodeInstance.getFlowInstanceId(), "节点被拒绝: " + comments);
        } else {
            // 非必须节点被拒绝，可以跳过或退回
            // 根据业务规则处理
            log.warn("非必须节点被拒绝，节点实例ID: {}, 意见: {}", nodeInstance.getId(), comments);
        }
    }
    
    /**
     * 处理转发操作
     */
    private void handleForward(FlowNodeInstance nodeInstance, String comments) {
        // TODO: 实现转发逻辑
        log.warn("转发功能暂未实现，节点实例ID: {}", nodeInstance.getId());
    }
    
    /**
     * 处理退回操作
     */
    private void handleReturn(FlowNodeInstance nodeInstance, String comments) {
        // TODO: 实现退回逻辑
        log.warn("退回功能暂未实现，节点实例ID: {}", nodeInstance.getId());
    }
    
    /**
     * 终止流程
     */
    private void terminateFlow(Long flowInstanceId, String reason) {
        log.info("终止流程，流程实例ID: {}, 原因: {}", flowInstanceId, reason);
        
        FlowInstance flowInstance = flowInstanceMapper.selectById(flowInstanceId);
        flowInstance.setStatus(FlowInstance.STATUS_TERMINATED);
        flowInstance.setEndTime(LocalDateTime.now());
        flowInstance.setUpdatedAt(LocalDateTime.now());
        flowInstanceMapper.updateById(flowInstance);
    }
    
    /**
     * 验证审批权限
     */
    private FlowNodeInstance validateApprovalPermission(Long nodeInstanceId, Long approverId) {
        FlowNodeInstance nodeInstance = flowNodeInstanceMapper.selectById(nodeInstanceId);
        if (nodeInstance == null) {
            throw new BusinessException("节点实例不存在");
        }
        
        if (!nodeInstance.getApproverId().equals(approverId)) {
            throw new BusinessException("无权审批此节点");
        }
        
        if (nodeInstance.getStatus() != FlowNodeInstance.STATUS_PENDING) {
            throw new BusinessException("节点状态不正确，当前状态: " + nodeInstance.getStatus());
        }
        
        return nodeInstance;
    }
    
    /**
     * 确定流程类型
     */
    private Integer determineFlowType(Long documentId) {
        // TODO: 根据公文类型或其他条件确定流程类型
        // 暂时返回发文流程
        return FlowInstance.TYPE_ISSUANCE;
    }
    
    // ========== 辅助方法（需要集成用户服务） ==========
    
    /**
     * 解析用户ID列表
     */
    private List<Long> parseUserIds(String approverValue) {
        if (!StringUtils.hasText(approverValue)) {
            return new ArrayList<>();
        }
        return Arrays.stream(approverValue.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .map(Long::parseLong)
            .collect(Collectors.toList());
    }
    
    /**
     * 根据角色获取用户ID列表（TODO: 集成用户服务）
     */
    private List<Long> getUserIdsByRole(String roleCode) {
        // TODO: 调用用户服务获取角色下的用户
        log.warn("getUserIdsByRole 未实现，角色: {}", roleCode);
        return new ArrayList<>();
    }
    
    /**
     * 获取部门负责人ID列表（TODO: 集成用户服务）
     */
    private List<Long> getDeptLeaderIds(String deptIds, Document document) {
        // TODO: 调用用户服务获取部门负责人
        log.warn("getDeptLeaderIds 未实现，部门ID: {}", deptIds);
        // 如果公文有部门ID，可以返回该部门的负责人
        if (document.getDeptId() != null) {
            // TODO: 查询部门负责人
        }
        return new ArrayList<>();
    }
    
    /**
     * 从流程实例中获取发起人指定的审批人（TODO: 需要存储发起人指定的审批人）
     */
    private List<Long> getApproversFromFlowInstance(FlowInstance flowInstance) {
        // TODO: 从流程实例的扩展信息中获取发起人指定的审批人
        log.warn("getApproversFromFlowInstance 未实现，流程实例ID: {}", flowInstance.getId());
        return new ArrayList<>();
    }
}

