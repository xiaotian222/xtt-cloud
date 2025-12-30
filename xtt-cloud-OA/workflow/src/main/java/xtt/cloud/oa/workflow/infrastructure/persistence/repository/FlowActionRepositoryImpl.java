package xtt.cloud.oa.workflow.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowAction;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowActionRepository;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.FlowActionMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 流程动作仓储实现
 * 
 * @author xtt
 */
@Repository
public class FlowActionRepositoryImpl implements FlowActionRepository {
    
    private static final Logger log = LoggerFactory.getLogger(FlowActionRepositoryImpl.class);
    
    private final FlowActionMapper flowActionMapper;
    
    public FlowActionRepositoryImpl(FlowActionMapper flowActionMapper) {
        this.flowActionMapper = flowActionMapper;
    }
    
    @Override
    public Optional<FlowAction> findById(Long actionId) {
        if (actionId == null || actionId <= 0) {
            return Optional.empty();
        }
        
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowAction po = 
                flowActionMapper.selectById(actionId);
        if (po == null) {
            return Optional.empty();
        }
        
        return Optional.of(new FlowAction(po));
    }
    
    @Override
    public List<FlowAction> findAllEnabled() {
        List<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowAction> pos = 
                flowActionMapper.selectList(
                        new LambdaQueryWrapper<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowAction>()
                                .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowAction::getEnabled, 1)
                                .orderByAsc(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowAction::getActionType)
                );
        
        return pos.stream()
                .map(FlowAction::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<FlowAction> findByIds(List<Long> actionIds) {
        if (actionIds == null || actionIds.isEmpty()) {
            return List.of();
        }
        
        List<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowAction> pos = 
                flowActionMapper.selectList(
                        new LambdaQueryWrapper<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowAction>()
                                .in(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowAction::getId, actionIds)
                                .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowAction::getEnabled, 1)
                );
        
        return pos.stream()
                .map(FlowAction::new)
                .collect(Collectors.toList());
    }
}

