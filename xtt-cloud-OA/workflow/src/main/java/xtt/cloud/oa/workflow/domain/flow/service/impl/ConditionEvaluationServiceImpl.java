package xtt.cloud.oa.workflow.domain.flow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xtt.cloud.oa.workflow.domain.flow.service.ConditionEvaluationService;

import java.util.Map;

/**
 * 条件评估服务实现
 * 
 * 使用 Spring Expression Language (SpEL) 评估条件表达式
 * 
 * @author xtt
 */
@Service
public class ConditionEvaluationServiceImpl implements ConditionEvaluationService {
    
    private static final Logger log = LoggerFactory.getLogger(ConditionEvaluationServiceImpl.class);
    
    private final ExpressionParser parser = new SpelExpressionParser();
    
    @Override
    public boolean evaluate(String conditionExpression, Map<String, Object> processVariables) {
        if (!StringUtils.hasText(conditionExpression)) {
            log.warn("条件表达式为空，返回 false");
            return false;
        }
        
        try {
            // 解析表达式
            Expression expression = parser.parseExpression(conditionExpression);
            
            // 创建评估上下文，将流程变量放入上下文
            EvaluationContext context = new StandardEvaluationContext();
            if (processVariables != null) {
                for (Map.Entry<String, Object> entry : processVariables.entrySet()) {
                    context.setVariable(entry.getKey(), entry.getValue());
                }
            }
            
            // 评估表达式
            Object result = expression.getValue(context);
            
            // 转换为布尔值
            if (result instanceof Boolean) {
                return (Boolean) result;
            } else if (result instanceof Number) {
                return ((Number) result).doubleValue() != 0;
            } else if (result instanceof String) {
                return Boolean.parseBoolean((String) result);
            } else {
                log.warn("条件表达式返回值类型不支持: {}, 表达式: {}", 
                        result != null ? result.getClass().getName() : "null", 
                        conditionExpression);
                return false;
            }
        } catch (Exception e) {
            log.error("评估条件表达式失败，表达式: {}, 错误: {}", conditionExpression, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean isValid(String conditionExpression) {
        if (!StringUtils.hasText(conditionExpression)) {
            return false;
        }
        
        try {
            // 尝试解析表达式，如果解析成功则认为有效
            parser.parseExpression(conditionExpression);
            return true;
        } catch (Exception e) {
            log.debug("条件表达式无效: {}, 错误: {}", conditionExpression, e.getMessage());
            return false;
        }
    }
}

