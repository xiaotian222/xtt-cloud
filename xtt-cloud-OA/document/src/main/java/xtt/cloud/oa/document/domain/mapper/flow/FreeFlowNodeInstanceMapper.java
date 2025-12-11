package xtt.cloud.oa.document.domain.mapper.flow;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xtt.cloud.oa.document.domain.entity.flow.FreeFlowNodeInstance;

/**
 * 自由流节点实例扩展 Mapper
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Mapper
public interface FreeFlowNodeInstanceMapper extends BaseMapper<FreeFlowNodeInstance> {
    // 自由流节点实例扩展数据访问接口
}

