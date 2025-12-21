package xtt.cloud.oa.workflow.domain.flow.model.factory;

import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowInstanceId;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowMode;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.ProcessVariables;

import java.util.Map;

/**
 * 流程实例工厂
 * 
 * 负责创建流程实例聚合根
 * 
 * @author xtt
 */
public class FlowInstanceFactory {
    
    /**
     * 创建流程实例
     * 
     * @param documentId 文档ID
     * @param flowDefId 流程定义ID
     * @param flowType 流程类型
     * @param flowMode 流程模式
     * @param processVariables 流程变量
     * @return 流程实例聚合根
     */
    public static FlowInstance create(Long documentId, Long flowDefId, 
                                     FlowType flowType, FlowMode flowMode,
                                     Map<String, Object> processVariables) {
        ProcessVariables variables = new ProcessVariables(processVariables);
        
        return FlowInstance.create(
                documentId,
                flowDefId,
                flowType,
                flowMode,
                variables
        );
    }
    
    /**
     * 从持久化对象重建流程实例
     * 
     * @param id 流程实例ID
     * @param documentId 文档ID
     * @param flowDefId 流程定义ID
     * @param flowType 流程类型
     * @param flowMode 流程模式
     * @param processVariables 流程变量
     * @return 流程实例聚合根
     */
    public static FlowInstance reconstruct(Long id, Long documentId, Long flowDefId,
                                           FlowType flowType, FlowMode flowMode,
                                           Map<String, Object> processVariables) {
        ProcessVariables variables = new ProcessVariables(processVariables);
        
        FlowInstance instance = FlowInstance.reconstruct();
        instance.setId(FlowInstanceId.of(id));
        instance.setDocumentId(documentId);
        instance.setFlowDefId(flowDefId);
        instance.setFlowType(flowType);
        instance.setFlowMode(flowMode);
        instance.setProcessVariables(variables);
        return instance;
    }
}

