package xtt.cloud.oa.workflow.domain.flow.service;

import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.task.TaskAction;

import java.util.List;

public interface TaskService {

    void createTodoTask(
            FlowNodeInstance nodeInstance,
            Long approverId,
            FlowInstance flowInstance);

    void createDoneTask(FlowInstance flowInstance, FlowNodeInstance nodeInstance, TaskAction action, String comments);

    void markAsHandled(FlowNodeInstance nodeInstance,
                       TaskAction action,
                       String comments,
                       FlowInstance flowInstance);

    void cancelTodoTask(Long todoTaskId);

    void cancelTodoTasks(List<Long> todoTaskIds);
}
