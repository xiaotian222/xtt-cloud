package xtt.cloud.oa.workflow.infrastructure.cache.flowinstance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import xtt.cloud.oa.workflow.application.flow.dto.FlowInstanceDTO;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 流程实例缓存服务
 * 使用 Redis 缓存流程实例信息，减少数据库查询
 * 
 * @author xtt
 */
@Service
public class FlowInstanceCacheService {

    private static final Logger log = LoggerFactory.getLogger(FlowInstanceCacheService.class);
    
    private static final String FLOW_INSTANCE_CACHE_PREFIX = "workflow:instance:";
    private static final String FLOW_INSTANCE_BY_DOCUMENT_PREFIX = "workflow:instance:doc:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${workflow.cache.instance.expiration:1800}")
    private Long cacheExpiration; // 默认30分钟

    public FlowInstanceCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 从缓存中获取流程实例信息
     * 
     * @param instanceId 流程实例ID
     * @return 流程实例信息，如果缓存不存在则返回 empty
     */
    public Optional<FlowInstanceDTO> getFlowInstanceById(Long instanceId) {
        if (instanceId == null) {
            return Optional.empty();
        }
        
        try {
            String key = FLOW_INSTANCE_CACHE_PREFIX + instanceId;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                FlowInstanceDTO instance = objectMapper.readValue(value, FlowInstanceDTO.class);
                log.debug("Flow instance cache hit for instanceId: {}", instanceId);
                return Optional.of(instance);
            }
            log.debug("Flow instance cache miss for instanceId: {}", instanceId);
            return Optional.empty();
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize flow instance from cache for instanceId: {}", instanceId, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get flow instance from cache for instanceId: {}", instanceId, e);
            return Optional.empty();
        }
    }

    /**
     * 根据文档ID从缓存中获取流程实例信息
     * 
     * @param documentId 文档ID
     * @return 流程实例信息，如果缓存不存在则返回 empty
     */
    public Optional<FlowInstanceDTO> getFlowInstanceByDocumentId(Long documentId) {
        if (documentId == null) {
            return Optional.empty();
        }
        
        try {
            String key = FLOW_INSTANCE_BY_DOCUMENT_PREFIX + documentId;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                FlowInstanceDTO instance = objectMapper.readValue(value, FlowInstanceDTO.class);
                log.debug("Flow instance cache hit for documentId: {}", documentId);
                return Optional.of(instance);
            }
            log.debug("Flow instance cache miss for documentId: {}", documentId);
            return Optional.empty();
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize flow instance from cache for documentId: {}", documentId, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get flow instance from cache for documentId: {}", documentId, e);
            return Optional.empty();
        }
    }

    /**
     * 将流程实例信息存入缓存
     * 
     * @param instance 流程实例信息
     */
    public void cacheFlowInstance(FlowInstanceDTO instance) {
        if (instance == null || instance.getId() == null) {
            return;
        }
        
        try {
            // 按实例ID缓存
            String instanceKey = FLOW_INSTANCE_CACHE_PREFIX + instance.getId();
            String instanceValue = objectMapper.writeValueAsString(instance);
            redisTemplate.opsForValue().set(instanceKey, instanceValue, cacheExpiration, TimeUnit.SECONDS);
            
            // 按文档ID缓存（如果存在）
            if (instance.getDocumentId() != null) {
                String docKey = FLOW_INSTANCE_BY_DOCUMENT_PREFIX + instance.getDocumentId();
                redisTemplate.opsForValue().set(docKey, instanceValue, cacheExpiration, TimeUnit.SECONDS);
            }
            
            log.debug("Flow instance cached for instanceId: {}", instance.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize flow instance for cache for instanceId: {}", instance.getId(), e);
        } catch (Exception e) {
            log.error("Failed to cache flow instance for instanceId: {}", instance.getId(), e);
        }
    }

    /**
     * 从缓存中删除流程实例信息
     * 
     * @param instanceId 流程实例ID
     */
    public void evictFlowInstance(Long instanceId) {
        if (instanceId == null) {
            return;
        }
        
        try {
            String instanceKey = FLOW_INSTANCE_CACHE_PREFIX + instanceId;
            redisTemplate.delete(instanceKey);
            
            // 注意：这里无法直接删除按文档ID的缓存，因为需要知道 documentId
            // 如果需要，可以在缓存时记录 instanceId -> documentId 的映射关系
            log.debug("Flow instance cache evicted for instanceId: {}", instanceId);
        } catch (Exception e) {
            log.error("Failed to evict flow instance cache for instanceId: {}", instanceId, e);
        }
    }

    /**
     * 从缓存中删除流程实例信息（按文档ID）
     * 
     * @param documentId 文档ID
     */
    public void evictFlowInstanceByDocumentId(Long documentId) {
        if (documentId == null) {
            return;
        }
        
        try {
            String docKey = FLOW_INSTANCE_BY_DOCUMENT_PREFIX + documentId;
            redisTemplate.delete(docKey);
            log.debug("Flow instance cache evicted for documentId: {}", documentId);
        } catch (Exception e) {
            log.error("Failed to evict flow instance cache for documentId: {}", documentId, e);
        }
    }

    /**
     * 更新流程实例缓存
     * 先删除旧缓存，再存入新缓存
     * 
     * @param instance 流程实例信息
     */
    public void updateFlowInstance(FlowInstanceDTO instance) {
        if (instance == null || instance.getId() == null) {
            return;
        }
        
        // 先删除旧缓存
        evictFlowInstance(instance.getId());
        if (instance.getDocumentId() != null) {
            evictFlowInstanceByDocumentId(instance.getDocumentId());
        }
        
        // 再存入新缓存
        cacheFlowInstance(instance);
    }
}

