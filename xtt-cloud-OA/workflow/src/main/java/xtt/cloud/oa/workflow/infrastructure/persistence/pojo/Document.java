package xtt.cloud.oa.workflow.infrastructure.persistence.pojo;

import java.time.LocalDateTime;

/**
 * 文档实体 - 流程引擎中的文件抽象类型
 * 
 * Document 是流程引擎中的通用文档抽象，可以有不同的子类型：
 * - IssuanceInfo（发文）：Document 的子类型，用于发文流程
 * - ReceiptInfo（收文）：Document 的子类型，用于收文流程
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
public class Document {
    
    // 文档状态常量
    public static final Integer STATUS_DRAFT = 0;        // 草稿
    public static final Integer STATUS_REVIEWING = 1;    // 审核中
    public static final Integer STATUS_PUBLISHED = 2;   // 已发布
    public static final Integer STATUS_ARCHIVED = 3;     // 已归档
    
    // 文档类型常量（通过 docTypeId 区分）
    // 1: 发文类型
    // 2: 收文类型
    // 其他类型可以扩展


    private Long id;
    private String title;
    private String docNumber;
    private Long docTypeId;
    private Integer secretLevel;
    private Integer urgencyLevel;
    private String content;
    private String attachment;
    private Integer status;
    private Long creatorId;
    private Long deptId;
    private LocalDateTime publishTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDocNumber() { return docNumber; }
    public void setDocNumber(String docNumber) { this.docNumber = docNumber; }
    
    public Long getDocTypeId() { return docTypeId; }
    public void setDocTypeId(Long docTypeId) { this.docTypeId = docTypeId; }
    
    public Integer getSecretLevel() { return secretLevel; }
    public void setSecretLevel(Integer secretLevel) { this.secretLevel = secretLevel; }
    
    public Integer getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(Integer urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    
    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}