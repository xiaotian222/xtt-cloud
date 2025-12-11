package xtt.cloud.oa.document.domain.entity.gw;

import java.time.LocalDateTime;

/**
 * 发文信息实体
 * Document 的子类型，用于发文流程
 */
public class IssuanceInfo {
    private Long id;
    private Long documentId;  // 关联到 Document，而不是 FlowInstance
    private Long draftUserId;
    private Long draftDeptId;
    private String issuingUnit;
    private String documentCategory;
    private Integer urgencyLevel;
    private Integer secretLevel;
    private Integer wordCount;
    private Integer printingCopies;
    private String mainRecipients;
    private String ccRecipients;
    private String keywords;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     *     private Long id;
     *     private String title;
     *     private String docNumber;
     *     private Long docTypeId;
     *     private Integer secretLevel;
     *     private Integer urgencyLevel;
     *     private String content;
     *     private String attachment;
     *     private Integer status;
     *     private Long creatorId;
     *     private Long deptId;
     *     private LocalDateTime publishTime;
     *
     */

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    
    public Long getDraftUserId() { return draftUserId; }
    public void setDraftUserId(Long draftUserId) { this.draftUserId = draftUserId; }
    
    public Long getDraftDeptId() { return draftDeptId; }
    public void setDraftDeptId(Long draftDeptId) { this.draftDeptId = draftDeptId; }
    
    public String getIssuingUnit() { return issuingUnit; }
    public void setIssuingUnit(String issuingUnit) { this.issuingUnit = issuingUnit; }
    
    public String getDocumentCategory() { return documentCategory; }
    public void setDocumentCategory(String documentCategory) { this.documentCategory = documentCategory; }
    
    public Integer getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(Integer urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    
    public Integer getSecretLevel() { return secretLevel; }
    public void setSecretLevel(Integer secretLevel) { this.secretLevel = secretLevel; }
    
    public Integer getWordCount() { return wordCount; }
    public void setWordCount(Integer wordCount) { this.wordCount = wordCount; }
    
    public Integer getPrintingCopies() { return printingCopies; }
    public void setPrintingCopies(Integer printingCopies) { this.printingCopies = printingCopies; }
    
    public String getMainRecipients() { return mainRecipients; }
    public void setMainRecipients(String mainRecipients) { this.mainRecipients = mainRecipients; }
    
    public String getCcRecipients() { return ccRecipients; }
    public void setCcRecipients(String ccRecipients) { this.ccRecipients = ccRecipients; }
    
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}