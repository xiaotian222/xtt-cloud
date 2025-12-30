package xtt.cloud.oa.workflow.domain.flow.service.state.impl;

import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowStatus;
import xtt.cloud.oa.workflow.domain.flow.service.state.FlowState;

/**
 * 已完成状态
 * 
 * @author xtt
 */
public class CompletedState implements FlowState {
    
    @Override
    public boolean canProceed() {
        return false;
    }
    
    @Override
    public FlowStatus getStatus() {
        return FlowStatus.COMPLETED;
    }
}

