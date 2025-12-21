package xtt.cloud.oa.workflow.domain.flow.repository;

import xtt.cloud.oa.workflow.domain.flow.model.entity.Document;

import java.util.Optional;

/**
 * 文档仓储接口
 * 
 * @author xtt
 */
public interface DocumentRepository {
    
    /**
     * 根据ID查找文档
     */
    Optional<Document> findById(Long id);
    
    /**
     * 保存文档
     */
    void save(Document document);
    
    /**
     * 更新文档
     */
    void update(Document document);
    
    /**
     * 删除文档
     */
    void delete(Long id);
}

