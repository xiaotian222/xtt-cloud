package xtt.cloud.oa.workflow.domain.flow.model.entity;

/**
 * 动作规则实体（领域实体）
 * 
 * 定义在什么条件下可以使用某个发送动作
 * 
 * 注意：此实体引用现有的 FlowActionRule PO
 * 
 * @author xtt
 */
public class FlowActionRule {
    
    // 文档状态常量
    public static final int DOCUMENT_STATUS_DRAFT = 0;      // 草稿
    public static final int DOCUMENT_STATUS_REVIEWING = 1;   // 审核中
    public static final int DOCUMENT_STATUS_PUBLISHED = 2;   // 已发布
    public static final int DOCUMENT_STATUS_ARCHIVED = 3;    // 已归档
    
    private final xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowActionRule po;
    
    public FlowActionRule(xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowActionRule po) {
        this.po = po;
    }
    
    /**
     * 判断规则是否启用
     */
    public boolean isEnabled() {
        return po != null && po.getEnabled() != null && po.getEnabled() == 1;
    }
    
    /**
     * 判断规则是否匹配文档状态
     */
    public boolean matchesDocumentStatus(Integer documentStatus) {
        if (po == null || documentStatus == null) {
            return false;
        }
        return po.getDocumentStatus() != null && po.getDocumentStatus().equals(documentStatus);
    }
    
    /**
     * 判断规则是否匹配用户角色
     * 
     * @param userRoles 用户角色列表
     * @return 是否匹配
     */
    public boolean matchesUserRoles(java.util.List<String> userRoles) {
        if (po == null || userRoles == null || userRoles.isEmpty()) {
            return false;
        }
        
        String ruleRole = po.getUserRole();
        if (ruleRole == null || ruleRole.trim().isEmpty()) {
            return false;
        }
        
        // 如果规则角色是 "*"，表示所有角色都匹配
        if ("*".equals(ruleRole.trim())) {
            return true;
        }
        
        // 检查用户角色是否在规则角色列表中（支持逗号分隔）
        String[] ruleRoles = ruleRole.split(",");
        for (String role : ruleRoles) {
            if (userRoles.contains(role.trim())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 判断规则是否匹配部门
     */
    public boolean matchesDepartment(Long deptId) {
        if (po == null) {
            return false;
        }
        // 如果规则没有指定部门，则匹配所有部门
        if (po.getDeptId() == null) {
            return true;
        }
        // 如果规则指定了部门，则必须匹配
        return po.getDeptId().equals(deptId);
    }
    
    // 委托方法
    public Long getId() {
        return po != null ? po.getId() : null;
    }
    
    public Long getActionId() {
        return po != null ? po.getActionId() : null;
    }
    
    public Integer getDocumentStatus() {
        return po != null ? po.getDocumentStatus() : null;
    }
    
    public String getUserRole() {
        return po != null ? po.getUserRole() : null;
    }
    
    public Long getDeptId() {
        return po != null ? po.getDeptId() : null;
    }
    
    public Integer getPriority() {
        return po != null ? po.getPriority() : null;
    }
    
    /**
     * 获取持久化对象
     */
    public xtt.cloud.oa.workflow.infrastructure.persistence.pojo.FlowActionRule getPO() {
        return po;
    }
}

