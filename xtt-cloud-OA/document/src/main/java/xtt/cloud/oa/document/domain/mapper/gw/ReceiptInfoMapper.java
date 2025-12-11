package xtt.cloud.oa.document.domain.mapper.gw;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import xtt.cloud.oa.document.domain.entity.gw.ReceiptInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReceiptInfoMapper extends BaseMapper<ReceiptInfo> {
    // 收文流程扩展信息数据访问接口
}