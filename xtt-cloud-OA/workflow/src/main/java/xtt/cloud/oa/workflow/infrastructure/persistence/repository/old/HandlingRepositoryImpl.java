package xtt.cloud.oa.workflow.infrastructure.persistence.repository.old;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.HandlingMapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.pojo.Handling;

import java.util.List;

/**
 * 承办记录 Repository
 * 封装 HandlingMapper 的数据访问操作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Repository
public class HandlingRepositoryImpl {

    private final HandlingMapper mapper;

    public HandlingRepositoryImpl(HandlingMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 根据ID查询承办记录
     */
    public Handling findById(Long id) {
        return mapper.selectById(id);
    }
    
    /**
     * 根据流程实例ID查询承办记录列表
     */
    public List<Handling> findByFlowInstanceId(Long flowInstanceId) {
        return mapper.selectList(new LambdaQueryWrapper<Handling>()
                .eq(Handling::getFlowInstanceId, flowInstanceId)
                .orderByDesc(Handling::getCreatedAt));
    }
    
    /**
     * 保存承办记录
     */
    public void save(Handling handling) {
        if (handling.getId() == null) {
            mapper.insert(handling);
        } else {
            mapper.updateById(handling);
        }
    }
    
    /**
     * 更新承办记录
     */
    public void update(Handling handling) {
        mapper.updateById(handling);
    }
    
    /**
     * 删除承办记录
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

