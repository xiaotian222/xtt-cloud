package xtt.cloud.oa.workflow.infrastructure.persistence.repository.old;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.FlowActionRuleMapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowActionRule;

import java.util.List;

/**
 * 流程动作规则 Repository
 * 封装 FlowActionRuleMapper 的数据访问操作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Repository
public class FlowActionRuleRepositoryImpl {
    
    private final FlowActionRuleMapper mapper;

    public FlowActionRuleRepositoryImpl(FlowActionRuleMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 根据ID查询规则
     */
    public FlowActionRule findById(Long id) {
        return mapper.selectById(id);
    }
    
    /**
     * 根据动作ID查询规则列表
     */
    public List<FlowActionRule> findByActionId(Long actionId) {
        return mapper.selectList(
            new LambdaQueryWrapper<FlowActionRule>()
                .eq(FlowActionRule::getActionId, actionId)
        );
    }
    
    /**
     * 根据文档状态、用户角色、部门ID匹配规则
     */
    public List<FlowActionRule> matchRules(Integer documentStatus, List<String> userRoles, Long deptId) {
        LambdaQueryWrapper<FlowActionRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlowActionRule::getDocumentStatus, documentStatus)
               .eq(FlowActionRule::getEnabled, 1);
        
        // 角色匹配（支持多个角色，逗号分隔，*表示所有角色）
        if (userRoles != null && !userRoles.isEmpty()) {
            wrapper.and(w -> {
                // 检查是否有通配符规则
                w.like(FlowActionRule::getUserRole, "*").or();
                // 检查每个角色
                for (String role : userRoles) {
                    w.like(FlowActionRule::getUserRole, role).or();
                }
            });
        }
        
        // 部门匹配（可选）
        if (deptId != null) {
            wrapper.and(w -> w.eq(FlowActionRule::getDeptId, deptId).or().isNull(FlowActionRule::getDeptId));
        }
        
        wrapper.orderByDesc(FlowActionRule::getPriority);
        
        return mapper.selectList(wrapper);
    }
    
    /**
     * 保存规则
     */
    public void save(FlowActionRule rule) {
        if (rule.getId() == null) {
            mapper.insert(rule);
        } else {
            mapper.updateById(rule);
        }
    }
    
    /**
     * 更新规则
     */
    public void update(FlowActionRule rule) {
        mapper.updateById(rule);
    }
    
    /**
     * 删除规则
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

