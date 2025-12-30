package xtt.cloud.oa.workflow.application.flow.flowdefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowDefinition;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowDefinitionId;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowDefinitionRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeRepository;

import java.util.List;
import java.util.Optional;

/**
 * 流程定义应用服务
 * 
 * 职责：
 * 1. 编排领域对象完成业务流程
 * 2. 事务管理
 * 3. 权限控制
 * 
 * @author xtt
 */
@Service
public class FlowDefinitionApplicationService {
    
    private static final Logger log = LoggerFactory.getLogger(FlowDefinitionApplicationService.class);
    
    private final FlowDefinitionRepository flowDefinitionRepository;
    private final FlowNodeRepository flowNodeRepository;
    
    public FlowDefinitionApplicationService(
            FlowDefinitionRepository flowDefinitionRepository,
            FlowNodeRepository flowNodeRepository) {
        this.flowDefinitionRepository = flowDefinitionRepository;
        this.flowNodeRepository = flowNodeRepository;
    }
    
    /**
     * 创建流程定义
     * 
     * @param name 流程名称
     * @param code 流程编码
     * @param docTypeId 文档类型ID
     * @param description 描述
     * @param creatorId 创建人ID
     * @return 流程定义ID
     */
    @Transactional
    public FlowDefinitionId createFlowDefinition(
            String name,
            String code,
            Long docTypeId,
            String description,
            Long creatorId) {
        
        log.info("创建流程定义，名称: {}, 编码: {}", name, code);
        
        // 1. 检查编码是否已存在
        Optional<FlowDefinition> existing = flowDefinitionRepository.findByCode(code);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("流程定义编码已存在: " + code);
        }
        
        // 2. 创建聚合根
        FlowDefinition flowDef = FlowDefinition.create(name, code, docTypeId, description, creatorId);
        
        // 3. 保存聚合根
        flowDef = flowDefinitionRepository.save(flowDef);
        
        log.info("流程定义创建成功，ID: {}", flowDef.getId().getValue());
        
        return flowDef.getId();
    }
    
    /**
     * 添加节点到流程定义
     * 
     * @param flowDefId 流程定义ID
     * @param nodeName 节点名称
     * @param nodeType 节点类型
     * @param orderNum 顺序号
     * @return 节点ID
     */
    @Transactional
    public Long addNode(
            FlowDefinitionId flowDefId,
            String nodeName,
            Integer nodeType,
            Integer orderNum) {
        
        log.info("添加节点到流程定义，流程定义ID: {}, 节点名称: {}", flowDefId.getValue(), nodeName);
        
        // 1. 加载聚合根
        FlowDefinition flowDef = flowDefinitionRepository.findById(flowDefId)
                .orElseThrow(() -> new IllegalArgumentException("流程定义不存在: " + flowDefId.getValue()));
        
        // 2. 创建节点
        FlowNode node = FlowNode.create(
                flowDefId.getValue(),
                nodeName,
                nodeType,
                orderNum);
        
        // 3. 通过聚合根添加节点（聚合根会验证业务规则）
        flowDef.addNode(node);
        
        // 4. 保存聚合根（会自动保存节点）
        flowDefinitionRepository.save(flowDef);
        
        log.info("节点添加成功，节点ID: {}", node.getId());
        
        return node.getId();
    }
    
    /**
     * 删除节点
     * 
     * @param flowDefId 流程定义ID
     * @param nodeId 节点ID
     */
    @Transactional
    public void removeNode(FlowDefinitionId flowDefId, xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowNodeId nodeId) {
        log.info("删除节点，流程定义ID: {}, 节点ID: {}", flowDefId.getValue(), nodeId.getValue());
        
        // 1. 加载聚合根
        FlowDefinition flowDef = flowDefinitionRepository.findById(flowDefId)
                .orElseThrow(() -> new IllegalArgumentException("流程定义不存在: " + flowDefId.getValue()));
        
        // 2. 通过聚合根删除节点（聚合根会验证业务规则）
        flowDef.removeNode(nodeId);
        
        // 3. 保存聚合根（会自动删除节点）
        flowDefinitionRepository.save(flowDef);
        
        log.info("节点删除成功");
    }
    
    /**
     * 启用流程定义
     */
    @Transactional
    public void enableFlowDefinition(FlowDefinitionId flowDefId) {
        log.info("启用流程定义，ID: {}", flowDefId.getValue());
        
        FlowDefinition flowDef = flowDefinitionRepository.findById(flowDefId)
                .orElseThrow(() -> new IllegalArgumentException("流程定义不存在: " + flowDefId.getValue()));
        
        flowDef.enable();
        flowDefinitionRepository.save(flowDef);
        
        log.info("流程定义启用成功");
    }
    
    /**
     * 停用流程定义
     */
    @Transactional
    public void disableFlowDefinition(FlowDefinitionId flowDefId) {
        log.info("停用流程定义，ID: {}", flowDefId.getValue());
        
        FlowDefinition flowDef = flowDefinitionRepository.findById(flowDefId)
                .orElseThrow(() -> new IllegalArgumentException("流程定义不存在: " + flowDefId.getValue()));
        
        flowDef.disable();
        flowDefinitionRepository.save(flowDef);
        
        log.info("流程定义停用成功");
    }
    
    /**
     * 查询流程定义（不包含节点）
     */
    @Transactional(readOnly = true)
    public Optional<FlowDefinition> getFlowDefinition(FlowDefinitionId id) {
        return flowDefinitionRepository.findById(id);
    }
    
    /**
     * 查询流程定义及其节点
     */
    @Transactional(readOnly = true)
    public FlowDefinitionWithNodesDTO getFlowDefinitionWithNodes(FlowDefinitionId id) {
        FlowDefinition flowDef = flowDefinitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("流程定义不存在: " + id.getValue()));
        
        // 按需加载节点
        List<FlowNode> nodes = flowNodeRepository.findByFlowDefId(id);
        
        return new FlowDefinitionWithNodesDTO(flowDef, nodes);
    }
    
    /**
     * 查询所有启用的流程定义
     */
    @Transactional(readOnly = true)
    public List<FlowDefinition> getAllEnabledFlowDefinitions() {
        return flowDefinitionRepository.findAllEnabled();
    }
    
    /**
     * 根据文档类型查询流程定义
     */
    @Transactional(readOnly = true)
    public List<FlowDefinition> getFlowDefinitionsByDocType(Long docTypeId) {
        return flowDefinitionRepository.findByDocTypeId(docTypeId);
    }
    
    /**
     * 流程定义及节点DTO（内部类）
     */
    public static class FlowDefinitionWithNodesDTO {
        private final FlowDefinition flowDefinition;
        private final List<FlowNode> nodes;
        
        public FlowDefinitionWithNodesDTO(FlowDefinition flowDefinition, List<FlowNode> nodes) {
            this.flowDefinition = flowDefinition;
            this.nodes = nodes;
        }
        
        public FlowDefinition getFlowDefinition() {
            return flowDefinition;
        }
        
        public List<FlowNode> getNodes() {
            return nodes;
        }
    }
}

