package xtt.cloud.oa.workflow.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowActionRule;

/**
 * 动作规则 Mapper
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Mapper
public interface FlowActionRuleMapper extends BaseMapper<FlowActionRule> {
    // 动作规则数据访问接口
}

