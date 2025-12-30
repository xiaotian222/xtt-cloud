package xtt.cloud.oa.workflow.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowActionRule;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowActionRuleRepository;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.FlowActionRuleMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 动作规则仓储实现
 * 
 * @author xtt
 */
@Repository
public class FlowActionRuleRepositoryImpl implements FlowActionRuleRepository {
    
    private static final Logger log = LoggerFactory.getLogger(FlowActionRuleRepositoryImpl.class);
    
    private final FlowActionRuleMapper flowActionRuleMapper;
    
    public FlowActionRuleRepositoryImpl(FlowActionRuleMapper flowActionRuleMapper) {
        this.flowActionRuleMapper = flowActionRuleMapper;
    }
    
    @Override
    public List<FlowActionRule> findByActionId(Long actionId) {
        if (actionId == null || actionId <= 0) {
            return List.of();
        }
        
        List<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowActionRule> pos = 
                flowActionRuleMapper.selectList(
                        new LambdaQueryWrapper<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowActionRule>()
                                .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowActionRule::getActionId, actionId)
                );
        
        return pos.stream()
                .map(FlowActionRule::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<FlowActionRule> matchRules(Integer documentStatus, List<String> userRoles, Long deptId) {
        if (documentStatus == null) {
            return List.of();
        }
        
        LambdaQueryWrapper<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowActionRule> wrapper = 
                new LambdaQueryWrapper<>();
        wrapper.eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowActionRule::getDocumentStatus, documentStatus)
               .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowActionRule::getEnabled, 1);
        
        // 匹配用户角色（支持 * 表示所有角色，或逗号分隔的角色列表）
        if (userRoles != null && !userRoles.isEmpty()) {
            wrapper.and(w -> {
                // 匹配 "*" 或包含用户角色的规则
                w.like(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowActionRule::getUserRole, "*").or();
                for (String role : userRoles) {
                    w.like(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowActionRule::getUserRole, role).or();
                }
            });
        }
        
        // 匹配部门（如果规则没有指定部门，则匹配所有部门）
        if (deptId != null) {
            wrapper.and(w -> 
                w.eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowActionRule::getDeptId, deptId)
                 .or()
                 .isNull(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowActionRule::getDeptId)
            );
        }
        
        // 按优先级降序排序
        wrapper.orderByDesc(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowActionRule::getPriority);
        
        List<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowActionRule> pos = 
                flowActionRuleMapper.selectList(wrapper);
        
        return pos.stream()
                .map(FlowActionRule::new)
                .collect(Collectors.toList());
    }
}

