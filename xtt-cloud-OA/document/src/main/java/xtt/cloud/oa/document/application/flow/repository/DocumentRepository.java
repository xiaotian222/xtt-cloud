package xtt.cloud.oa.document.application.flow.repository;

import org.springframework.stereotype.Repository;
import xtt.cloud.oa.document.domain.entity.flow.definition.Document;
import xtt.cloud.oa.document.domain.mapper.flow.DocumentMapper;

/**
 * 文档 Repository
 * 封装 DocumentMapper 的数据访问操作
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Repository
public class DocumentRepository {
    
    private final DocumentMapper mapper;
    
    public DocumentRepository(DocumentMapper mapper) {
        this.mapper = mapper;
    }
    
    /**
     * 根据ID查询文档
     */
    public Document findById(Long id) {
        return mapper.selectById(id);
    }
    
    /**
     * 保存文档
     */
    public void save(Document document) {
        if (document.getId() == null) {
            mapper.insert(document);
        } else {
            mapper.updateById(document);
        }
    }
    
    /**
     * 更新文档
     */
    public void update(Document document) {
        mapper.updateById(document);
    }
    
    /**
     * 删除文档
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}

