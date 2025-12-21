package xtt.cloud.oa.document.application.flow.core;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import xtt.cloud.oa.common.BusinessException;
import xtt.cloud.oa.document.domain.entity.flow.definition.Document;
import xtt.cloud.oa.document.domain.mapper.flow.DocumentMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 公文服务
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Service
public class DocumentService {
    
    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
    
    private static final DateTimeFormatter DOC_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    // 公文状态常量
    public static final int STATUS_DRAFT = 0;        // 草稿
    public static final int STATUS_REVIEWING = 1;    // 审核中
    public static final int STATUS_PUBLISHED = 2;    // 已发布
    public static final int STATUS_ARCHIVED = 3;     // 已归档
    
    // 密级常量
    public static final int SECRET_LEVEL_NORMAL = 0;  // 普通
    public static final int SECRET_LEVEL_SECRET = 1;  // 秘密
    public static final int SECRET_LEVEL_CONFIDENTIAL = 2; // 机密
    public static final int SECRET_LEVEL_TOP_SECRET = 3;  // 绝密
    
    // 紧急程度常量
    public static final int URGENCY_LEVEL_NORMAL = 0; // 普通
    public static final int URGENCY_LEVEL_URGENT = 1; // 急
    public static final int URGENCY_LEVEL_VERY_URGENT = 2; // 特急
    
    @Autowired
    private DocumentMapper documentMapper;
    
