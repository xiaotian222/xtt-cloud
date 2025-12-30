package xtt.cloud.oa.workflow.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Repository;
import xtt.cloud.oa.workflow.domain.flow.model.entity.task.DoneTask;
import xtt.cloud.oa.workflow.domain.flow.repository.DoneTaskRepository;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.DoneTaskMapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.converter.DoneTaskConverter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 已办任务仓储实现
 * 
 * @author xtt
 */
@Repository
public class DoneTaskRepositoryImpl implements DoneTaskRepository {
    
    private final DoneTaskMapper mapper;
    
    public DoneTaskRepositoryImpl(DoneTaskMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public Optional<DoneTask> findById(Long id) {
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask po = mapper.selectById(id);
        return Optional.ofNullable(DoneTaskConverter.toEntity(po));
    }
    
    @Override
    public List<DoneTask> findByHandlerId(Long handlerId) {
        List<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask> pos = mapper.selectList(
            new LambdaQueryWrapper<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask>()
                .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask::getHandlerId, handlerId)
        );
        return pos.stream()
                .map(DoneTaskConverter::toEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DoneTask> findByFlowInstanceId(Long flowInstanceId) {
        List<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask> pos = mapper.selectList(
            new LambdaQueryWrapper<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask>()
                .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask::getFlowInstanceId, flowInstanceId)
                .orderByDesc(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask::getHandledAt)
        );
        return pos.stream()
                .map(DoneTaskConverter::toEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public IPage<DoneTask> findPageByHandlerId(Long handlerId, int pageNum, int pageSize) {
        Page<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask> page = 
                new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask> queryWrapper = 
                new LambdaQueryWrapper<>();
        queryWrapper.eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask::getHandlerId, handlerId)
                   .orderByDesc(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask::getHandledAt);
        
        IPage<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask> poPage = 
                mapper.selectPage(page, queryWrapper);
        
        // 转换为领域实体分页
        Page<DoneTask> entityPage = new Page<>(pageNum, pageSize, poPage.getTotal());
        entityPage.setRecords(poPage.getRecords().stream()
                .map(DoneTaskConverter::toEntity)
                .collect(Collectors.toList()));
        
        return entityPage;
    }
    
    @Override
    public IPage<DoneTask> findPageByTypeAndHandlerId(Integer taskType, Long handlerId, int pageNum, int pageSize) {
        Page<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask> page = 
                new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask> queryWrapper = 
                new LambdaQueryWrapper<>();
        queryWrapper.eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask::getTaskType, taskType)
                   .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask::getHandlerId, handlerId)
                   .orderByDesc(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask::getHandledAt);
        
        IPage<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask> poPage = 
                mapper.selectPage(page, queryWrapper);
        
        // 转换为领域实体分页
        Page<DoneTask> entityPage = new Page<>(pageNum, pageSize, poPage.getTotal());
        entityPage.setRecords(poPage.getRecords().stream()
                .map(DoneTaskConverter::toEntity)
                .collect(Collectors.toList()));
        
        return entityPage;
    }
    
    @Override
    public DoneTask save(DoneTask task) {
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask po = DoneTaskConverter.toPO(task);
        if (po.getId() == null) {
            mapper.insert(po);
        } else {
            mapper.updateById(po);
        }
        // 更新实体ID
        task.setId(po.getId());
        return task;
    }
    
    @Override
    public void update(DoneTask task) {
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask po = DoneTaskConverter.toPO(task);
        mapper.updateById(po);
    }
    
    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

