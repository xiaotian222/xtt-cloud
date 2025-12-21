package xtt.cloud.oa.workflow.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNode;

import java.util.List;

/**
 * 流程节点定义 Mapper
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Mapper
public interface FlowNodeMapper extends BaseMapper<FlowNode> {
    List<FlowNode> findByFlowDefId(Long flowDefId);

    FlowNode findByFlowDefIdAndOrderNum(Long flowDefId, Integer orderNum);
    // 流程节点定义数据访问接口
}

