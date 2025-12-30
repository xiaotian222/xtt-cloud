package xtt.cloud.oa.workflow.domain.flow.service.strategy;

import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;

import java.util.List;
import java.util.Map;

/**
 * 审批人分配策略接口
 * 
 * 策略模式：将不同的审批人分配逻辑封装为独立策略
 * 
 * @author xtt
 */
public interface ApproverAssignmentStrategy {
    
    /**
     * 分配审批人
     * 
     * @param approverValue 审批人配置值
     * @param flowInstanceId 流程实例ID
     * @param processVariables 流程变量
     * @return 审批人列表
     */
    List<Approver> assign(String approverValue, Long flowInstanceId, 
                         Map<String, Object> processVariables);
    
    /**
     * 判断是否支持该审批人类型
     * 
     * @param approverType 审批人类型
     * @return 是否支持
     */
    boolean supports(Integer approverType);
    
    /**
     * 获取策略优先级（数字越小优先级越高）
     * 用于处理多个策略都支持同一类型的情况
     * 
     * @return 优先级
     */
    default int getPriority() {
        return 100;
    }
}

