package xtt.cloud.oa.document.domain.entity.flow;

import java.time.LocalDateTime;

/**
 * 外单位签收登记实体
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
public class ExternalSignReceipt {
    private Long id;
    private Long flowInstanceId;    // 流程实例ID
    private Long externalUnitId;      // 外单位ID
    private String receiverName;     // 接收人姓名
    private LocalDateTime receiptTime; // 签收时间
    private Integer receiptStatus;   // 签收状态(0:未签收,1:已签收,2:拒签)
    private String remarks;          // 备注
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 签收状态常量
    public static final int STATUS_UNRECEIVED = 0;  // 未签收
    public static final int STATUS_RECEIVED = 1;   // 已签收
    public static final int STATUS_REJECTED = 2;    // 拒签
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getFlowInstanceId() { return flowInstanceId; }
    public void setFlowInstanceId(Long flowInstanceId) { this.flowInstanceId = flowInstanceId; }
    
    public Long getExternalUnitId() { return externalUnitId; }
    public void setExternalUnitId(Long externalUnitId) { this.externalUnitId = externalUnitId; }
    
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    
    public LocalDateTime getReceiptTime() { return receiptTime; }
    public void setReceiptTime(LocalDateTime receiptTime) { this.receiptTime = receiptTime; }
    
    public Integer getReceiptStatus() { return receiptStatus; }
    public void setReceiptStatus(Integer receiptStatus) { this.receiptStatus = receiptStatus; }
    
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

