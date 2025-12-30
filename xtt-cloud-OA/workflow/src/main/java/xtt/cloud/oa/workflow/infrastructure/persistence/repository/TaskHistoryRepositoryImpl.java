package xtt.cloud.oa.workflow.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;
import xtt.cloud.oa.workflow.domain.flow.model.entity.history.TaskHistory;
import xtt.cloud.oa.workflow.domain.flow.repository.TaskHistoryRepository;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.TaskHistoryMapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.converter.TaskHistoryConverter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 任务历史仓储实现
 * 
 * @author xtt
 */
@Repository
public class TaskHistoryRepositoryImpl implements TaskHistoryRepository {
    
    private final TaskHistoryMapper mapper;
    
    public TaskHistoryRepositoryImpl(TaskHistoryMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public Optional<TaskHistory> findById(Long id) {
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TaskHistory po = mapper.selectById(id);
        return Optional.ofNullable(TaskHistoryConverter.toEntity(po));
    }
    
    @Override
    public List<TaskHistory> findByFlowInstanceId(Long flowInstanceId) {
        List<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TaskHistory> pos = mapper.selectList(
            new LambdaQueryWrapper<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TaskHistory>()
                .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TaskHistory::getFlowInstanceId, flowInstanceId)
                .orderByAsc(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TaskHistory::getStartTime)
        );
        return pos.stream()
                .map(TaskHistoryConverter::toEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<TaskHistory> findByNodeInstanceId(Long nodeInstanceId) {
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TaskHistory po = mapper.selectOne(
            new LambdaQueryWrapper<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TaskHistory>()
                .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TaskHistory::getNodeInstanceId, nodeInstanceId)
        );
        return Optional.ofNullable(TaskHistoryConverter.toEntity(po));
    }
    
    @Override
    public List<TaskHistory> findByHandlerId(Long handlerId) {
        List<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TaskHistory> pos = mapper.selectList(
            new LambdaQueryWrapper<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TaskHistory>()
                .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TaskHistory::getHandlerId, handlerId)
                .orderByDesc(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TaskHistory::getEndTime)
        );
        return pos.stream()
                .map(TaskHistoryConverter::toEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public TaskHistory save(TaskHistory history) {
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TaskHistory po = TaskHistoryConverter.toPO(history);
        if (po.getId() == null) {
            mapper.insert(po);
        } else {
            mapper.updateById(po);
        }
        // 更新实体ID
        history.setId(po.getId());
        return history;
    }
    
    @Override
    public void update(TaskHistory history) {
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TaskHistory po = TaskHistoryConverter.toPO(history);
        mapper.updateById(po);
    }
    
    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

