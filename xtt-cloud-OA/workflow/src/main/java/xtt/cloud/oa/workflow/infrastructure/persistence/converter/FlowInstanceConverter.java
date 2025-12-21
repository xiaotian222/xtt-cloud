package xtt.cloud.oa.workflow.infrastructure.persistence.converter;

import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowInstanceId;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowStatus;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowType;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowMode;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.FlowNodeInstanceMapper;

/**
 * 流程实例转换器
 * 
 * 负责聚合根（Domain Model）和持久化对象（PO）之间的转换
 * 
 * @author xtt
 */
public class FlowInstanceConverter {
    
    private final FlowNodeInstanceMapper flowNodeInstanceMapper;
    
    public FlowInstanceConverter(FlowNodeInstanceMapper flowNodeInstanceMapper) {
        this.flowNodeInstanceMapper = flowNodeInstanceMapper;
    }
    
    /**
     * 聚合根转PO（用于保存）
     */
    public xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowInstance toPO(FlowInstance aggregate) {
        if (aggregate == null) {
            return null;
        }

        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowInstance po = new xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowInstance();
        po.setId(aggregate.getId() != null ? aggregate.getId().getValue() : null);
        po.setDocumentId(aggregate.getDocumentId());
        po.setFlowDefId(aggregate.getFlowDefId());
        
        if (aggregate.getFlowType() != null) {
            po.setFlowType(aggregate.getFlowType().getValue());
        }
        
        if (aggregate.getStatus() != null) {
            po.setStatus(aggregate.getStatus().getValue());
        }
        
        if (aggregate.getFlowMode() != null) {
            po.setFlowMode(aggregate.getFlowMode().getValue());
        }
        
        po.setCurrentNodeId(aggregate.getCurrentNodeId());
        po.setParentFlowInstanceId(aggregate.getParentFlowInstanceId());
        po.setStartTime(aggregate.getStartTime());
        po.setEndTime(aggregate.getEndTime());
        po.setCreatedAt(aggregate.getCreatedAt());
        po.setUpdatedAt(aggregate.getUpdatedAt());
        
        // ProcessVariables 需要序列化为 JSON 字符串存储
        // TODO: 在 PO 中添加 processVariables 字段，或使用扩展表存储
        
        return po;
    }
    
    /**
     * PO转聚合根（用于查询）
     */
    public FlowInstance toAggregate(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowInstance po) {
        if (po == null) {
            return null;
        }
        
        FlowInstance aggregate = FlowInstance.reconstruct();
        
        if (po.getId() != null) {
            aggregate.setId(FlowInstanceId.of(po.getId()));
        }
        aggregate.setDocumentId(po.getDocumentId());
        aggregate.setFlowDefId(po.getFlowDefId());
        
        if (po.getFlowType() != null) {
            aggregate.setFlowType(FlowType.fromValue(po.getFlowType()));
        }
        
        if (po.getStatus() != null) {
            aggregate.setStatus(FlowStatus.fromValue(po.getStatus()));
        }
        
        if (po.getFlowMode() != null) {
            aggregate.setFlowMode(FlowMode.fromValue(po.getFlowMode()));
        }
        
        aggregate.setCurrentNodeId(po.getCurrentNodeId());
        aggregate.setParentFlowInstanceId(po.getParentFlowInstanceId());
        aggregate.setStartTime(po.getStartTime());
        aggregate.setEndTime(po.getEndTime());
        aggregate.setCreatedAt(po.getCreatedAt());
        aggregate.setUpdatedAt(po.getUpdatedAt());
        
        // 加载节点实例集合
        if (po.getId() != null) {
            // TODO: 加载节点实例集合
            // List<xtt.cloud.oa.document.domain.entity.flow.FlowNodeInstance> nodeInstancePOs = 
            //         flowNodeInstanceMapper.selectByFlowInstanceId(po.getId());
            // List<FlowNodeInstance> nodeInstances = nodeInstancePOs.stream()
            //         .map(this::toNodeInstanceEntity)
            //         .collect(Collectors.toList());
            // aggregate.setNodeInstances(nodeInstances);
        }
        
        // ProcessVariables 需要从扩展表或JSON字段中加载
        // TODO: 实现 ProcessVariables 的加载逻辑
        
        return aggregate;
    }
    
}

