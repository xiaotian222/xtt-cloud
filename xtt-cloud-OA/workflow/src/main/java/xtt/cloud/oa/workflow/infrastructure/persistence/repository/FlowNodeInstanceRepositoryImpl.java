package xtt.cloud.oa.workflow.infrastructure.persistence.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNodeInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.NodeStatus;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowNodeInstanceRepository;
import xtt.cloud.oa.workflow.infrastructure.persistence.converter.FlowNodeInstanceConverter;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.FlowNodeInstanceMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 节点实例仓储实现
 * 
 * @author xtt
 */
@Repository
public class FlowNodeInstanceRepositoryImpl implements FlowNodeInstanceRepository {
    
    private static final Logger log = LoggerFactory.getLogger(FlowNodeInstanceRepositoryImpl.class);
    
    private final FlowNodeInstanceMapper flowNodeInstanceMapper;
    private final FlowNodeInstanceConverter converter;
    
    public FlowNodeInstanceRepositoryImpl(FlowNodeInstanceMapper flowNodeInstanceMapper) {
        this.flowNodeInstanceMapper = flowNodeInstanceMapper;
        this.converter = new FlowNodeInstanceConverter();
    }
    
    @Override
    @Transactional
    public FlowNodeInstance save(FlowNodeInstance nodeInstance) {
        if (nodeInstance == null) {
            throw new IllegalArgumentException("Node instance cannot be null");
        }
        
        // 转换为PO
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNodeInstance po = converter.toPO(nodeInstance);
        
        if (po.getId() == null) {
            // 新增
            flowNodeInstanceMapper.insert(po);
            // 设置ID
            if (nodeInstance.getId() == null && po.getId() != null) {
                nodeInstance.setId(po.getId());
            }
        } else {
            // 更新
            flowNodeInstanceMapper.updateById(po);
        }
        
        log.debug("保存节点实例成功，ID: {}", nodeInstance.getId());
        return nodeInstance;
    }
    
    @Override
    @Transactional
    public List<FlowNodeInstance> saveAll(List<FlowNodeInstance> nodeInstances) {
        if (nodeInstances == null || nodeInstances.isEmpty()) {
            return List.of();
        }
        
        return nodeInstances.stream()
                .map(this::save)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<FlowNodeInstance> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNodeInstance po = 
                flowNodeInstanceMapper.selectById(id);
        if (po == null) {
            return Optional.empty();
        }
        
        FlowNodeInstance entity = converter.toEntity(po);
        return Optional.ofNullable(entity);
    }
    
    @Override
    public List<FlowNodeInstance> findByFlowInstanceId(Long flowInstanceId) {
        if (flowInstanceId == null) {
            return List.of();
        }
        
        List<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNodeInstance> pos = 
                flowNodeInstanceMapper.selectByFlowInstanceId(flowInstanceId);
        if (pos == null || pos.isEmpty()) {
            return List.of();
        }
        
        return pos.stream()
                .map(converter::toEntity)
                .filter(entity -> entity != null)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<FlowNodeInstance> findByNodeIdAndFlowInstanceId(Long nodeId, Long flowInstanceId) {
        if (nodeId == null || flowInstanceId == null) {
            return List.of();
        }
        
        List<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNodeInstance> pos = 
                flowNodeInstanceMapper.selectByNodeIdAndFlowInstanceId(nodeId, flowInstanceId);
        if (pos == null || pos.isEmpty()) {
            return List.of();
        }
        
        return pos.stream()
                .map(converter::toEntity)
                .filter(entity -> entity != null)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<FlowNodeInstance> findPendingByApproverId(Long approverId) {
        if (approverId == null) {
            return List.of();
        }
        
        List<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowNodeInstance> pos = 
                flowNodeInstanceMapper.selectPendingByApproverId(approverId);
        if (pos == null || pos.isEmpty()) {
            return List.of();
        }
        
        return pos.stream()
                .map(converter::toEntity)
                .filter(entity -> entity != null)
                .filter(entity -> {
                    // 确保状态是待处理
                    NodeStatus status = entity.getStatus();
                    return status != null && status.isPending();
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void delete(FlowNodeInstance nodeInstance) {
        if (nodeInstance == null || nodeInstance.getId() == null) {
            return;
        }
        
        flowNodeInstanceMapper.deleteById(nodeInstance.getId());
        log.debug("删除节点实例成功，ID: {}", nodeInstance.getId());
    }
    
    @Override
    @Transactional
    public void deleteByFlowInstanceId(Long flowInstanceId) {
        if (flowInstanceId == null) {
            return;
        }
        
        flowNodeInstanceMapper.deleteByFlowInstanceId(flowInstanceId);
        log.debug("删除流程实例的所有节点实例成功，流程实例ID: {}", flowInstanceId);
    }
}

