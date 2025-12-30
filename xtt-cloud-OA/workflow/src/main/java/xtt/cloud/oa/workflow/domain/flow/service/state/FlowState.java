package xtt.cloud.oa.workflow.domain.flow.service.state;

import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowStatus;

/**
 * 流程状态接口
 * 
 * 状态模式：封装流程状态转换逻辑
 * 
 * @author xtt
 */
public interface FlowState {
    
    /**
     * 启动流程
     */
    default void start(FlowInstance context) {
        throw new IllegalStateException("当前状态不允许启动: " + getStatus());
    }
    
    /**
     * 完成流程
     */
    default void complete(FlowInstance context) {
        throw new IllegalStateException("当前状态不允许完成: " + getStatus());
    }
    
    /**
     * 终止流程
     */
    default void terminate(FlowInstance context) {
        throw new IllegalStateException("当前状态不允许终止: " + getStatus());
    }
    
    /**
     * 暂停流程
     */
    default void suspend(FlowInstance context) {
        throw new IllegalStateException("当前状态不允许暂停: " + getStatus());
    }
    
    /**
     * 恢复流程
     */
    default void resume(FlowInstance context) {
        throw new IllegalStateException("当前状态不允许恢复: " + getStatus());
    }
    
    /**
     * 取消流程
     */
    default void cancel(FlowInstance context) {
        throw new IllegalStateException("当前状态不允许取消: " + getStatus());
    }
    
    /**
     * 判断是否可以继续执行
     */
    boolean canProceed();
    
    /**
     * 获取状态值对象
     */
    FlowStatus getStatus();
}

