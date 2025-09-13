package xtt.cloud.oa.platform.domain.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xtt.cloud.oa.platform.domain.entity.Permission;

import java.util.List;

@Mapper
public interface RolePermissionMapper {
    
    /**
     * 根据角色ID查询权限列表
     */
    List<Permission> selectPermissionsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据权限ID查询角色ID列表
     */
    List<Long> selectRoleIdsByPermissionId(@Param("permissionId") Long permissionId);
    
    /**
     * 插入角色权限关联
     */
    int insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
    
    /**
     * 删除角色的所有权限关联
     */
    int deleteRolePermissions(@Param("roleId") Long roleId);
    
    /**
     * 删除权限的所有角色关联
     */
    int deletePermissionRoles(@Param("permissionId") Long permissionId);
    
    /**
     * 批量插入角色权限关联
     */
    int insertRolePermissions(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);
}
