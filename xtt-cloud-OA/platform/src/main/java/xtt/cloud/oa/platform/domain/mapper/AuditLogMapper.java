package xtt.cloud.oa.platform.domain.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xtt.cloud.oa.platform.infrastructure.audit.AuditLog;

import java.util.List;

@Mapper
public interface AuditLogMapper {
    
    /**
     * 插入审计日志
     */
    int insert(AuditLog auditLog);
    
    /**
     * 根据ID查询审计日志
     */
    AuditLog selectById(@Param("id") Long id);
    
    /**
     * 查询所有审计日志
     */
    List<AuditLog> selectAll();
    
    /**
     * 根据用户名查询审计日志
     */
    List<AuditLog> selectByUsername(@Param("username") String username);
}
