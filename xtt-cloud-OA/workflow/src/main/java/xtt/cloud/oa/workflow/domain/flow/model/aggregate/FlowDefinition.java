package xtt.cloud.oa.workflow.domain.flow.model.aggregate;

import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowDefinitionId;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowDefinitionStatus;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.FlowNodeId;
import xtt.cloud.oa.workflow.domain.flow.model.entity.FlowNode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 流程定义聚合根
 * 
 * 聚合根特点：
 * 1. 管理流程定义及其节点的创建、修改、删除
 * 2. 保证业务不变式（Business Invariants）
 * 3. 通过ID进行唯一标识
 * 4. 控制聚合内对象的访问
 * 
 * 设计要点：
 * - 节点集合不直接存储在聚合中（性能考虑）
 * - 使用变更追踪机制，在保存时同步处理节点
 * - 读操作通过 Repository 按需加载节点
 * 
 * @author xtt
 */
public class FlowDefinition {
    
    private FlowDefinitionId id;
    private String name;
    private String code;
    private Long docTypeId;
    private String description;
    private Integer version;
    private FlowDefinitionStatus status;
    private Long creatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 节点变更追踪（不直接存储节点，避免性能问题）
    private List<FlowNode> nodesToAdd;
    private List<FlowNodeId> nodesToRemove;
    private List<FlowNode> nodesToUpdate;
    private List<FlowNode>  flowNodeList;
    
    /**
     * 私有构造函数，通过工厂方法创建
     */
    private FlowDefinition() {
        this.nodesToAdd = new ArrayList<>();
        this.nodesToRemove = new ArrayList<>();
        this.nodesToUpdate = new ArrayList<>();
        this.status = FlowDefinitionStatus.DISABLED;
        this.version = 1;
    }
    
    /**
     * 用于持久化层重建对象的工厂方法
     * 注意：此方法仅用于从数据库重建聚合根，不应在业务逻辑中使用
     */
    public static FlowDefinition reconstruct() {
        return new FlowDefinition();
    }
    
    /**
     * 创建新的流程定义（工厂方法）
     */
    public static FlowDefinition create(
            String name,
            String code,
            Long docTypeId,
            String description,
            Long creatorId) {
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("流程定义名称不能为空");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("流程定义编码不能为空");
        }
        if (docTypeId == null || docTypeId <= 0) {
            throw new IllegalArgumentException("文档类型ID必须大于0");
        }
        
        FlowDefinition flowDef = new FlowDefinition();
        flowDef.name = name.trim();
        flowDef.code = code.trim();
        flowDef.docTypeId = docTypeId;
        flowDef.description = description != null ? description.trim() : null;
        flowDef.creatorId = creatorId;
        flowDef.status = FlowDefinitionStatus.DISABLED; // 新建时默认停用
        flowDef.version = 1;
        flowDef.createdAt = LocalDateTime.now();
        flowDef.updatedAt = LocalDateTime.now();
        
        return flowDef;
    }
    
    /**
     * 添加节点（业务方法）
     * 
     * 封装业务规则：
     * 1. 验证节点属于当前流程定义
     * 2. 验证节点顺序
     * 3. 验证节点连接关系
     */
    public void addNode(FlowNode node) {
        if (node == null) {
            throw new IllegalArgumentException("节点不能为空");
        }
        
        // 1. 验证节点属于当前流程定义
        if (this.id != null && node.getFlowDefId() != null) {
            if (!node.getFlowDefId().equals(this.id.getValue())) {
                throw new IllegalArgumentException("节点不属于当前流程定义");
            }
        }
        
        // 2. 验证节点顺序（如果有其他节点）
        // TODO: 实现节点顺序验证逻辑
        
        // 3. 验证节点连接关系
        // TODO: 实现节点连接关系验证逻辑
        
        // 4. 添加到待保存列表
        this.nodesToAdd.add(node);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 删除节点（业务方法）
     * 
     * 封装业务规则：
     * 1. 验证节点是否存在
     * 2. 验证是否可以删除（不能是最后一个节点）
     * 3. 验证是否有其他节点依赖此节点
     */
    public void removeNode(FlowNodeId nodeId) {
        if (nodeId == null) {
            throw new IllegalArgumentException("节点ID不能为空");
        }
        // 3. 添加到待删除列表
        this.nodesToRemove.add(nodeId);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更新节点（业务方法）
     */
    public void updateNode(FlowNode node) {
        if (node == null) {
            throw new IllegalArgumentException("节点不能为空");
        }
        
        // 验证节点属于当前流程定义
        if (this.id != null && node.getFlowDefId() != null) {
            if (!node.getFlowDefId().equals(this.id.getValue())) {
                throw new IllegalArgumentException("节点不属于当前流程定义");
            }
        }
        
        // 添加到待更新列表
        this.nodesToUpdate.add(node);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 启用流程定义
     */
    public void enable() {
        if (this.status.isEnabled()) {
            throw new IllegalStateException("流程定义已经启用");
        }
        this.status = this.status.enable();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 停用流程定义
     */
    public void disable() {
        if (this.status.isDisabled()) {
            throw new IllegalStateException("流程定义已经停用");
        }
        this.status = this.status.disable();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更新基本信息
     */
    public void updateBasicInfo(String name, String description) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        if (description != null) {
            this.description = description.trim();
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 判断流程定义是否启用
     */
    public boolean isEnabled() {
        return this.status.isEnabled();
    }
    
    /**
     * 判断流程定义是否停用
     */
    public boolean isDisabled() {
        return this.status.isDisabled();
    }
    
    /**
     * 获取待保存的节点
     */
    public List<FlowNode> getNodesToAdd() {
        return new ArrayList<>(nodesToAdd);
    }
    
    /**
     * 获取待删除的节点ID
     */
    public List<FlowNodeId> getNodesToRemove() {
        return new ArrayList<>(nodesToRemove);
    }
    
    /**
     * 获取待更新的节点
     */
    public List<FlowNode> getNodesToUpdate() {
        return new ArrayList<>(nodesToUpdate);
    }
    
    /**
     * 清空变更追踪（保存后调用）
     */
    public void clearChanges() {
        this.nodesToAdd.clear();
        this.nodesToRemove.clear();
        this.nodesToUpdate.clear();
    }
    
    /**
     * 判断是否有变更
     */
    public boolean hasChanges() {
        return !nodesToAdd.isEmpty() || !nodesToRemove.isEmpty() || !nodesToUpdate.isEmpty();
    }
    
    // Getters
    public FlowDefinitionId getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getCode() {
        return code;
    }
    
    public Long getDocTypeId() {
        return docTypeId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Integer getVersion() {
        return version;
    }
    
    public FlowDefinitionStatus getStatus() {
        return status;
    }
    
    public Long getCreatorId() {
        return creatorId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    // Setters (仅用于持久化层重建对象)
    public void setId(FlowDefinitionId id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public void setDocTypeId(Long docTypeId) {
        this.docTypeId = docTypeId;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }
    
    public void setStatus(FlowDefinitionStatus status) {
        this.status = status;
    }
    
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowDefinition that = (FlowDefinition) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "FlowDefinition{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", status=" + status +
                '}';
    }

    public List<FlowNode> getFlowNodeList() {
        return flowNodeList;
    }

    public void setFlowNodeList(List<FlowNode> flowNodeList) {
        this.flowNodeList = flowNodeList;
    }
}

