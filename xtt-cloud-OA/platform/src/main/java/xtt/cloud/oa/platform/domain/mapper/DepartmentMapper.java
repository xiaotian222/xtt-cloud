package xtt.cloud.oa.platform.domain.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xtt.cloud.oa.platform.domain.entity.Department;

import java.util.List;
import java.util.Optional;

@Mapper
public interface DepartmentMapper {
    
    /**
     * 根据ID查询部门
     */
    Department selectById(@Param("id") Long id);
    
    /**
     * 根据父ID查询部门列表
     */
    List<Department> selectByParentId(@Param("parentId") Long parentId);
    
    /**
     * 根据ID列表查询部门
     */
    List<Department> selectByIds(@Param("ids") List<Long> ids);
    
    /**
     * 查询所有部门
     */
    List<Department> selectAll();
    
    /**
     * 插入部门
     */
    int insert(Department department);
    
    /**
     * 更新部门
     */
    int update(Department department);
    
    /**
     * 根据ID删除部门
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据ID查询部门（返回Optional）
     */
    default Optional<Department> findById(Long id) {
        Department department = selectById(id);
        return Optional.ofNullable(department);
    }
}
