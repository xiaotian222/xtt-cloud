package xtt.cloud.oa.workflow.infrastructure.persistence.repository.old;

import org.springframework.stereotype.Repository;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.ApproverScopeMapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.pojo.ApproverScope;

/**
 * 审批人范围 Repository
 * 封装 ApproverScopeMapper 的数据访问操作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Repository
public class ApproverScopeRepositoryImpl {
    
    private final ApproverScopeMapper mapper;

    public ApproverScopeRepositoryImpl(ApproverScopeMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 根据ID查询审批人范围
     */
    public ApproverScope findById(Long id) {
        return mapper.selectById(id);
    }
    
    /**
     * 根据动作ID查询审批人范围
     */
    public ApproverScope findByActionId(Long actionId) {
        return mapper.selectOne(null); // TODO: 添加查询条件
    }
    
    /**
     * 保存审批人范围
     */
    public void save(ApproverScope scope) {
        if (scope.getId() == null) {
            mapper.insert(scope);
        } else {
            mapper.updateById(scope);
        }
    }
    
    /**
     * 更新审批人范围
     */
    public void update(ApproverScope scope) {
        mapper.updateById(scope);
    }
    
    /**
     * 删除审批人范围
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

