package xtt.cloud.oa.workflow.domain.flow.service.state.impl;

import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowStatus;
import xtt.cloud.oa.workflow.domain.flow.service.state.FlowState;

/**
 * 已暂停状态
 * 
 * @author xtt
 */
public class SuspendedState implements FlowState {
    
    @Override
    public void resume(FlowInstance context) {
        // 转换到进行中状态
        context.transitionTo(new ProcessingState());
    }
    
    @Override
    public boolean canProceed() {
        return false; // 暂停状态不能继续执行
    }
    
    @Override
    public FlowStatus getStatus() {
        return FlowStatus.SUSPENDED;
    }
}

