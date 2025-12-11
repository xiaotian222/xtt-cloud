package xtt.cloud.oa.document.domain.mapper.gw;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import xtt.cloud.oa.document.domain.entity.gw.IssuanceInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IssuanceInfoMapper extends BaseMapper<IssuanceInfo> {
    // 发文流程扩展信息数据访问接口
}