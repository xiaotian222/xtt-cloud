package xtt.cloud.oa.workflow.infrastructure.messaging.event;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 缓存失效事件
 * 
 * 用于通知缓存服务需要失效某些缓存数据
 * 
 * @author xtt
 */
public class CacheInvalidationEvent {
    
    /**
     * 缓存类型
     */
    public enum CacheType {
        USER,           // 用户缓存
        ROLE,           // 角色缓存
        DEPARTMENT,     // 部门缓存
        FLOW_INSTANCE   // 流程实例缓存
    }
    
    /**
     * 操作类型
     */
    public enum OperationType {
        UPDATE,         // 更新
        DELETE,         // 删除
        CREATE          // 创建（通常不需要失效，但可能需要）
    }
    
    private CacheType cacheType;
    private OperationType operationType;
    private List<Long> entityIds;  // 实体ID列表
    private LocalDateTime timestamp;
    private String source;  // 事件来源（如：platform-service）
    
    public CacheInvalidationEvent() {
        this.timestamp = LocalDateTime.now();
    }
    
    public CacheInvalidationEvent(CacheType cacheType, OperationType operationType, List<Long> entityIds) {
        this();
        this.cacheType = cacheType;
        this.operationType = operationType;
        this.entityIds = entityIds;
    }
    
    public CacheType getCacheType() {
        return cacheType;
    }
    
    public void setCacheType(CacheType cacheType) {
        this.cacheType = cacheType;
    }
    
    public OperationType getOperationType() {
        return operationType;
    }
    
    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }
    
    public List<Long> getEntityIds() {
        return entityIds;
    }
    
    public void setEntityIds(List<Long> entityIds) {
        this.entityIds = entityIds;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
}


