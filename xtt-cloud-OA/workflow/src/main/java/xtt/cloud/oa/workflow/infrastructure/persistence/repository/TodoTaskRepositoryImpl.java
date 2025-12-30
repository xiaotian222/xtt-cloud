package xtt.cloud.oa.workflow.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Repository;
import xtt.cloud.oa.workflow.domain.flow.model.entity.task.TodoTask;
import xtt.cloud.oa.workflow.domain.flow.repository.TodoTaskRepository;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.TodoTaskMapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.converter.TodoTaskConverter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 待办任务仓储实现
 * 
 * @author xtt
 */
@Repository
public class TodoTaskRepositoryImpl implements TodoTaskRepository {
    
    private final TodoTaskMapper mapper;
    
    public TodoTaskRepositoryImpl(TodoTaskMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public Optional<TodoTask> findById(Long id) {
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask po = mapper.selectById(id);
        return Optional.ofNullable(TodoTaskConverter.toEntity(po));
    }
    
    @Override
    public List<TodoTask> findByAssigneeId(Long assigneeId) {
        List<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask> pos = mapper.selectList(
            new LambdaQueryWrapper<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask>()
                .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask::getAssigneeId, assigneeId)
        );
        return pos.stream()
                .map(TodoTaskConverter::toEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<TodoTask> findByNodeInstanceIdAndAssigneeId(Long nodeInstanceId, Long assigneeId) {
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask po = mapper.selectOne(
            new LambdaQueryWrapper<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask>()
                .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask::getNodeInstanceId, nodeInstanceId)
                .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask::getAssigneeId, assigneeId)
                .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask::getStatus, 
                        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask.STATUS_PENDING)
        );
        return Optional.ofNullable(TodoTaskConverter.toEntity(po));
    }
    
    @Override
    public List<TodoTask> findByFlowInstanceId(Long flowInstanceId) {
        List<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask> pos = mapper.selectList(
            new LambdaQueryWrapper<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask>()
                .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask::getFlowInstanceId, flowInstanceId)
                .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask::getStatus, 
                        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask.STATUS_PENDING)
                .orderByDesc(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask::getCreatedAt)
        );
        return pos.stream()
                .map(TodoTaskConverter::toEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public IPage<TodoTask> findPageByAssigneeId(Long assigneeId, int pageNum, int pageSize) {
        Page<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask> page = 
                new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask> queryWrapper = 
                new LambdaQueryWrapper<>();
        queryWrapper.eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask::getAssigneeId, assigneeId)
                   .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask::getStatus, 
                           xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask.STATUS_PENDING)
                   .orderByDesc(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask::getCreatedAt);
        
        IPage<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask> poPage = 
                mapper.selectPage(page, queryWrapper);
        
        // 转换为领域实体分页
        Page<TodoTask> entityPage = new Page<>(pageNum, pageSize, poPage.getTotal());
        entityPage.setRecords(poPage.getRecords().stream()
                .map(TodoTaskConverter::toEntity)
                .collect(Collectors.toList()));
        
        return entityPage;
    }
    
    @Override
    public IPage<TodoTask> findPageByTypeAndAssigneeId(Integer taskType, Long assigneeId, int pageNum, int pageSize) {
        Page<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask> page = 
                new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask> queryWrapper = 
                new LambdaQueryWrapper<>();
        queryWrapper.eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask::getTaskType, taskType)
                   .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask::getAssigneeId, assigneeId)
                   .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask::getStatus, 
                           xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask.STATUS_PENDING)
                   .orderByDesc(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask::getCreatedAt);
        
        IPage<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask> poPage = 
                mapper.selectPage(page, queryWrapper);
        
        // 转换为领域实体分页
        Page<TodoTask> entityPage = new Page<>(pageNum, pageSize, poPage.getTotal());
        entityPage.setRecords(poPage.getRecords().stream()
                .map(TodoTaskConverter::toEntity)
                .collect(Collectors.toList()));
        
        return entityPage;
    }
    
    @Override
    public TodoTask save(TodoTask task) {
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask po = TodoTaskConverter.toPO(task);
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
    public void update(TodoTask task) {
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask po = TodoTaskConverter.toPO(task);
        mapper.updateById(po);
    }
    
    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

