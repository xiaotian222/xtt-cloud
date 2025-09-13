package xtt.cloud.oa.platform.domain.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xtt.cloud.oa.platform.domain.entity.Department;

import java.util.List;

@Mapper
public interface UserDepartmentMapper {
    
    /**
     * 根据用户ID查询部门列表
     */
    List<Department> selectDepartmentsByUserId(@Param("userId") Long userId);
    
    /**
     * 根据部门ID查询用户ID列表
     */
    List<Long> selectUserIdsByDepartmentId(@Param("departmentId") Long departmentId);
    
    /**
     * 插入用户部门关联
     */
    int insertUserDepartment(@Param("userId") Long userId, @Param("departmentId") Long departmentId);
    
    /**
     * 删除用户的所有部门关联
     */
    int deleteUserDepartments(@Param("userId") Long userId);
    
    /**
     * 删除部门的所有用户关联
     */
    int deleteDepartmentUsers(@Param("departmentId") Long departmentId);
    
    /**
     * 批量插入用户部门关联
     */
    int insertUserDepartments(@Param("userId") Long userId, @Param("departmentIds") List<Long> departmentIds);
}
