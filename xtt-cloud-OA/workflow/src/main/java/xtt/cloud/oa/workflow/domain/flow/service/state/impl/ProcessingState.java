package xtt.cloud.oa.workflow.domain.flow.service.state.impl;

import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowStatus;
import xtt.cloud.oa.workflow.domain.flow.event.FlowCompletedEvent;
import xtt.cloud.oa.workflow.domain.flow.event.FlowTerminatedEvent;
import xtt.cloud.oa.workflow.domain.flow.service.state.FlowState;

import java.time.LocalDateTime;

/**
 * 进行中状态
 * 
 * @author xtt
 */
public class ProcessingState implements FlowState {
    
    @Override
    public void complete(FlowInstance context) {
        // 转换到已完成状态
        context.transitionTo(new CompletedState());
        context.setEndTime(LocalDateTime.now());
        
        // 发布领域事件
        context.addDomainEvent(new FlowCompletedEvent(
                context.getId() != null ? context.getId().getValue() : null,
                context.getDocumentId(),
                context.getStartTime(),
                context.getEndTime()));
    }
    
    @Override
    public void terminate(FlowInstance context) {
        // 转换到已终止状态
        context.transitionTo(new TerminatedState());
        context.setEndTime(LocalDateTime.now());
        
        // 发布领域事件
        context.addDomainEvent(new FlowTerminatedEvent(
                context.getId() != null ? context.getId().getValue() : null,
                context.getDocumentId(),
                FlowStatus.TERMINATED.getValue()));
    }
    
    @Override
    public void suspend(FlowInstance context) {
        // 转换到已暂停状态
        context.transitionTo(new SuspendedState());
    }
    
    @Override
    public void cancel(FlowInstance context) {
        // 转换到已取消状态
        context.transitionTo(new CancelledState());
        context.setEndTime(LocalDateTime.now());
    }
    
    @Override
    public boolean canProceed() {
        return true;
    }
    
    @Override
    public FlowStatus getStatus() {
        return FlowStatus.PROCESSING;
    }
}

