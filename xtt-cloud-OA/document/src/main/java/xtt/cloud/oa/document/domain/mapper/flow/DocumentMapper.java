package xtt.cloud.oa.document.domain.mapper.flow;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import xtt.cloud.oa.document.domain.entity.flow.Document;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DocumentMapper extends BaseMapper<Document> {
    // 公文数据访问接口
}