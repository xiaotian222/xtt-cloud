package xtt.cloud.oa.document.application.flow.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;
import xtt.cloud.oa.document.domain.entity.flow.Handling;
import xtt.cloud.oa.document.domain.mapper.flow.HandlingMapper;

import java.util.List;

/**
 * 承办记录 Repository
 * 封装 HandlingMapper 的数据访问操作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Repository
public class HandlingRepository {
    
    private final HandlingMapper mapper;
    
    public HandlingRepository(HandlingMapper mapper) {
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

