package xtt.cloud.oa.document.application.flow.repository;

import org.springframework.stereotype.Repository;
import xtt.cloud.oa.document.domain.entity.flow.FreeFlowNodeInstance;
import xtt.cloud.oa.document.domain.mapper.flow.FreeFlowNodeInstanceMapper;

/**
 * 自由流节点实例 Repository
 * 封装 FreeFlowNodeInstanceMapper 的数据访问操作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Repository
public class FreeFlowNodeInstanceRepository {
    
    private final FreeFlowNodeInstanceMapper mapper;
    
    public FreeFlowNodeInstanceRepository(FreeFlowNodeInstanceMapper mapper) {
        this.mapper = mapper;
    }
    
    /**
     * 根据ID查询自由流节点实例
     */
    public FreeFlowNodeInstance findById(Long id) {
        return mapper.selectById(id);
    }
    
    /**
     * 根据节点实例ID查询自由流节点实例
     */
    public FreeFlowNodeInstance findByNodeInstanceId(Long nodeInstanceId) {
        return mapper.selectOne(null); // TODO: 添加查询条件
    }
    
    /**
     * 保存自由流节点实例
     */
    public void save(FreeFlowNodeInstance instance) {
        if (instance.getId() == null) {
            mapper.insert(instance);
        } else {
            mapper.updateById(instance);
        }
    }
    
    /**
     * 更新自由流节点实例
     */
    public void update(FreeFlowNodeInstance instance) {
        mapper.updateById(instance);
    }
    
    /**
     * 删除自由流节点实例
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

