package xtt.cloud.oa.workflow.domain.flow.service.strategy.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;
import xtt.cloud.oa.workflow.domain.flow.service.ApproverProvider;
import xtt.cloud.oa.workflow.domain.flow.service.strategy.ApproverAssignmentStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户审批人分配策略
 * 
 * 处理 APPROVER_TYPE_USER 类型：根据用户ID列表分配审批人
 * 
 * @author xtt
 */
@Component
public class UserApproverStrategy implements ApproverAssignmentStrategy {
    
    private static final Logger log = LoggerFactory.getLogger(UserApproverStrategy.class);
    
    private final ApproverProvider approverProvider;
    
    public UserApproverStrategy(ApproverProvider approverProvider) {
        this.approverProvider = approverProvider;
    }
    
    @Override
    public List<Approver> assign(String approverValue, Long flowInstanceId, 
                                Map<String, Object> processVariables) {
        if (!StringUtils.hasText(approverValue)) {
            log.warn("审批人配置值为空");
            return new ArrayList<>();
        }
        
        try {
            // 解析用户ID列表（支持逗号分隔或JSON数组）
            List<Long> userIds = parseIdList(approverValue);
            
            if (userIds.isEmpty()) {
                log.warn("解析用户ID列表为空，配置值: {}", approverValue);
                return new ArrayList<>();
            }
            
            // 调用审批人提供者批量获取用户并转换为审批人
            List<Approver> approvers = approverProvider.convertToApprovers(userIds);
            
            log.debug("用户审批人分配完成，用户ID数量: {}, 审批人数量: {}", userIds.size(), approvers.size());
            return approvers;
        } catch (Exception e) {
            log.error("根据用户ID列表分配审批人失败，配置值: {}", approverValue, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public boolean supports(Integer approverType) {
        return FlowNode.APPROVER_TYPE_USER.equals(approverType);
    }
    
    @Override
    public int getPriority() {
        return 10; // 用户类型优先级较高
    }
    
    /**
     * 解析ID列表（支持逗号分隔或JSON数组）
     */
    private List<Long> parseIdList(String value) {
        if (!StringUtils.hasText(value)) {
            return new ArrayList<>();
        }
        
        try {
            // 尝试解析为JSON数组
            if (value.trim().startsWith("[")) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = 
                        new com.fasterxml.jackson.databind.ObjectMapper();
                return mapper.readValue(value, 
                    new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {});
            }
            
            // 解析为逗号分隔的字符串
            return Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("解析ID列表失败: {}", value, e);
            return new ArrayList<>();
        }
    }
}

