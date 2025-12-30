package xtt.cloud.oa.workflow.infrastructure.persistence.converter;

import xtt.cloud.oa.workflow.domain.flow.model.entity.task.TodoTask;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskStatus;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskPriority;

/**
 * 待办任务转换器
 * 
 * 负责领域实体和持久化对象之间的转换
 * 
 * @author xtt
 */
public class TodoTaskConverter {
    
    /**
     * 转换为持久化对象
     */
    public static xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask toPO(
            TodoTask entity) {
        if (entity == null) {
            return null;
        }
        
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask po = 
                new xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask();
        po.setId(entity.getId());
        po.setDocumentId(entity.getDocumentId());
        po.setFlowInstanceId(entity.getFlowInstanceId());
        po.setNodeInstanceId(entity.getNodeInstanceId());
        po.setAssigneeId(entity.getAssigneeId());
        po.setTitle(entity.getTitle());
        po.setContent(entity.getContent());
        po.setTaskType(entity.getTaskType() != null ? entity.getTaskType().getValue() : null);
        po.setPriority(entity.getPriority() != null ? entity.getPriority().getValue() : null);
        po.setStatus(entity.getStatus() != null ? entity.getStatus().getValue() : null);
        po.setDueDate(entity.getDueDate());
        po.setHandledAt(entity.getHandledAt());
        po.setCreatedAt(entity.getCreatedAt());
        po.setUpdatedAt(entity.getUpdatedAt());
        
        return po;
    }
    
    /**
     * 从持久化对象重建领域实体
     */
    public static TodoTask toEntity(
            xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TodoTask po) {
        if (po == null) {
            return null;
        }
        
        TodoTask entity =
                new TodoTask();
        entity.setId(po.getId());
        entity.setDocumentId(po.getDocumentId());
        entity.setFlowInstanceId(po.getFlowInstanceId());
        entity.setNodeInstanceId(po.getNodeInstanceId());
        entity.setAssigneeId(po.getAssigneeId());
        entity.setTitle(po.getTitle());
        entity.setContent(po.getContent());
        entity.setTaskType(TaskType.fromValue(po.getTaskType()));
        entity.setPriority(TaskPriority.fromValue(po.getPriority()));
        entity.setStatus(TaskStatus.fromValue(po.getStatus()));
        entity.setDueDate(po.getDueDate());
        entity.setHandledAt(po.getHandledAt());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        
        return entity;
    }
    
}

