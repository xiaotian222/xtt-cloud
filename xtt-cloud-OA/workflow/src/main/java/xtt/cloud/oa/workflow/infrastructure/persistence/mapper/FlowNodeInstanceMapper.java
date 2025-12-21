package xtt.cloud.oa.workflow.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNodeInstance;

import java.util.List;

/**
 * 节点实例 Mapper
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Mapper
public interface FlowNodeInstanceMapper extends BaseMapper<FlowNodeInstance> {
    void deleteByFlowInstanceId(Long id);

    List<FlowNodeInstance> selectByFlowInstanceId(Long flowInstanceId);

    List<FlowNodeInstance> selectByNodeIdAndFlowInstanceId(Long nodeId, Long flowInstanceId);

    List<FlowNodeInstance> selectPendingByApproverId(Long approverId);
    // 节点实例数据访问接口
}

