package xtt.cloud.oa.workflow.domain.flow.repository;

import xtt.cloud.oa.workflow.domain.flow.model.entity.history.ActivityHistory;

import java.util.List;
import java.util.Optional;

/**
 * 活动历史仓储接口
 * 
 * @author xtt
 */
public interface ActivityHistoryRepository {
    
    /**
     * 根据ID查找活动历史
     */
    Optional<ActivityHistory> findById(Long id);
    
    /**
     * 根据流程实例ID查找活动历史列表
     */
    List<ActivityHistory> findByFlowInstanceId(Long flowInstanceId);
    
    /**
     * 根据节点实例ID查找活动历史列表
     */
    List<ActivityHistory> findByNodeInstanceId(Long nodeInstanceId);
    
    /**
     * 保存活动历史
     */
    ActivityHistory save(ActivityHistory history);
    
    /**
     * 更新活动历史
     */
    void update(ActivityHistory history);
    
    /**
     * 删除活动历史
     */
    void delete(Long id);
}

