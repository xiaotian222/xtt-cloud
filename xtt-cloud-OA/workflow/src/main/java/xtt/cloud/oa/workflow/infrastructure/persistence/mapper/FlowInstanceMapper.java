package xtt.cloud.oa.workflow.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowInstance;

import java.util.List;

@Mapper
public interface FlowInstanceMapper extends BaseMapper<FlowInstance> {
    FlowInstance selectByDocumentId(Long documentId);

    List<FlowInstance> selectByStatus(Integer status);
    // 流程实例数据访问接口
}