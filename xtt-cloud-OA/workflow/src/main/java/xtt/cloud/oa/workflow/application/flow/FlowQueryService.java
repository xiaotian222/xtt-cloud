package xtt.cloud.oa.workflow.application.flow;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.workflow.application.flow.dto.FlowInstanceDTO;
import xtt.cloud.oa.workflow.application.flow.query.FlowInstanceQuery;
import xtt.cloud.oa.workflow.application.flow.query.TodoTaskQuery;

import java.util.List;

/**
 * 流程查询应用服务
 * 
 * 专门处理查询相关的业务逻辑（CQRS 中的 Query 端）
 * 
 * @author xtt
 */
@Service
public class FlowQueryService {
    
    private final FlowApplicationService flowApplicationService;
    
    public FlowQueryService(FlowApplicationService flowApplicationService) {
        this.flowApplicationService = flowApplicationService;
    }
    
    /**
     * 查询流程实例
     */
    @Transactional(readOnly = true)
    public FlowInstanceDTO getFlowInstance(Long flowInstanceId) {
        return flowApplicationService.getFlowInstance(flowInstanceId);
    }
    
    /**
     * 查询流程实例列表
     */
    @Transactional(readOnly = true)
    public List<FlowInstanceDTO> queryFlowInstances(FlowInstanceQuery query) {
        return flowApplicationService.queryFlowInstances(query);
    }
    
    /**
     * 查询待办任务
     */
    @Transactional(readOnly = true)
    public List<FlowInstanceDTO> queryTodoTasks(TodoTaskQuery query) {
        // TODO: 实现待办任务查询逻辑
        return List.of();
    }
}

