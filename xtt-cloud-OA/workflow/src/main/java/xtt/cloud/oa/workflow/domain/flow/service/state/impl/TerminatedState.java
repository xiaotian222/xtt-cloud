package xtt.cloud.oa.workflow.domain.flow.service.state.impl;

import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowStatus;
import xtt.cloud.oa.workflow.domain.flow.service.state.FlowState;

/**
 * 已终止状态
 * 
 * @author xtt
 */
public class TerminatedState implements FlowState {
    
    @Override
    public boolean canProceed() {
        return false;
    }
    
    @Override
    public FlowStatus getStatus() {
        return FlowStatus.TERMINATED;
    }
}

