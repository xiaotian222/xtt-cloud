package xtt.cloud.oa.platform.domain.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xtt.cloud.oa.platform.domain.entity.User;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {
    
    /**
     * 根据ID查询用户
     */
    User selectById(@Param("id") Long id);
    
    /**
     * 根据用户名查询用户
     */
    User selectByUsername(@Param("username") String username);
    
    /**
     * 根据用户名列表查询用户
     */
    List<User> selectByUsernames(@Param("usernames") List<String> usernames);
    
    /**
     * 根据ID列表查询用户
     */
    List<User> selectByIds(@Param("ids") List<Long> ids);
    
    /**
     * 查询所有用户
     */
    List<User> selectAll();
    
    /**
     * 插入用户
     */
    int insert(User user);
    
    /**
     * 更新用户
     */
    int update(User user);
    
    /**
     * 根据ID删除用户
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据用户名查询用户（返回Optional）
     */
    default Optional<User> findByUsername(String username) {
        User user = selectByUsername(username);
        return Optional.ofNullable(user);
    }
    
    /**
     * 根据ID查询用户（返回Optional）
     */
    default Optional<User> findById(Long id) {
        User user = selectById(id);
        return Optional.ofNullable(user);
    }
}
