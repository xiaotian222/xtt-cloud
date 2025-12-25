package xtt.cloud.oa.workflow.infrastructure.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.workflow.application.flow.dto.FlowInstanceDTO;
import xtt.cloud.oa.workflow.infrastructure.cache.flowinstance.FlowInstanceCacheService;
import xtt.cloud.oa.workflow.infrastructure.messaging.event.CacheInvalidationEvent;

import java.util.List;

/**
 * 缓存更新服务
 * 
 * 负责在数据变更时更新或失效缓存，保证缓存一致性
 * 
 * 使用策略：
 * 1. Cache Aside 模式：写操作后删除缓存，让下次读取时重新加载
 * 2. 事件驱动：通过事件通知其他服务更新缓存
 * 
 * @author xtt
 */
@Service
public class CacheUpdateService {
    
    private static final Logger log = LoggerFactory.getLogger(CacheUpdateService.class);
    
    private final FlowInstanceCacheService flowInstanceCacheService;
    private final ApplicationEventPublisher eventPublisher;
    
    public CacheUpdateService(
            FlowInstanceCacheService flowInstanceCacheService,
            ApplicationEventPublisher eventPublisher) {
        this.flowInstanceCacheService = flowInstanceCacheService;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * 更新流程实例缓存
     * 
     * 在流程实例保存后调用，更新缓存
     */
    @Transactional
    public void updateFlowInstanceCache(FlowInstanceDTO instance) {
        if (instance == null || instance.getId() == null) {
            return;
        }
        
        try {
            // 更新缓存
            flowInstanceCacheService.updateFlowInstance(instance);
            log.debug("流程实例缓存已更新，实例ID: {}", instance.getId());
        } catch (Exception e) {
            log.error("更新流程实例缓存失败，实例ID: {}", instance.getId(), e);
            // 缓存更新失败不影响业务，只记录日志
        }
    }
    
    /**
     * 失效流程实例缓存
     * 
     * 在流程实例删除或需要强制刷新时调用
     */
    public void evictFlowInstanceCache(Long instanceId) {
        if (instanceId == null) {
            return;
        }
        
        try {
            flowInstanceCacheService.evictFlowInstance(instanceId);
            log.debug("流程实例缓存已失效，实例ID: {}", instanceId);
        } catch (Exception e) {
            log.error("失效流程实例缓存失败，实例ID: {}", instanceId, e);
        }
    }
    
    /**
     * 失效流程实例缓存（按文档ID）
     */
    public void evictFlowInstanceCacheByDocumentId(Long documentId) {
        if (documentId == null) {
            return;
        }
        
        try {
            flowInstanceCacheService.evictFlowInstanceByDocumentId(documentId);
            log.debug("流程实例缓存已失效，文档ID: {}", documentId);
        } catch (Exception e) {
            log.error("失效流程实例缓存失败，文档ID: {}", documentId, e);
        }
    }
    
    /**
     * 发布缓存失效事件
     * 
     * 用于通知其他服务（如 Platform 服务）需要失效相关缓存
     */
    public void publishCacheInvalidationEvent(
            CacheInvalidationEvent.CacheType cacheType,
            CacheInvalidationEvent.OperationType operationType,
            List<Long> entityIds) {
        
        if (cacheType == null || operationType == null || entityIds == null || entityIds.isEmpty()) {
            return;
        }
        
        try {
            CacheInvalidationEvent event = new CacheInvalidationEvent(cacheType, operationType, entityIds);
            event.setSource("workflow-service");
            eventPublisher.publishEvent(event);
            log.debug("发布缓存失效事件，类型: {}, 操作: {}, 实体ID列表: {}", cacheType, operationType, entityIds);
        } catch (Exception e) {
            log.error("发布缓存失效事件失败", e);
        }
    }
}


