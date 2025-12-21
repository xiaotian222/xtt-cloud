package xtt.cloud.oa.workflow.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.pojo.Handling;

@Mapper
public interface HandlingMapper extends BaseMapper<Handling> {
    // 承办记录数据访问接口
}