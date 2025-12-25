package xtt.cloud.oa.document.domain.mapper.flow;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xtt.cloud.oa.document.domain.entity.flow.history.TaskHistory;

/**
 * 任务历史记录 Mapper
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Mapper
public interface TaskHistoryMapper extends BaseMapper<TaskHistory> {
    // 任务历史记录数据访问接口
}

