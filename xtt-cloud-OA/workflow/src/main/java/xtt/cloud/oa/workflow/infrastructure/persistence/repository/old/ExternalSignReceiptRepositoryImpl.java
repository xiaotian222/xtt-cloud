package xtt.cloud.oa.workflow.infrastructure.persistence.repository.old;

import org.springframework.stereotype.Repository;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.ExternalSignReceiptMapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.pojo.ExternalSignReceipt;

/**
 * 外部签收 Repository
 * 封装 ExternalSignReceiptMapper 的数据访问操作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Repository
public class ExternalSignReceiptRepositoryImpl {
    
    private final ExternalSignReceiptMapper mapper;

    public ExternalSignReceiptRepositoryImpl(ExternalSignReceiptMapper mapper) {
        this.mapper = mapper;
    }


    /**
     * 根据ID查询外部签收
     */
    public ExternalSignReceipt findById(Long id) {
        return mapper.selectById(id);
    }
    
    /**
     * 保存外部签收
     */
    public void save(ExternalSignReceipt receipt) {
        if (receipt.getId() == null) {
            mapper.insert(receipt);
        } else {
            mapper.updateById(receipt);
        }
    }
    
    /**
     * 更新外部签收
     */
    public void update(ExternalSignReceipt receipt) {
        mapper.updateById(receipt);
    }
    
    /**
     * 删除外部签收
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

