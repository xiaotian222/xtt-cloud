package xtt.cloud.oa.platform.domain.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xtt.cloud.oa.platform.domain.entity.Role;

import java.util.List;
import java.util.Optional;

@Mapper
public interface RoleMapper {
    
    /**
     * 根据ID查询角色
     */
    Role selectById(@Param("id") Long id);
    
    /**
     * 根据代码查询角色
     */
    Role selectByCode(@Param("code") String code);
    
    /**
     * 根据代码列表查询角色
     */
    List<Role> selectByCodes(@Param("codes") List<String> codes);
    
    /**
     * 根据ID列表查询角色
     */
    List<Role> selectByIds(@Param("ids") List<Long> ids);
    
    /**
     * 查询所有角色
     */
    List<Role> selectAll();
    
    /**
     * 插入角色
     */
    int insert(Role role);
    
    /**
     * 更新角色
     */
    int update(Role role);
    
    /**
     * 根据ID删除角色
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据代码查询角色（返回Optional）
     */
    default Optional<Role> findByCode(String code) {
        Role role = selectByCode(code);
        return Optional.ofNullable(role);
    }
    
    /**
     * 根据ID查询角色（返回Optional）
     */
    default Optional<Role> findById(Long id) {
        Role role = selectById(id);
        return Optional.ofNullable(role);
    }
}
