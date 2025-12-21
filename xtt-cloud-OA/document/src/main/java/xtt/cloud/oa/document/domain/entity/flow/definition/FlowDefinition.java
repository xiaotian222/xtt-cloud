package xtt.cloud.oa.document.domain.entity.flow.definition;

import java.time.LocalDateTime;

/**
 * 流程定义实体
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
public class FlowDefinition {
    private Long id;
    private String name;           // 流程名称
    private String code;           // 流程编码（唯一）
    private Long docTypeId;        // 适用公文类型ID
    private String description;    // 流程描述
    private Integer version;       // 版本号
    private Integer status;        // 状态（0:停用,1:启用）
    private Long creatorId;        // 创建人ID
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 状态常量
    public static final int STATUS_DISABLED = 0;  // 停用
    public static final int STATUS_ENABLED = 1;   // 启用
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public Long getDocTypeId() { return docTypeId; }
    public void setDocTypeId(Long docTypeId) { this.docTypeId = docTypeId; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

