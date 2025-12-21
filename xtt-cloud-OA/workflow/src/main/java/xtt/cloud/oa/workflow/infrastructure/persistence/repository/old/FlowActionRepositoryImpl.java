package xtt.cloud.oa.workflow.infrastructure.persistence.repository.old;

import org.springframework.stereotype.Repository;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.FlowActionMapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowAction;

import java.util.List;

/**
 * 流程动作 Repository
 * 封装 FlowActionMapper 的数据访问操作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Repository
public class FlowActionRepositoryImpl {
    
    private final FlowActionMapper mapper;

    public FlowActionRepositoryImpl(FlowActionMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 根据ID查询动作
     */
    public FlowAction findById(Long id) {
        return mapper.selectById(id);
    }
    
    /**
     * 查询所有启用的动作
     */
    public List<FlowAction> findAllEnabled() {
        return mapper.selectList(null); // TODO: 添加启用状态过滤
    }
    
    /**
     * 保存动作
     */
    public void save(FlowAction action) {
        if (action.getId() == null) {
            mapper.insert(action);
        } else {
            mapper.updateById(action);
        }
    }
    
    /**
     * 更新动作
     */
    public void update(FlowAction action) {
        mapper.updateById(action);
    }
    
    /**
     * 删除动作
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

