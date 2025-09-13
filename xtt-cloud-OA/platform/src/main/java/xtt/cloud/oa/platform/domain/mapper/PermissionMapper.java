package xtt.cloud.oa.platform.domain.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xtt.cloud.oa.platform.domain.entity.Permission;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PermissionMapper {
    
    /**
     * 根据ID查询权限
     */
    Permission selectById(@Param("id") Long id);
    
    /**
     * 根据代码查询权限
     */
    Permission selectByCode(@Param("code") String code);
    
    /**
     * 根据代码列表查询权限
     */
    List<Permission> selectByCodes(@Param("codes") List<String> codes);
    
    /**
     * 根据ID列表查询权限
     */
    List<Permission> selectByIds(@Param("ids") List<Long> ids);
    
    /**
     * 根据类型查询权限
     */
    List<Permission> selectByType(@Param("type") String type);
    
    /**
     * 查询所有权限
     */
    List<Permission> selectAll();
    
    /**
     * 插入权限
     */
    int insert(Permission permission);
    
    /**
     * 更新权限
     */
    int update(Permission permission);
    
    /**
     * 根据ID删除权限
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据代码查询权限（返回Optional）
     */
    default Optional<Permission> findByCode(String code) {
        Permission permission = selectByCode(code);
        return Optional.ofNullable(permission);
    }
    
    /**
     * 根据ID查询权限（返回Optional）
     */
    default Optional<Permission> findById(Long id) {
        Permission permission = selectById(id);
        return Optional.ofNullable(permission);
    }
}
