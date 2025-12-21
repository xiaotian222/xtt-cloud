package xtt.cloud.oa.workflow.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.pojo.ActivityHistory;

/**
 * 活动历史记录 Mapper
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Mapper
public interface ActivityHistoryMapper extends BaseMapper<ActivityHistory> {
    // 活动历史记录数据访问接口
}

