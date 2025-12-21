package xtt.cloud.oa.document.application.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.common.BusinessException;
import xtt.cloud.oa.document.application.flow.core.FlowApprovalService;
import xtt.cloud.oa.document.application.flow.core.FlowEngineService;
import xtt.cloud.oa.document.application.flow.core.free.FreeFlowEngineService;
import xtt.cloud.oa.document.application.flow.task.TaskService;
import xtt.cloud.oa.document.application.flow.core.DocumentService;
import xtt.cloud.oa.document.application.flow.history.FlowHistoryService;
import xtt.cloud.oa.document.domain.entity.flow.definition.Document;
import xtt.cloud.oa.document.domain.entity.flow.definition.FlowDefinition;
import xtt.cloud.oa.document.domain.entity.flow.FlowInstance;
import xtt.cloud.oa.document.domain.entity.flow.definition.FlowNode;
import xtt.cloud.oa.document.domain.entity.flow.FlowNodeInstance;
import xtt.cloud.oa.document.domain.entity.flow.Handling;
import xtt.cloud.oa.document.domain.entity.flow.history.ActivityHistory;
import xtt.cloud.oa.document.domain.entity.flow.history.FlowInstanceHistory;
import xtt.cloud.oa.document.domain.entity.flow.history.TaskHistory;
import xtt.cloud.oa.document.application.flow.repository.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程服务 - 流程引擎对外统一接口
 * 
 * <p>本服务是流程引擎的唯一对外接口，封装了所有流程相关的业务操作。</p>
 * 
 * <p>设计原则：</p>
 * <ul>
 *   <li>所有流程操作必须通过 FlowService 进行，不直接调用核心服务</li>
 *   <li>隐藏流程引擎的内部实现细节，提供简洁的业务接口</li>
 *   <li>统一异常处理和日志记录</li>
 *   <li>保证事务一致性</li>
 * </ul>
 * 
 * <p>核心功能：</p>
 * <ul>
 *   <li>流程启动：创建并启动流程实例</li>
 *   <li>审批处理：同意、拒绝、转发、退回等操作</li>
 *   <li>流程实例管理：查询和管理流程实例</li>
 *   <li>流程扩展功能：承办记录等</li>
 * </ul>
 * 
 * <p>内部服务：</p>
 * <ul>
 *   <li>FlowEngineService：流程引擎核心，负责节点流转</li>
 *   <li>FlowApprovalService：审批处理服务</li>
 * </ul>
 * 
 * <p>注意：待办已办管理请使用 {@link TaskService}</p>
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Service
public class FlowService {
    
    private static final Logger log = LoggerFactory.getLogger(FlowService.class);
    
    // 流程引擎核心服务
    private final FlowEngineService flowEngineService;
    private final FlowApprovalService flowApprovalService;
    private final FreeFlowEngineService freeFlowEngineService;
    private final TaskService taskService;
    private final DocumentService documentService;
    private final FlowHistoryService flowHistoryService;
    
    // 流程实例和扩展信息 Repository
    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowNodeRepository flowNodeRepository;
    private final FlowNodeInstanceRepository flowNodeInstanceRepository;
    private final FlowDefinitionRepository flowDefinitionRepository;

    public FlowService(
            FlowEngineService flowEngineService,
            FlowApprovalService flowApprovalService,
            FreeFlowEngineService freeFlowEngineService,
            TaskService taskService,
            DocumentService documentService,
            FlowHistoryService flowHistoryService,
            FlowInstanceRepository flowInstanceRepository,
            FlowNodeRepository flowNodeRepository,
            FlowNodeInstanceRepository flowNodeInstanceRepository,
            FlowDefinitionRepository flowDefinitionRepository,
            HandlingRepository handlingRepository) {
        this.flowEngineService = flowEngineService;
        this.flowApprovalService = flowApprovalService;
        this.freeFlowEngineService = freeFlowEngineService;
        this.taskService = taskService;
        this.documentService = documentService;
        this.flowHistoryService = flowHistoryService;
        this.flowInstanceRepository = flowInstanceRepository;
        this.flowNodeRepository = flowNodeRepository;
        this.flowNodeInstanceRepository = flowNodeInstanceRepository;
        this.flowDefinitionRepository = flowDefinitionRepository;
    }
    
