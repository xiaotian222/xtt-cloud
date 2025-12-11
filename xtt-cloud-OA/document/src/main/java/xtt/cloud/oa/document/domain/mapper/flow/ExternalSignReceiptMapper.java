package xtt.cloud.oa.document.domain.mapper.flow;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import xtt.cloud.oa.document.domain.entity.flow.ExternalSignReceipt;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExternalSignReceiptMapper extends BaseMapper<ExternalSignReceipt> {
    // 外单位签收登记数据访问接口
}