package xtt.cloud.oa.workflow.domain.flow.service;

/**
 * 条件评估领域服务
 * 
 * 负责评估流程中的条件表达式（如 SpEL 表达式）
 * 
 * @author xtt
 */
public interface ConditionEvaluationService {
    
    /**
     * 评估条件表达式
     * 
     * @param conditionExpression 条件表达式（如 SpEL 表达式）
     * @param processVariables 流程变量
     * @return 条件评估结果
     */
    boolean evaluate(String conditionExpression, java.util.Map<String, Object> processVariables);
    
    /**
     * 验证条件表达式是否有效
     * 
     * @param conditionExpression 条件表达式
     * @return 是否有效
     */
    boolean isValid(String conditionExpression);
}

