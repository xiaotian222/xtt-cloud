package xtt.cloud.oa.workflow.infrastructure.persistence.converter;

import xtt.cloud.oa.workflow.domain.flow.model.entity.history.TaskHistory;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskAction;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.NodeStatus;

/**
 * 任务历史转换器
 * 
 * 负责领域实体和持久化对象之间的转换
 * 
 * @author xtt
 */
public class TaskHistoryConverter {
    
    /**
     * 转换为持久化对象
     */
    public static xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TaskHistory toPO(
            TaskHistory entity) {
        if (entity == null) {
            return null;
        }
        
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TaskHistory po = 
                new xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TaskHistory();
        po.setId(entity.getId());
        po.setFlowInstanceId(entity.getFlowInstanceId());
        po.setNodeInstanceId(entity.getNodeInstanceId());
        po.setDocumentId(entity.getDocumentId());
        po.setDocumentTitle(entity.getDocumentTitle());
        po.setTaskName(entity.getTaskName());
        po.setHandlerId(entity.getHandlerId());
        po.setHandlerName(entity.getHandlerName());
        po.setHandlerDeptId(entity.getHandlerDeptId());
        po.setHandlerDeptName(entity.getHandlerDeptName());
        po.setTaskType(entity.getTaskType() != null ? entity.getTaskType().getValue() : null);
        po.setAction(entity.getAction() != null ? entity.getAction().getValue() : null);
        po.setActionName(entity.getActionName());
        po.setComments(entity.getComments());
        po.setStartTime(entity.getStartTime());
        po.setEndTime(entity.getEndTime());
        po.setDuration(entity.getDuration());
        po.setStatus(entity.getStatus() != null ? entity.getStatus().getValue() : null);
        po.setCreatedAt(entity.getCreatedAt());
        
        return po;
    }
    
    /**
     * 从持久化对象重建领域实体
     */
    public static TaskHistory toEntity(
            xtt.cloud.oa.workflow.infrastructure.persistence.pojo.TaskHistory po) {
        if (po == null) {
            return null;
        }
        
        TaskHistory entity =
                new TaskHistory();
        entity.setId(po.getId());
        entity.setFlowInstanceId(po.getFlowInstanceId());
        entity.setNodeInstanceId(po.getNodeInstanceId());
        entity.setDocumentId(po.getDocumentId());
        entity.setDocumentTitle(po.getDocumentTitle());
        entity.setTaskName(po.getTaskName());
        entity.setHandlerId(po.getHandlerId());
        entity.setHandlerName(po.getHandlerName());
        entity.setHandlerDeptId(po.getHandlerDeptId());
        entity.setHandlerDeptName(po.getHandlerDeptName());
        entity.setTaskType(TaskType.fromValue(po.getTaskType()));
        entity.setAction(po.getAction() != null ? TaskAction.fromValue(po.getAction()) : null);
        entity.setActionName(po.getActionName());
        entity.setComments(po.getComments());
        entity.setStartTime(po.getStartTime());
        entity.setEndTime(po.getEndTime());
        entity.setDuration(po.getDuration());
        entity.setStatus(NodeStatus.fromValue(po.getStatus()));
        entity.setCreatedAt(po.getCreatedAt());
        
        return entity;
    }
}

