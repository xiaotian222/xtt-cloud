package xtt.cloud.oa.workflow.application.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.workflow.application.flow.command.ApproveCommand;
import xtt.cloud.oa.workflow.application.flow.command.RejectCommand;

/**
 * 流程审批应用服务
 * 
 * 专门处理审批相关的业务逻辑
 * 
 * @author xtt
 */
@Service
public class FlowApprovalApplicationService {
    
    private static final Logger log = LoggerFactory.getLogger(FlowApprovalApplicationService.class);
    
    private final FlowApplicationService flowApplicationService;
    
    public FlowApprovalApplicationService(FlowApplicationService flowApplicationService) {
        this.flowApplicationService = flowApplicationService;
    }
    
    /**
     * 审批通过
     */
    @Transactional
    public void approve(ApproveCommand command) {
        log.info("审批通过，流程实例ID: {}, 节点实例ID: {}, 审批人ID: {}", 
                command.getFlowInstanceId(), command.getNodeInstanceId(), command.getApproverId());
        
        flowApplicationService.approve(command);
    }
    
    /**
     * 审批拒绝
     */
    @Transactional
    public void reject(RejectCommand command) {
        log.info("审批拒绝，流程实例ID: {}, 节点实例ID: {}, 审批人ID: {}", 
                command.getFlowInstanceId(), command.getNodeInstanceId(), command.getApproverId());
        
        flowApplicationService.reject(command);
    }
}

