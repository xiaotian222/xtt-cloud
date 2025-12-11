package xtt.cloud.oa.document.domain.mapper.flow;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xtt.cloud.oa.document.domain.entity.flow.FlowNode;

/**
 * 流程节点定义 Mapper
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Mapper
public interface FlowNodeMapper extends BaseMapper<FlowNode> {
    // 流程节点定义数据访问接口
}

