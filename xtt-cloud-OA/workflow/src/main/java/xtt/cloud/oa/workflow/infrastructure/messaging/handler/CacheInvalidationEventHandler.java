package xtt.cloud.oa.workflow.infrastructure.messaging.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import xtt.cloud.oa.workflow.infrastructure.cache.org.DepartmentCacheService;
import xtt.cloud.oa.workflow.infrastructure.cache.flowinstance.FlowInstanceCacheService;
import xtt.cloud.oa.workflow.infrastructure.cache.org.RoleCacheService;
import xtt.cloud.oa.workflow.infrastructure.cache.org.UserCacheService;
import xtt.cloud.oa.workflow.infrastructure.messaging.event.CacheInvalidationEvent;

import java.util.List;

/**
 * 缓存失效事件处理器
 * 
 * 监听缓存失效事件，并执行相应的缓存清理操作
 * 
 * @author xtt
 */
@Component
public class CacheInvalidationEventHandler {
    
    private static final Logger log = LoggerFactory.getLogger(CacheInvalidationEventHandler.class);
    
    private final UserCacheService userCacheService;
    private final RoleCacheService roleCacheService;
    private final DepartmentCacheService departmentCacheService;
    private final FlowInstanceCacheService flowInstanceCacheService;
    
    public CacheInvalidationEventHandler(
            UserCacheService userCacheService,
            RoleCacheService roleCacheService,
            DepartmentCacheService departmentCacheService,
            FlowInstanceCacheService flowInstanceCacheService) {
        this.userCacheService = userCacheService;
        this.roleCacheService = roleCacheService;
        this.departmentCacheService = departmentCacheService;
        this.flowInstanceCacheService = flowInstanceCacheService;
    }
    
    /**
     * 处理缓存失效事件
     * 
     * 使用 @Async 异步处理，避免阻塞主流程
     */
    @Async
    @EventListener
    public void handleCacheInvalidation(CacheInvalidationEvent event) {
        if (event == null || event.getEntityIds() == null || event.getEntityIds().isEmpty()) {
            return;
        }
        
        try {
            switch (event.getCacheType()) {
                case USER:
                    handleUserCacheInvalidation(event);
                    break;
                case ROLE:
                    handleRoleCacheInvalidation(event);
                    break;
                case DEPARTMENT:
                    handleDepartmentCacheInvalidation(event);
                    break;
                case FLOW_INSTANCE:
                    handleFlowInstanceCacheInvalidation(event);
                    break;
                default:
                    log.warn("未知的缓存类型: {}", event.getCacheType());
            }
        } catch (Exception e) {
            log.error("处理缓存失效事件失败，缓存类型: {}, 实体ID列表: {}", 
                    event.getCacheType(), event.getEntityIds(), e);
        }
    }
    
    /**
     * 处理用户缓存失效
     */
    private void handleUserCacheInvalidation(CacheInvalidationEvent event) {
        List<Long> userIds = event.getEntityIds();
        log.info("失效用户缓存，用户ID列表: {}", userIds);
        
        for (Long userId : userIds) {
            userCacheService.evictUser(userId);
        }
        
        // 如果用户角色或部门关系变更，还需要失效相关的角色和部门缓存
        if (event.getOperationType() == CacheInvalidationEvent.OperationType.UPDATE) {
            // 注意：这里无法知道具体变更了哪些关系，可能需要失效所有相关缓存
            // 或者 Platform 服务应该发送更详细的事件信息
            log.debug("用户信息更新，可能需要失效相关角色和部门缓存");
        }
    }
    
    /**
     * 处理角色缓存失效
     */
    private void handleRoleCacheInvalidation(CacheInvalidationEvent event) {
        List<Long> roleIds = event.getEntityIds();
        log.info("失效角色缓存，角色ID列表: {}", roleIds);
        
        for (Long roleId : roleIds) {
            roleCacheService.evictRole(roleId);
        }
        
        // 如果角色下的用户列表变更，还需要失效相关用户的缓存
        if (event.getOperationType() == CacheInvalidationEvent.OperationType.UPDATE) {
            log.debug("角色信息更新，可能需要失效相关用户缓存");
        }
    }
    
    /**
     * 处理部门缓存失效
     */
    private void handleDepartmentCacheInvalidation(CacheInvalidationEvent event) {
        List<Long> deptIds = event.getEntityIds();
        log.info("失效部门缓存，部门ID列表: {}", deptIds);
        
        for (Long deptId : deptIds) {
            departmentCacheService.evictDepartment(deptId);
        }
        
        // 如果部门下的用户列表变更，还需要失效相关用户的缓存
        if (event.getOperationType() == CacheInvalidationEvent.OperationType.UPDATE) {
            log.debug("部门信息更新，可能需要失效相关用户缓存");
        }
    }
    
    /**
     * 处理流程实例缓存失效
     */
    private void handleFlowInstanceCacheInvalidation(CacheInvalidationEvent event) {
        List<Long> instanceIds = event.getEntityIds();
        log.info("失效流程实例缓存，实例ID列表: {}", instanceIds);
        
        for (Long instanceId : instanceIds) {
            flowInstanceCacheService.evictFlowInstance(instanceId);
        }
    }
}


