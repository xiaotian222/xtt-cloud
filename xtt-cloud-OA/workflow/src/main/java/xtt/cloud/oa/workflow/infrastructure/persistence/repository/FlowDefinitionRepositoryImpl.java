package xtt.cloud.oa.workflow.infrastructure.persistence.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowDefinition;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowDefinitionId;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowDefinitionStatus;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowNodeId;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowDefinitionRepository;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeRepository;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.FlowDefinitionMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 流程定义仓储实现
 * 
 * 实现 FlowDefinition 聚合根的持久化
 * 处理节点的保存、更新、删除（通过变更追踪）
 * 
 * @author xtt
 */
@Repository
public class FlowDefinitionRepositoryImpl implements FlowDefinitionRepository {
    
    private static final Logger log = LoggerFactory.getLogger(FlowDefinitionRepositoryImpl.class);
    
    private final FlowDefinitionMapper flowDefinitionMapper;
    private final FlowNodeRepository flowNodeRepository;
    
    public FlowDefinitionRepositoryImpl(
            FlowDefinitionMapper flowDefinitionMapper,
            FlowNodeRepository flowNodeRepository) {
        this.flowDefinitionMapper = flowDefinitionMapper;
        this.flowNodeRepository = flowNodeRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<FlowDefinition> findById(FlowDefinitionId id) {
        if (id == null) {
            return Optional.empty();
        }
        
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowDefinition po = 
                flowDefinitionMapper.selectById(id.getValue());
        if (po == null) {
            return Optional.empty();
        }
        
        return Optional.of(toAggregate(po));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<FlowDefinition> findByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }
        
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowDefinition po = 
                flowDefinitionMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowDefinition>()
                                .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowDefinition::getCode, code));
        if (po == null) {
            return Optional.empty();
        }
        
        return Optional.of(toAggregate(po));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<FlowDefinition> findByDocTypeId(Long docTypeId) {
        if (docTypeId == null) {
            return List.of();
        }
        
        List<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowDefinition> pos = 
                flowDefinitionMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowDefinition>()
                                .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowDefinition::getDocTypeId, docTypeId));
        if (pos == null || pos.isEmpty()) {
            return List.of();
        }
        
        return pos.stream()
                .map(this::toAggregate)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<FlowDefinition> findAllEnabled() {
        List<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowDefinition> pos = 
                flowDefinitionMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowDefinition>()
                                .eq(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowDefinition::getStatus, 
                                        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowDefinition.STATUS_ENABLED));
        if (pos == null || pos.isEmpty()) {
            return List.of();
        }
        
        return pos.stream()
                .map(this::toAggregate)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public FlowDefinition save(FlowDefinition aggregate) {
        if (aggregate == null) {
            throw new IllegalArgumentException("Flow definition aggregate cannot be null");
        }
        
        // 1. 保存流程定义
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowDefinition po = toPO(aggregate);
        if (po.getId() == null) {
            // 新增
            flowDefinitionMapper.insert(po);
            // 设置ID
            if (aggregate.getId() == null && po.getId() != null) {
                aggregate.setId(FlowDefinitionId.of(po.getId()));
            }
        } else {
            // 更新
            flowDefinitionMapper.updateById(po);
        }
        
        // 2. 处理节点变更（通过变更追踪）
        // 2.1 保存新增的节点
        for (FlowNode node : aggregate.getNodesToAdd()) {
            // 确保节点关联到当前流程定义
            if (node.getPO() != null) {
                node.getPO().setFlowDefId(aggregate.getId().getValue());
            }
            flowNodeRepository.save(node);
        }
        
        // 2.2 更新节点
        for (FlowNode node : aggregate.getNodesToUpdate()) {
            flowNodeRepository.update(node);
        }
        
        // 2.3 删除节点
        for (FlowNodeId nodeId : aggregate.getNodesToRemove()) {
            flowNodeRepository.delete(nodeId);
        }
        
        // 3. 清空变更追踪
        aggregate.clearChanges();
        
        log.debug("保存流程定义成功，ID: {}, 新增节点: {}, 更新节点: {}, 删除节点: {}", 
                aggregate.getId() != null ? aggregate.getId().getValue() : null,
                aggregate.getNodesToAdd().size(),
                aggregate.getNodesToUpdate().size(),
                aggregate.getNodesToRemove().size());
        
        return aggregate;
    }
    
    @Override
    @Transactional
    public void delete(FlowDefinitionId id) {
        if (id == null) {
            return;
        }
        
        // 1. 级联删除所有节点
        flowNodeRepository.deleteByFlowDefId(id);
        
        // 2. 删除流程定义
        flowDefinitionMapper.deleteById(id.getValue());
        
        log.debug("删除流程定义成功，ID: {}", id.getValue());
    }
    
    /**
     * 转换为持久化对象
     */
    private xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowDefinition toPO(FlowDefinition aggregate) {
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowDefinition po = 
                new xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowDefinition();
        if (aggregate.getId() != null) {
            po.setId(aggregate.getId().getValue());
        }
        po.setName(aggregate.getName());
        po.setCode(aggregate.getCode());
        po.setDocTypeId(aggregate.getDocTypeId());
        po.setDescription(aggregate.getDescription());
        po.setVersion(aggregate.getVersion());
        po.setStatus(aggregate.getStatus().getValue());
        po.setCreatorId(aggregate.getCreatorId());
        po.setCreatedAt(aggregate.getCreatedAt());
        po.setUpdatedAt(aggregate.getUpdatedAt() != null ? aggregate.getUpdatedAt() : LocalDateTime.now());
        return po;
    }
    
    /**
     * 从持久化对象重建聚合根
     */
    private FlowDefinition toAggregate(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowDefinition po) {
        FlowDefinition aggregate = FlowDefinition.reconstruct();
        if (po.getId() != null) {
            aggregate.setId(FlowDefinitionId.of(po.getId()));
        }
        aggregate.setName(po.getName());
        aggregate.setCode(po.getCode());
        aggregate.setDocTypeId(po.getDocTypeId());
        aggregate.setDescription(po.getDescription());
        aggregate.setVersion(po.getVersion());
        aggregate.setStatus(FlowDefinitionStatus.fromValue(po.getStatus()));
        aggregate.setCreatorId(po.getCreatorId());
        aggregate.setCreatedAt(po.getCreatedAt());
        aggregate.setUpdatedAt(po.getUpdatedAt());
        return aggregate;
    }
}

