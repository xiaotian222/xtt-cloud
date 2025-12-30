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
 * 角色审批人分配策略
 * 
 * 处理 APPROVER_TYPE_ROLE 类型：根据角色ID列表分配审批人
 * 
 * @author xtt
 */
@Component
public class RoleApproverStrategy implements ApproverAssignmentStrategy {
    
    private static final Logger log = LoggerFactory.getLogger(RoleApproverStrategy.class);
    
    private final ApproverProvider approverProvider;
    
    public RoleApproverStrategy(ApproverProvider approverProvider) {
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
            // 解析角色ID列表
            List<Long> roleIds = parseIdList(approverValue);
            
            if (roleIds.isEmpty()) {
                log.warn("解析角色ID列表为空，配置值: {}", approverValue);
                return new ArrayList<>();
            }
            
            // 调用审批人提供者获取角色下的所有用户（去重）
            List<Approver> approvers = approverProvider.getUsersByRoleIds(roleIds);
            
            log.debug("角色审批人分配完成，角色ID数量: {}, 审批人数量: {}", roleIds.size(), approvers.size());
            return approvers;
        } catch (Exception e) {
            log.error("根据角色ID列表分配审批人失败，配置值: {}", approverValue, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public boolean supports(Integer approverType) {
        return FlowNode.APPROVER_TYPE_ROLE.equals(approverType);
    }
    
    @Override
    public int getPriority() {
        return 20;
    }
    
    /**
     * 解析ID列表
     */
    private List<Long> parseIdList(String value) {
        if (!StringUtils.hasText(value)) {
            return new ArrayList<>();
        }
        
        try {
            if (value.trim().startsWith("[")) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = 
                        new com.fasterxml.jackson.databind.ObjectMapper();
                return mapper.readValue(value, 
                    new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {});
            }
            
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

