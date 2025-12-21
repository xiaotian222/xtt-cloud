package xtt.cloud.oa.workflow.infrastructure.persistence.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.workflow.domain.flow.model.aggregate.FlowInstance;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowInstanceId;
import xtt.cloud.oa.workflow.domain.flow.repository.FlowInstanceRepository;
import xtt.cloud.oa.workflow.infrastructure.persistence.converter.FlowInstanceConverter;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.FlowInstanceMapper;
import xtt.cloud.oa.workflow.infrastructure.persistence.mapper.FlowNodeInstanceMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 流程实例仓储实现
 * 
 * 使用 MyBatis Plus 实现数据持久化
 * 
 * @author xtt
 */
@Repository
public class FlowInstanceRepositoryImpl implements FlowInstanceRepository {
    
    private static final Logger log = LoggerFactory.getLogger(FlowInstanceRepositoryImpl.class);
    
    private final FlowInstanceMapper flowInstanceMapper;
    private final FlowNodeInstanceMapper flowNodeInstanceMapper;
    private final FlowInstanceConverter converter;
    
    public FlowInstanceRepositoryImpl(
            FlowInstanceMapper flowInstanceMapper,
            FlowNodeInstanceMapper flowNodeInstanceMapper) {
        this.flowInstanceMapper = flowInstanceMapper;
        this.flowNodeInstanceMapper = flowNodeInstanceMapper;
        this.converter = new FlowInstanceConverter(flowNodeInstanceMapper);
    }
    
    @Override
    @Transactional
    public FlowInstance save(FlowInstance aggregate) {
        if (aggregate == null) {
            throw new IllegalArgumentException("Flow instance aggregate cannot be null");
        }
        
        // 转换为PO
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowInstance po = converter.toPO(aggregate);
        
        if (po.getId() == null) {
            // 新增
            flowInstanceMapper.insert(po);
            // 设置ID
            if (aggregate.getId() == null && po.getId() != null) {
                aggregate.setId(FlowInstanceId.of(po.getId()));
            }
        } else {
            // 更新
            flowInstanceMapper.updateById(po);
        }
        
        // 保存节点实例集合
        if (aggregate.getNodeInstances() != null) {
            // TODO: 保存节点实例
            // for (FlowNodeInstance nodeInstance : aggregate.getNodeInstances()) {
            //     saveNodeInstance(nodeInstance);
            // }
        }
        
        // 保存流程变量
        // TODO: 保存流程变量到扩展表或JSON字段
        
        log.debug("保存流程实例成功，ID: {}", aggregate.getId() != null ? aggregate.getId().getValue() : null);
        return aggregate;
    }
    
    @Override
    public Optional<FlowInstance> findById(FlowInstanceId id) {
        if (id == null) {
            return Optional.empty();
        }
        return findById(id.getValue());
    }
    
    @Override
    public Optional<FlowInstance> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowInstance po = flowInstanceMapper.selectById(id);
        if (po == null) {
            return Optional.empty();
        }
        
        FlowInstance aggregate = converter.toAggregate(po);
        return Optional.of(aggregate);
    }
    
    @Override
    public Optional<FlowInstance> findByDocumentId(Long documentId) {
        if (documentId == null) {
            return Optional.empty();
        }
        
        xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowInstance po = flowInstanceMapper.selectByDocumentId(documentId);
        if (po == null) {
            return Optional.empty();
        }
        
        FlowInstance aggregate = converter.toAggregate(po);
        return Optional.of(aggregate);
    }
    
    @Override
    public List<FlowInstance> findByStatus(Integer status) {
        if (status == null) {
            return List.of();
        }
        
        List<xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowInstance> pos = flowInstanceMapper.selectByStatus(status);
        if (pos == null || pos.isEmpty()) {
            return List.of();
        }
        return pos.stream()
                .map(converter::toAggregate)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void delete(FlowInstance aggregate) {
        if (aggregate == null || aggregate.getId() == null) {
            return;
        }
        
        Long id = aggregate.getId().getValue();
        
        // 删除节点实例
        flowNodeInstanceMapper.deleteByFlowInstanceId(id);
        
        // 删除流程实例
        flowInstanceMapper.deleteById(id);
        
        log.debug("删除流程实例成功，ID: {}", id);
    }
    
    @Override
    public boolean existsById(FlowInstanceId id) {
        if (id == null) {
            return false;
        }
        return flowInstanceMapper.selectById(id.getValue()) != null;
    }
}