    // ========== 流程引擎核心接口 ==========
    /**
     * 创建流程实例（仅创建，不启动）
     *
     * @param flowInstance 流程实例
     * @return 流程实例
     */
    @Transactional
    public FlowInstance createFlowInstance(FlowInstance flowInstance) {
        log.info("创建流程实例，公文ID: {}, 流程类型: {}, 流程定义ID: {}",
                flowInstance.getDocumentId(), flowInstance.getFlowType(), flowInstance.getFlowDefId());
        try {
            flowInstance.setCreatedAt(LocalDateTime.now());
            flowInstance.setUpdatedAt(LocalDateTime.now());
            flowInstanceRepository.save(flowInstance);
            log.info("创建流程实例成功，ID: {}", flowInstance.getId());
            return flowInstance;
        } catch (Exception e) {
            log.error("创建流程实例失败，公文ID: {}", flowInstance.getDocumentId(), e);
            throw new BusinessException("创建流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 启动流程
     * 
     * @param documentId 公文ID
     * @param flowDefId 流程定义ID
     * @return 流程实例
     */
    @Transactional
    public FlowInstance startFlow(Long documentId, Long flowDefId) {
        log.info("启动流程，公文ID: {}, 流程定义ID: {}", documentId, flowDefId);
        try {
            Document document = documentService.getDocument(documentId);
            if (document == null) {
                throw new BusinessException("公文不存在");
            }
            
            FlowDefinition flowDef = flowDefinitionRepository.findById(flowDefId);
            if (flowDef == null) {
                throw new BusinessException("流程定义不存在");
            }
            
            return flowEngineService.startFlow(document, flowDef);
        } catch (BusinessException e) {
            log.error("启动流程失败，公文ID: {}, 流程定义ID: {}", documentId, flowDefId, e);
            throw e;
        } catch (Exception e) {
            log.error("启动流程异常，公文ID: {}, 流程定义ID: {}", documentId, flowDefId, e);
            throw new BusinessException("启动流程失败: " + e.getMessage());
        }
    }
    
    /**
     * 审批同意
     * 
     * @param nodeInstanceId 节点实例ID
     * @param comments 审批意见
     * @param approverId 审批人ID
     */
    @Transactional
    public void approve(Long nodeInstanceId, String comments, Long approverId) {
        log.info("审批同意，节点实例ID: {}, 审批人ID: {}", nodeInstanceId, approverId);
        
        FlowNodeInstance nodeInstance = flowNodeInstanceRepository.findById(nodeInstanceId);
        if (nodeInstance == null) {
            throw new BusinessException("节点实例不存在");
        }
        
        FlowInstance flowInstance = flowInstanceRepository.findById(nodeInstance.getFlowInstanceId());
        if (flowInstance == null) {
            throw new BusinessException("流程实例不存在");
        }
        
        Document document = documentService.getDocument(flowInstance.getDocumentId());
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        
        FlowNode nodeDef = flowNodeRepository.findById(nodeInstance.getNodeId());
        if (nodeDef == null) {
            throw new BusinessException("节点定义不存在");
        }
        
        flowApprovalService.approve(nodeInstance, flowInstance, document, nodeDef, comments, approverId);
    }
    
    /**
     * 审批拒绝
     * 
     * @param nodeInstanceId 节点实例ID
     * @param comments 审批意见
     * @param approverId 审批人ID
     */
    @Transactional
    public void reject(Long nodeInstanceId, String comments, Long approverId) {
        log.info("审批拒绝，节点实例ID: {}, 审批人ID: {}", nodeInstanceId, approverId);
        
        // 在 FlowService 中组装对象
        FlowNodeInstance nodeInstance = flowNodeInstanceRepository.findById(nodeInstanceId);
        if (nodeInstance == null) {
            throw new BusinessException("节点实例不存在");
        }
        
        FlowInstance flowInstance = flowInstanceRepository.findById(nodeInstance.getFlowInstanceId());
        if (flowInstance == null) {
            throw new BusinessException("流程实例不存在");
        }
        
        Document document = documentService.getDocument(flowInstance.getDocumentId());
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        
        FlowNode nodeDef = flowNodeRepository.findById(nodeInstance.getNodeId());
        if (nodeDef == null) {
            throw new BusinessException("节点定义不存在");
        }
        
        flowApprovalService.reject(nodeInstance, flowInstance, document, nodeDef, comments, approverId);
    }
    
    /**
     * 审批转发
     * 
     * @param nodeInstanceId 节点实例ID
     * @param comments 审批意见
     * @param approverId 审批人ID
     */
    @Transactional
    public void forward(Long nodeInstanceId, String comments, Long approverId) {
        log.info("审批转发，节点实例ID: {}, 审批人ID: {}", nodeInstanceId, approverId);
        
        // 在 FlowService 中组装对象
        FlowNodeInstance nodeInstance = flowNodeInstanceRepository.findById(nodeInstanceId);
        if (nodeInstance == null) {
            throw new BusinessException("节点实例不存在");
        }
        
        FlowInstance flowInstance = flowInstanceRepository.findById(nodeInstance.getFlowInstanceId());
        if (flowInstance == null) {
            throw new BusinessException("流程实例不存在");
        }
        
        Document document = documentService.getDocument(flowInstance.getDocumentId());
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        
        FlowNode nodeDef = flowNodeRepository.findById(nodeInstance.getNodeId());
        if (nodeDef == null) {
            throw new BusinessException("节点定义不存在");
        }
        
        flowApprovalService.forward(nodeInstance, flowInstance, document, nodeDef, comments, approverId);
    }
    
    /**
     * 审批退回
     * 
     * @param nodeInstanceId 节点实例ID
     * @param comments 审批意见
     * @param approverId 审批人ID
     */
    @Transactional
    public void returnBack(Long nodeInstanceId, String comments, Long approverId) {
        log.info("审批退回，节点实例ID: {}, 审批人ID: {}", nodeInstanceId, approverId);
        
        // 在 FlowService 中组装对象
        FlowNodeInstance nodeInstance = flowNodeInstanceRepository.findById(nodeInstanceId);
        if (nodeInstance == null) {
            throw new BusinessException("节点实例不存在");
        }
        
        FlowInstance flowInstance = flowInstanceRepository.findById(nodeInstance.getFlowInstanceId());
        if (flowInstance == null) {
            throw new BusinessException("流程实例不存在");
        }
        
        Document document = documentService.getDocument(flowInstance.getDocumentId());
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        
        FlowNode nodeDef = flowNodeRepository.findById(nodeInstance.getNodeId());
        if (nodeDef == null) {
            throw new BusinessException("节点定义不存在");
        }
        
        flowApprovalService.returnBack(nodeInstance, flowInstance, document, nodeDef, comments, approverId);
    }


    // ========== todo 流程实例管理 ==========
    /**
     * 获取流程实例详情
     * 
     * @param id 流程实例ID
     * @return 流程实例
     */
    public FlowInstance getFlowInstance(Long id) {
        log.debug("获取流程实例详情，ID: {}", id);
        return flowInstanceRepository.findById(id);
    }
    
    /**
     * 根据公文ID获取流程实例
     * 
     * @param documentId 公文ID
     * @return 流程实例
     */
    public FlowInstance getFlowInstanceByDocumentId(Long documentId) {
        log.debug("根据公文ID获取流程实例，公文ID: {}", documentId);
        // TODO: 在 Repository 中添加查询方法
        List<FlowInstance> instances = flowInstanceRepository.findByDocumentId(documentId);
        return instances.isEmpty() ? null : instances.get(0);
    }


    // ========== 固定流和自由流整合接口 ==========
    
    /**
     * 在固定流程节点中执行自由流转
     * 当节点 allowFreeFlow=1 时，可以使用此方法执行自由流转
     * 
     * @param nodeInstanceId 节点实例ID
     * @param actionId 自由流动作ID
     * @param selectedDeptIds 选择的部门ID列表
     * @param selectedUserIds 选择的人员ID列表
     * @param comment 备注
     * @param operatorId 操作人ID
     * @return 新创建的节点实例
     */
    @Transactional
    public FlowNodeInstance executeFreeFlowInFixedNode(
            Long nodeInstanceId,
            Long actionId,
            List<Long> selectedDeptIds,
            List<Long> selectedUserIds,
            String comment,
            Long operatorId) {
        log.info("在固定流程节点中执行自由流转，节点实例ID: {}, 动作ID: {}", nodeInstanceId, actionId);
        
        try {
            // 1. 验证节点实例
            FlowNodeInstance nodeInstance = flowNodeInstanceRepository.findById(nodeInstanceId);
            if (nodeInstance == null) {
                throw new BusinessException("节点实例不存在");
            }
            
            // 2. 验证节点是否允许自由流
            FlowNode nodeDef = flowNodeRepository.findById(nodeInstance.getNodeId());
            if (nodeDef.getAllowFreeFlow() == null || nodeDef.getAllowFreeFlow() != 1) {
                throw new BusinessException("该节点不允许自由流转");
            }
            
            // 3. 验证节点状态
            if (nodeInstance.getStatus() != FlowNodeInstance.STATUS_PENDING) {
                throw new BusinessException("节点状态不正确，无法执行自由流转");
            }
            
            // 4. 获取流程实例和文档信息
            FlowInstance flowInstance = flowInstanceRepository.findById(nodeInstance.getFlowInstanceId());
            if (flowInstance == null) {
                throw new BusinessException("流程实例不存在");
            }
            
            Document document = documentService.getDocument(flowInstance.getDocumentId());
            if (document == null) {
                throw new BusinessException("文档不存在");
            }
            
            // 5. 调用自由流引擎执行动作（传递对象）
            FlowNodeInstance newNodeInstance = freeFlowEngineService.executeAction(
                nodeInstance.getId(),
                actionId,
                selectedDeptIds,
                selectedUserIds,
                comment,
                operatorId
            );
            
            // 6. 更新当前节点实例状态
            nodeInstance.setStatus(FlowNodeInstance.STATUS_COMPLETED);
            nodeInstance.setComments("已执行自由流转: " + comment);
            nodeInstance.setHandledAt(LocalDateTime.now());
            nodeInstance.setUpdatedAt(LocalDateTime.now());
            flowNodeInstanceRepository.update(nodeInstance);
            
            // 7. 创建已办任务
            taskService.createDoneTask(nodeInstance, "自由流转", comment, flowInstance, document);
            taskService.markAsHandled(nodeInstance.getId(), operatorId);
            
            log.info("在固定流程节点中执行自由流转成功，新节点实例ID: {}", newNodeInstance.getId());
            return newNodeInstance;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("在固定流程节点中执行自由流转失败，节点实例ID: {}, 动作ID: {}", 
                    nodeInstanceId, actionId, e);
            throw new BusinessException("执行自由流转失败: " + e.getMessage());
        }
    }
    
    /**
     * 在自由流中启动固定子流程
     * 当需要在自由流中执行一个固定的审批流程时，可以使用此方法
     * 
     * @param parentFlowInstanceId 父流程实例ID（当前自由流实例）
     * @param documentId 公文ID
     * @param flowDefId 要启动的固定流程定义ID
     * @return 子流程实例
     */
    @Transactional
    public FlowInstance startFixedSubFlowInFreeFlow(
            Long parentFlowInstanceId,
            Long documentId,
            Long flowDefId) {
        log.info("在自由流中启动固定子流程，父流程实例ID: {}, 公文ID: {}, 流程定义ID: {}", 
                parentFlowInstanceId, documentId, flowDefId);
        
        try {
            // 1. 验证父流程实例
            FlowInstance parentFlowInstance = flowInstanceRepository.findById(parentFlowInstanceId);
            if (parentFlowInstance == null) {
                throw new BusinessException("父流程实例不存在");
            }
            
            // 2. 验证父流程是否为自由流或混合流
            if (parentFlowInstance.getFlowMode() == null || 
                (parentFlowInstance.getFlowMode() != FlowInstance.FLOW_MODE_FREE && 
                 parentFlowInstance.getFlowMode() != FlowInstance.FLOW_MODE_MIXED)) {
                throw new BusinessException("只能在自由流或混合流中启动固定子流程");
            }
            
            // 3. 组装对象并启动固定子流程
            Document document = documentService.getDocument(documentId);
            if (document == null) {
                throw new BusinessException("文档不存在");
            }
            
            FlowDefinition flowDef = flowDefinitionRepository.findById(flowDefId);
            if (flowDef == null) {
                throw new BusinessException("流程定义不存在");
            }
            
            FlowInstance subFlowInstance = flowEngineService.startFlow(document, flowDef);
            
            // 4. 设置父子关系
            subFlowInstance.setParentFlowInstanceId(parentFlowInstanceId);
            subFlowInstance.setFlowMode(FlowInstance.FLOW_MODE_FIXED);
            flowInstanceRepository.update(subFlowInstance);
            
            log.info("在自由流中启动固定子流程成功，子流程实例ID: {}", subFlowInstance.getId());
            return subFlowInstance;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("在自由流中启动固定子流程失败，父流程实例ID: {}, 流程定义ID: {}", 
                    parentFlowInstanceId, flowDefId, e);
            throw new BusinessException("启动固定子流程失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查子流程是否完成并继续父流程
     * 当固定子流程完成时，需要调用此方法通知父流程继续
     * 
     * @param subFlowInstanceId 子流程实例ID
     */
    @Transactional
    public void checkAndContinueParentFlow(Long subFlowInstanceId) {
        log.info("检查子流程是否完成并继续父流程，子流程实例ID: {}", subFlowInstanceId);
        
        try {
            // 1. 获取子流程实例
            FlowInstance subFlowInstance = flowInstanceRepository.findById(subFlowInstanceId);
            if (subFlowInstance == null) {
                throw new BusinessException("子流程实例不存在");
            }
            
            // 2. 检查子流程是否完成
            if (subFlowInstance.getStatus() != FlowInstance.STATUS_COMPLETED) {
                log.debug("子流程尚未完成，流程实例ID: {}", subFlowInstanceId);
                return;
            }
            
            // 3. 获取父流程实例
            if (subFlowInstance.getParentFlowInstanceId() == null) {
                log.debug("子流程没有父流程，流程实例ID: {}", subFlowInstanceId);
                return;
            }
            
            FlowInstance parentFlowInstance = flowInstanceRepository.findById(subFlowInstance.getParentFlowInstanceId());
            if (parentFlowInstance == null) {
                log.warn("父流程实例不存在，子流程实例ID: {}, 父流程实例ID: {}", 
                        subFlowInstanceId, subFlowInstance.getParentFlowInstanceId());
                return;
            }
            
            // 4. 在父流程中创建节点实例，表示子流程已完成
            // 这里可以根据业务需求，创建相应的节点实例或更新父流程状态
            log.info("子流程已完成，父流程实例ID: {}, 子流程实例ID: {}", 
                    parentFlowInstance.getId(), subFlowInstanceId);
            
            // TODO: 根据业务需求，可以在这里创建父流程的节点实例，表示子流程已完成
            // 或者更新父流程的当前节点，继续流转
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("检查子流程并继续父流程失败，子流程实例ID: {}", subFlowInstanceId, e);
            throw new BusinessException("检查子流程并继续父流程失败: " + e.getMessage());
        }
    }



    // ========== 历史记录查询 ==========
    
    /**
     * 获取流程实例历史
     * 
     * @param flowInstanceId 流程实例ID
     * @return 流程实例历史记录
     */
    public FlowInstanceHistory getFlowInstanceHistory(Long flowInstanceId) {
        log.debug("查询流程实例历史，流程实例ID: {}", flowInstanceId);
        return flowHistoryService.getFlowInstanceHistory(flowInstanceId);
    }
    
    /**
     * 获取任务历史列表
     * 
     * @param flowInstanceId 流程实例ID
     * @return 任务历史记录列表
     */
    public List<TaskHistory> getTaskHistoryList(Long flowInstanceId) {
        log.debug("查询任务历史列表，流程实例ID: {}", flowInstanceId);
        return flowHistoryService.getTaskHistoryList(flowInstanceId);
    }
    
    /**
     * 获取活动历史列表
     * 
     * @param flowInstanceId 流程实例ID
     * @return 活动历史记录列表
     */
    public List<ActivityHistory> getActivityHistoryList(Long flowInstanceId) {
        log.debug("查询活动历史列表，流程实例ID: {}", flowInstanceId);
        return flowHistoryService.getActivityHistoryList(flowInstanceId);
    }
    
    /**
     * 根据处理人查询任务历史
     * 
     * @param handlerId 处理人ID
     * @return 任务历史记录列表
     */
    public List<TaskHistory> getTaskHistoryByHandler(Long handlerId) {
        log.debug("根据处理人查询任务历史，处理人ID: {}", handlerId);
        return flowHistoryService.getTaskHistoryByHandler(handlerId);
    }

}
