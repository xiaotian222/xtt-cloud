package xtt.cloud.oa.document.application.flow.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Repository;
import xtt.cloud.oa.document.domain.entity.flow.task.DoneTask;
import xtt.cloud.oa.document.domain.mapper.flow.DoneTaskMapper;

import java.util.List;

/**
 * 已办任务 Repository
 * 封装 DoneTaskMapper 的数据访问操作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Repository
public class DoneTaskRepository {
    
    private final DoneTaskMapper mapper;
    
    public DoneTaskRepository(DoneTaskMapper mapper) {
        this.mapper = mapper;
    }
    
    /**
     * 根据ID查询已办任务
     */
    public DoneTask findById(Long id) {
        return mapper.selectById(id);
    }
    
    /**
     * 根据处理人ID查询已办任务列表
     */
    public List<DoneTask> findByHandlerId(Long handlerId) {
        return mapper.selectList(
            new LambdaQueryWrapper<DoneTask>()
                .eq(DoneTask::getHandlerId, handlerId)
        );
    }
    
    /**
     * 根据流程实例ID查询已办任务列表
     */
    public List<DoneTask> findByFlowInstanceId(Long flowInstanceId) {
        return mapper.selectList(
            new LambdaQueryWrapper<DoneTask>()
                .eq(DoneTask::getFlowInstanceId, flowInstanceId)
                .orderByDesc(DoneTask::getHandledAt)
        );
    }
    
    /**
     * 根据处理人ID分页查询已办任务
     */
    public IPage<DoneTask> findPageByHandlerId(Long handlerId, int pageNum, int pageSize) {
        Page<DoneTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DoneTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DoneTask::getHandlerId, handlerId)
                   .orderByDesc(DoneTask::getHandledAt);
        return mapper.selectPage(page, queryWrapper);
    }
    
    /**
     * 根据任务类型和处理人ID分页查询已办任务
     */
    public IPage<DoneTask> findPageByTypeAndHandlerId(Integer taskType, Long handlerId, int pageNum, int pageSize) {
        Page<DoneTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DoneTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DoneTask::getTaskType, taskType)
                   .eq(DoneTask::getHandlerId, handlerId)
                   .orderByDesc(DoneTask::getHandledAt);
        return mapper.selectPage(page, queryWrapper);
    }
    
    /**
     * 保存已办任务
     */
    public void save(DoneTask task) {
        if (task.getId() == null) {
            mapper.insert(task);
        } else {
            mapper.updateById(task);
        }
    }
    
    /**
     * 更新已办任务
     */
    public void update(DoneTask task) {
        mapper.updateById(task);
    }
    
    /**
     * 删除已办任务
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

