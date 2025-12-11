package xtt.cloud.oa.document.application.flow.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.document.domain.entity.flow.task.DoneTask;

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

    public FlowApprovalService(FlowEngineService flowEngineService) {
        this.flowEngineService = flowEngineService;
    }

    /**
     * 审批同意
     */
    @Transactional
    public void approve(Long nodeInstanceId, String comments, Long approverId) {
        log.info("审批同意，节点实例ID: {}, 审批人ID: {}", nodeInstanceId, approverId);
        flowEngineService.processNodeApproval(nodeInstanceId, DoneTask.ACTION_APPROVE, comments, approverId);
    }
    
    /**
     * 审批拒绝
     */
    @Transactional
    public void reject(Long nodeInstanceId, String comments, Long approverId) {
        log.info("审批拒绝，节点实例ID: {}, 审批人ID: {}", nodeInstanceId, approverId);
        flowEngineService.processNodeApproval(nodeInstanceId, DoneTask.ACTION_REJECT, comments, approverId);
    }
    
    /**
     * 审批转发
     */
    @Transactional
    public void forward(Long nodeInstanceId, String comments, Long approverId) {
        log.info("审批转发，节点实例ID: {}, 审批人ID: {}", nodeInstanceId, approverId);
        flowEngineService.processNodeApproval(nodeInstanceId, DoneTask.ACTION_FORWARD, comments, approverId);
    }
    
    /**
     * 审批退回
     */
    @Transactional
    public void returnBack(Long nodeInstanceId, String comments, Long approverId) {
        log.info("审批退回，节点实例ID: {}, 审批人ID: {}", nodeInstanceId, approverId);
        flowEngineService.processNodeApproval(nodeInstanceId, DoneTask.ACTION_RETURN, comments, approverId);
    }
}

