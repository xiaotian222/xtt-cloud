package xtt.cloud.oa.document.domain.entity.gw;

import java.time.LocalDateTime;

/**
 * 收文信息实体
 * Document 的子类型，用于收文流程
 */
public class ReceiptInfo {
    private Long id;
    private Long documentId;  // 关联到 Document，而不是 FlowInstance
    private LocalDateTime receiveDate;
    private String senderUnit;
    private String documentNumber;
    private Integer receiveMethod; // 1:纸质,2:电子,3:其他
    private String attachments;
    private String keywords;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 收文方式常量
    public static final int METHOD_PAPER = 1;     // 纸质
    public static final int METHOD_ELECTRONIC = 2; // 电子
    public static final int METHOD_OTHER = 3;      // 其他
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    
    public LocalDateTime getReceiveDate() { return receiveDate; }
    public void setReceiveDate(LocalDateTime receiveDate) { this.receiveDate = receiveDate; }
    
    public String getSenderUnit() { return senderUnit; }
    public void setSenderUnit(String senderUnit) { this.senderUnit = senderUnit; }
    
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    
    public Integer getReceiveMethod() { return receiveMethod; }
    public void setReceiveMethod(Integer receiveMethod) { this.receiveMethod = receiveMethod; }
    
    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }
    
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}