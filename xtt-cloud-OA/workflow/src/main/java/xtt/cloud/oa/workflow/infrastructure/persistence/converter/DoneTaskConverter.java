package xtt.cloud.oa.workflow.infrastructure.persistence.converter;

import xtt.cloud.oa.workflow.domain.flow.model.entity.task.DoneTask;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskAction;

/**
 * 已办任务转换器
 * 
 * 负责领域实体和持久化对象之间的转换
 * 
 * @author xtt
 */
public class DoneTaskConverter {
    
    /**
     * 转换为持久化对象
     */
    public static xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask toPO(
            DoneTask entity) {
        if (entity == null) {
            return null;
        }
        
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask po = 
                new xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask();
        po.setId(entity.getId());
        po.setDocumentId(entity.getDocumentId());
        po.setFlowInstanceId(entity.getFlowInstanceId());
        po.setNodeInstanceId(entity.getNodeInstanceId());
        po.setHandlerId(entity.getHandlerId());
        po.setTitle(entity.getTitle());
        po.setTaskType(entity.getTaskType() != null ? entity.getTaskType().getValue() : null);
        po.setAction(entity.getAction() != null ? entity.getAction().getValue() : null);
        po.setComments(entity.getComments());
        po.setHandledAt(entity.getHandledAt());
        po.setCreatedAt(entity.getCreatedAt());
        
        return po;
    }
    
    /**
     * 从持久化对象重建领域实体
     */
    public static DoneTask toEntity(
            xtt.cloud.oa.workflow.infrastructure.persistence.pojo.DoneTask po) {
        if (po == null) {
            return null;
        }
        
        DoneTask entity =
                new DoneTask();
        entity.setId(po.getId());
        entity.setDocumentId(po.getDocumentId());
        entity.setFlowInstanceId(po.getFlowInstanceId());
        entity.setNodeInstanceId(po.getNodeInstanceId());
        entity.setHandlerId(po.getHandlerId());
        entity.setTitle(po.getTitle());
        entity.setTaskType(TaskType.fromValue(po.getTaskType()));
        entity.setAction(po.getAction() != null ? TaskAction.fromValue(po.getAction()) : null);
        entity.setComments(po.getComments());
        entity.setHandledAt(po.getHandledAt());
        entity.setCreatedAt(po.getCreatedAt());
        
        return entity;
    }
}

