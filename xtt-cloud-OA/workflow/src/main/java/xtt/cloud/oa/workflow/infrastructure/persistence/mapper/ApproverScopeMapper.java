package xtt.cloud.oa.workflow.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.pojo.ApproverScope;

/**
 * 审批人选择范围 Mapper
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Mapper
public interface ApproverScopeMapper extends BaseMapper<ApproverScope> {
    // 审批人选择范围数据访问接口
}

