package xtt.cloud.oa.document.domain.mapper.flow;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xtt.cloud.oa.document.domain.entity.flow.FlowAction;

/**
 * 发送动作 Mapper
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Mapper
public interface FlowActionMapper extends BaseMapper<FlowAction> {
    // 发送动作数据访问接口
}

