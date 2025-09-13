package xtt.cloud.oa.platform.domain.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xtt.cloud.oa.platform.domain.entity.Role;

import java.util.List;

@Mapper
public interface UserRoleMapper {
    
    /**
     * 根据用户ID查询角色列表
     */
    List<Role> selectRolesByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色ID查询用户ID列表
     */
    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 插入用户角色关联
     */
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    /**
     * 删除用户的所有角色关联
     */
    int deleteUserRoles(@Param("userId") Long userId);
    
    /**
     * 删除角色的所有用户关联
     */
    int deleteRoleUsers(@Param("roleId") Long roleId);
    
    /**
     * 批量插入用户角色关联
     */
    int insertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
}
