package xtt.cloud.oa.workflow.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.pojo.ExternalSignReceipt;

@Mapper
public interface ExternalSignReceiptMapper extends BaseMapper<ExternalSignReceipt> {
    // 外单位签收登记数据访问接口
}