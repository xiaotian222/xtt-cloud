package xtt.cloud.oa.workflow.domain.flow.model.valueobject;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 自由流转上下文值对象
 * 
 * 记录固定流中自由流转的状态信息
 * 
 * @author xtt
 */
public class FreeFlowContext {
    
    /**
     * 是否处于自由流转状态
     */
    private final boolean inFreeFlow;
    
    /**
     * 开启自由流转的节点实例ID
     */
    private final Long sourceNodeInstanceId;
    
    /**
     * 开启自由流转的时间
     */
    private final LocalDateTime startTime;
    
    /**
     * 结束自由流转的时间
     */
    private final LocalDateTime endTime;
    
    /**
     * 创建未开启自由流转的上下文
     */
    public static FreeFlowContext notInFreeFlow() {
        return new FreeFlowContext(false, null, null, null);
    }
    
    /**
     * 创建开启自由流转的上下文
     */
    public static FreeFlowContext inFreeFlow(Long sourceNodeInstanceId) {
        return new FreeFlowContext(true, sourceNodeInstanceId, LocalDateTime.now(), null);
    }
    
    /**
     * 结束自由流转
     */
    public FreeFlowContext endFreeFlow() {
        if (!inFreeFlow) {
            throw new IllegalStateException("当前不在自由流转状态");
        }
        return new FreeFlowContext(false, sourceNodeInstanceId, startTime, LocalDateTime.now());
    }
    
    private FreeFlowContext(boolean inFreeFlow, Long sourceNodeInstanceId, 
                           LocalDateTime startTime, LocalDateTime endTime) {
        this.inFreeFlow = inFreeFlow;
        this.sourceNodeInstanceId = sourceNodeInstanceId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public boolean isInFreeFlow() {
        return inFreeFlow;
    }
    
    public Long getSourceNodeInstanceId() {
        return sourceNodeInstanceId;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FreeFlowContext that = (FreeFlowContext) o;
        return inFreeFlow == that.inFreeFlow &&
                Objects.equals(sourceNodeInstanceId, that.sourceNodeInstanceId) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(inFreeFlow, sourceNodeInstanceId, startTime, endTime);
    }
    
    @Override
    public String toString() {
        return "FreeFlowContext{" +
                "inFreeFlow=" + inFreeFlow +
                ", sourceNodeInstanceId=" + sourceNodeInstanceId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}

