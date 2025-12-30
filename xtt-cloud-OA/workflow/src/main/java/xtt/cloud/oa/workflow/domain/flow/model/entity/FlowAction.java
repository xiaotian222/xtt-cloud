package xtt.cloud.oa.workflow.domain.flow.model.entity;

/**
 * 流程动作实体（领域实体）
 * 
 * 注意：此实体引用现有的 FlowAction PO
 * 
 * @author xtt
 */
public class FlowAction {
    
    // 动作类型常量
    public static final int TYPE_UNIT_HANDLE = 1;    // 单位内办理
    public static final int TYPE_REVIEW = 2;          // 核稿
    public static final int TYPE_EXTERNAL = 3;        // 转外单位办理
    public static final int TYPE_RETURN = 4;          // 返回
    
    private final xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowAction po;
    
    public FlowAction(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowAction po) {
        this.po = po;
    }
    
    /**
     * 判断动作是否启用
     */
    public boolean isEnabled() {
        return po != null && po.getEnabled() != null && po.getEnabled() == 1;
    }
    
    // 委托方法
    public Long getId() {
        return po != null ? po.getId() : null;
    }
    
    public String getActionCode() {
        return po != null ? po.getActionCode() : null;
    }
    
    public String getActionName() {
        return po != null ? po.getActionName() : null;
    }
    
    public Integer getActionType() {
        return po != null ? po.getActionType() : null;
    }
    
    public String getDescription() {
        return po != null ? po.getDescription() : null;
    }
    
    public String getIcon() {
        return po != null ? po.getIcon() : null;
    }
    
    /**
     * 获取持久化对象
     */
    public xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowAction getPO() {
        return po;
    }
}
