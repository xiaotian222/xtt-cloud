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
 * 发起人指定审批人分配策略
 * 
 * 处理 APPROVER_TYPE_INITIATOR 类型：从流程变量中获取发起人指定的审批人
 * 
 * @author xtt
 */
@Component
public class InitiatorApproverStrategy implements ApproverAssignmentStrategy {
    
    private static final Logger log = LoggerFactory.getLogger(InitiatorApproverStrategy.class);
    
    private final ApproverProvider approverProvider;
    
    public InitiatorApproverStrategy(ApproverProvider approverProvider) {
        this.approverProvider = approverProvider;
    }
    
    @Override
    public List<Approver> assign(String approverValue, Long flowInstanceId, 
                                Map<String, Object> processVariables) {
        try {
            // 从流程变量中获取发起人指定的审批人ID列表
            Object approverIdsObj = processVariables != null 
                    ? processVariables.get("approverIds") 
                    : null;
            
            if (approverIdsObj == null) {
                log.warn("流程变量中未找到审批人ID列表，流程实例ID: {}", flowInstanceId);
                return new ArrayList<>();
            }
            
            // 解析审批人ID列表
            String approverIdsStr = approverIdsObj.toString();
            List<Long> approverIds = parseIdList(approverIdsStr);
            
            if (approverIds.isEmpty()) {
                log.warn("解析审批人ID列表为空，流程实例ID: {}", flowInstanceId);
                return new ArrayList<>();
            }
            
            // 调用审批人提供者批量获取用户并转换为审批人
            List<Approver> approvers = approverProvider.convertToApprovers(approverIds);
            
            log.debug("发起人指定审批人分配完成，审批人ID数量: {}, 审批人数量: {}", 
                    approverIds.size(), approvers.size());
            return approvers;
        } catch (Exception e) {
            log.error("根据发起人指定分配审批人失败，流程实例ID: {}", flowInstanceId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public boolean supports(Integer approverType) {
        return FlowNode.APPROVER_TYPE_INITIATOR.equals(approverType);
    }
    
    @Override
    public int getPriority() {
        return 40;
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