    /**
     * 创建公文
     */
    @Transactional
    public Document createDocument(Document document) {
        log.info("创建公文，标题: {}, 类型ID: {}, 创建人ID: {}", 
                document.getTitle(), document.getDocTypeId(), document.getCreatorId());
        
        try {
            // 参数验证
            validateDocument(document);
            
            // 生成公文编号
            String docNumber = generateDocNumber(document.getDocTypeId());
            document.setDocNumber(docNumber);
            
            // 设置默认值
            if (document.getStatus() == null) {
                document.setStatus(STATUS_DRAFT);
            }
            if (document.getSecretLevel() == null) {
                document.setSecretLevel(SECRET_LEVEL_NORMAL);
            }
            if (document.getUrgencyLevel() == null) {
                document.setUrgencyLevel(URGENCY_LEVEL_NORMAL);
            }
            
            // 设置时间戳
            document.setCreatedAt(LocalDateTime.now());
            document.setUpdatedAt(LocalDateTime.now());
            
            // 保存到数据库
            documentMapper.insert(document);
            
            log.info("公文创建成功，ID: {}, 公文编号: {}, 标题: {}", 
                    document.getId(), docNumber, document.getTitle());
            
            return document;
        } catch (BusinessException e) {
            log.warn("创建公文失败，标题: {}, 原因: {}", document.getTitle(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("创建公文失败，标题: {}", document.getTitle(), e);
            throw new BusinessException("创建公文失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新公文
     */
    @Transactional
    public Document updateDocument(Long id, Document document) {
        log.info("更新公文，ID: {}, 标题: {}", id, document.getTitle());
        
        try {
            // 检查公文是否存在
            Document existing = documentMapper.selectById(id);
            if (existing == null) {
                log.warn("更新公文失败，公文不存在，ID: {}", id);
                throw new BusinessException("公文不存在");
            }
            
            // 参数验证
            validateDocument(document);
            
            // 设置ID和更新时间
            document.setId(id);
            document.setUpdatedAt(LocalDateTime.now());
            
            // 保留创建时间和创建人（不允许修改）
            document.setCreatedAt(existing.getCreatedAt());
            document.setCreatorId(existing.getCreatorId());
            
            // 保留公文编号（不允许修改）
            document.setDocNumber(existing.getDocNumber());
            
            // 更新数据库
            documentMapper.updateById(document);
            
            log.info("公文更新成功，ID: {}, 标题: {}", id, document.getTitle());
            
            return document;
        } catch (BusinessException e) {
            log.warn("更新公文失败，ID: {}, 原因: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("更新公文失败，ID: {}", id, e);
            throw new BusinessException("更新公文失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除公文（逻辑删除，通过状态标记）
     */
    @Transactional
    public void deleteDocument(Long id) {
        log.info("删除公文，ID: {}", id);
        
        try {
            Document document = documentMapper.selectById(id);
            if (document == null) {
                log.warn("删除公文失败，公文不存在，ID: {}", id);
                throw new BusinessException("公文不存在");
            }
            
            // 检查是否可以删除（只有草稿状态可以删除）
            if (document.getStatus() != STATUS_DRAFT) {
                log.warn("删除公文失败，公文状态不允许删除，ID: {}, 状态: {}", id, document.getStatus());
                throw new BusinessException("只有草稿状态的公文可以删除");
            }
            
            // 物理删除（或可以改为逻辑删除）
            documentMapper.deleteById(id);
            
            log.info("公文删除成功，ID: {}", id);
        } catch (BusinessException e) {
            log.warn("删除公文失败，ID: {}, 原因: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("删除公文失败，ID: {}", id, e);
            throw new BusinessException("删除公文失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取公文详情
     */
    public Document getDocument(Long id) {
        log.debug("查询公文详情，ID: {}", id);
        
        try {
            Document document = documentMapper.selectById(id);
            if (document == null) {
                log.debug("公文不存在，ID: {}", id);
                return null;
            }
            
            log.debug("查询到公文，ID: {}, 标题: {}, 状态: {}", 
                    id, document.getTitle(), document.getStatus());
            
            return document;
        } catch (Exception e) {
            log.error("查询公文详情失败，ID: {}", id, e);
            throw new BusinessException("查询公文详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 分页查询公文列表
     */
    public IPage<Document> listDocuments(DocumentQuery query) {
        log.debug("查询公文列表，页码: {}, 每页大小: {}, 标题: {}, 类型ID: {}, 状态: {}", 
                query.getPage(), query.getSize(), query.getTitle(), query.getDocTypeId(), query.getStatus());
        
        try {
            // 创建分页对象
            Page<Document> page = new Page<>(query.getPage(), query.getSize());
            
            // 构建查询条件
            LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
            
            // 标题模糊查询
            if (StringUtils.hasText(query.getTitle())) {
                wrapper.like(Document::getTitle, query.getTitle());
            }
            
            // 公文类型
            if (query.getDocTypeId() != null) {
                wrapper.eq(Document::getDocTypeId, query.getDocTypeId());
            }
            
            // 状态
            if (query.getStatus() != null) {
                wrapper.eq(Document::getStatus, query.getStatus());
            }
            
            // 创建人
            if (query.getCreatorId() != null) {
                wrapper.eq(Document::getCreatorId, query.getCreatorId());
            }
            
            // 部门
            if (query.getDeptId() != null) {
                wrapper.eq(Document::getDeptId, query.getDeptId());
            }
            
            // 创建时间范围
            if (query.getStartTime() != null) {
                wrapper.ge(Document::getCreatedAt, query.getStartTime());
            }
            if (query.getEndTime() != null) {
                wrapper.le(Document::getCreatedAt, query.getEndTime());
            }
            
            // 按创建时间倒序
            wrapper.orderByDesc(Document::getCreatedAt);
            
            // 执行查询
            IPage<Document> result = documentMapper.selectPage(page, wrapper);
            
            log.debug("查询公文列表成功，总数: {}, 当前页: {}, 每页大小: {}", 
                    result.getTotal(), result.getCurrent(), result.getSize());
            
            return result;
        } catch (Exception e) {
            log.error("查询公文列表失败", e);
            throw new BusinessException("查询公文列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 发布公文
     */
    @Transactional
    public void publishDocument(Long id) {
        log.info("发布公文，ID: {}", id);
        
        try {
            Document document = documentMapper.selectById(id);
            if (document == null) {
                log.warn("发布公文失败，公文不存在，ID: {}", id);
                throw new BusinessException("公文不存在");
            }
            
            // 检查状态
            if (document.getStatus() != STATUS_REVIEWING) {
                log.warn("发布公文失败，公文状态不正确，ID: {}, 当前状态: {}", id, document.getStatus());
                throw new BusinessException("只有审核中的公文可以发布");
            }
            
            // 更新状态和发布时间
            document.setStatus(STATUS_PUBLISHED);
            document.setPublishTime(LocalDateTime.now());
            document.setUpdatedAt(LocalDateTime.now());
            
            documentMapper.updateById(document);
            
            log.info("公文发布成功，ID: {}, 发布时间: {}", id, document.getPublishTime());
        } catch (BusinessException e) {
            log.warn("发布公文失败，ID: {}, 原因: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("发布公文失败，ID: {}", id, e);
            throw new BusinessException("发布公文失败: " + e.getMessage());
        }
    }
    
    /**
     * 归档公文
     */
    @Transactional
    public void archiveDocument(Long id) {
        log.info("归档公文，ID: {}", id);
        
        try {
            Document document = documentMapper.selectById(id);
            if (document == null) {
                log.warn("归档公文失败，公文不存在，ID: {}", id);
                throw new BusinessException("公文不存在");
            }
            
            // 检查状态
            if (document.getStatus() != STATUS_PUBLISHED) {
                log.warn("归档公文失败，公文状态不正确，ID: {}, 当前状态: {}", id, document.getStatus());
                throw new BusinessException("只有已发布的公文可以归档");
            }
            
            // 更新状态
            document.setStatus(STATUS_ARCHIVED);
            document.setUpdatedAt(LocalDateTime.now());
            
            documentMapper.updateById(document);
            
            log.info("公文归档成功，ID: {}", id);
        } catch (BusinessException e) {
            log.warn("归档公文失败，ID: {}, 原因: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("归档公文失败，ID: {}", id, e);
            throw new BusinessException("归档公文失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成公文编号
     * 格式：DOC-YYYYMMDD-序号（如：DOC-20231201-001）
     */
    private String generateDocNumber(Long docTypeId) {
        String dateStr = LocalDateTime.now().format(DOC_NUMBER_FORMATTER);
        
        // 查询当天同类型的公文数量
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getDocTypeId, docTypeId);
        wrapper.like(Document::getDocNumber, dateStr);
        
        long count = documentMapper.selectCount(wrapper);
        String sequence = String.format("%03d", count + 1);
        
        return String.format("DOC-%s-%s", dateStr, sequence);
    }
    
    /**
     * 验证公文参数
     */
    private void validateDocument(Document document) {
        if (!StringUtils.hasText(document.getTitle())) {
            throw new BusinessException("公文标题不能为空");
        }
        if (document.getDocTypeId() == null) {
            throw new BusinessException("公文类型不能为空");
        }
        if (document.getCreatorId() == null) {
            throw new BusinessException("创建人不能为空");
        }
        if (document.getDeptId() == null) {
            throw new BusinessException("所属部门不能为空");
        }
    }
    
    /**
     * 公文查询条件类
     */
    public static class DocumentQuery {
        private Integer page = 1;
        private Integer size = 10;
        private String title;
        private Long docTypeId;
        private Integer status;
        private Long creatorId;
        private Long deptId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        
        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }
        
        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public Long getDocTypeId() { return docTypeId; }
        public void setDocTypeId(Long docTypeId) { this.docTypeId = docTypeId; }
        
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        
        public Long getCreatorId() { return creatorId; }
        public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
        
        public Long getDeptId() { return deptId; }
        public void setDeptId(Long deptId) { this.deptId = deptId; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    }
}
