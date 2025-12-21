package xtt.cloud.oa.workflow.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask;

/**
 * 待办任务 Mapper
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Mapper
public interface TodoTaskMapper extends BaseMapper<TodoTask> {
    // 待办任务数据访问接口
}

