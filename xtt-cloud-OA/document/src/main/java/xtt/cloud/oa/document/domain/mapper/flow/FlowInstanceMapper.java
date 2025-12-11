package xtt.cloud.oa.document.domain.mapper.flow;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import xtt.cloud.oa.document.domain.entity.flow.FlowInstance;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FlowInstanceMapper extends BaseMapper<FlowInstance> {
    // 流程实例数据访问接口
}