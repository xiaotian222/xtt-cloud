package xtt.cloud.oa.workflow.domain.flow.model.valueobject;

import java.util.Objects;

/**
 * 审批人值对象
 *  用户，部门，角色，用户组，子流程。
 *
 * 
 * @author xtt
 */
public class Approver {
    
    private final Long userId;
    private final Long deptId;
    private final String userName;
    private final String deptName;
    
    public Approver(Long userId, Long deptId) {
        this(userId, deptId, null, null);
    }
    
    public Approver(Long userId, Long deptId, String userName, String deptName) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Approver user ID must be a positive number");
        }
        this.userId = userId;
        this.deptId = deptId;
        this.userName = userName;
        this.deptName = deptName;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public Long getDeptId() {
        return deptId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public String getDeptName() {
        return deptName;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Approver approver = (Approver) o;
        return Objects.equals(userId, approver.userId) &&
               Objects.equals(deptId, approver.deptId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, deptId);
    }
    
    @Override
    public String toString() {
        return "Approver{" +
                "userId=" + userId +
                ", deptId=" + deptId +
                ", userName='" + userName + '\'' +
                ", deptName='" + deptName + '\'' +
                '}';
    }
}

