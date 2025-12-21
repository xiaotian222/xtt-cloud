package xtt.cloud.oa.document.domain.mapper.flow;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xtt.cloud.oa.document.domain.entity.flow.task.DoneTask;

/**
 * 已办任务 Mapper
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
//@Mapper
public interface DoneTaskMapper extends BaseMapper<DoneTask> {
    // 已办任务数据访问接口
}

