package xtt.cloud.oa.document.application.flow.repository;

import org.springframework.stereotype.Repository;
import xtt.cloud.oa.document.domain.entity.gw.ReceiptInfo;
import xtt.cloud.oa.document.domain.mapper.gw.ReceiptInfoMapper;

/**
 * 收文信息 Repository
 * 封装 ReceiptInfoMapper 的数据访问操作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Repository
public class ReceiptInfoRepository {
    
    private final ReceiptInfoMapper mapper;
    
    public ReceiptInfoRepository(ReceiptInfoMapper mapper) {
        this.mapper = mapper;
    }
    
    /**
     * 根据ID查询收文信息
     */
    public ReceiptInfo findById(Long id) {
        return mapper.selectById(id);
    }
    
    /**
     * 根据文档ID查询收文信息
     */
    public ReceiptInfo findByDocumentId(Long documentId) {
        return mapper.selectOne(null); // TODO: 添加查询条件
    }
    
    /**
     * 保存收文信息
     */
    public void save(ReceiptInfo info) {
        if (info.getId() == null) {
            mapper.insert(info);
        } else {
            mapper.updateById(info);
        }
    }
    
    /**
     * 更新收文信息
     */
    public void update(ReceiptInfo info) {
        mapper.updateById(info);
    }
    
    /**
     * 删除收文信息
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

