package xtt.cloud.oa.common.mapper;

import org.springframework.stereotype.Component;
import xtt.cloud.oa.common.dto.DeptInfoDto;
import xtt.cloud.oa.common.dto.RoleInfoDto;
import xtt.cloud.oa.common.dto.UserInfoDto;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户映射器 - 通用映射器
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Component
public class UserMapper {

    public UserInfoDto toUserInfoDto(Object user) {
        // 这里使用反射或者具体的实现类来处理
        // 由于 common 包不应该依赖具体的实体类，这里提供一个抽象接口
        return null;
    }

    public Set<RoleInfoDto> toRoleInfoDtos(Set<?> roles) {
        if (roles == null) {
            return null;
        }
        
        return roles.stream()
                .map(this::toRoleInfoDto)
                .collect(Collectors.toSet());
    }

    private RoleInfoDto toRoleInfoDto(Object role) {
        // 具体实现由各个服务提供
        return null;
    }

    public Set<DeptInfoDto> toDeptInfoDtos(Set<?> departments) {
        if (departments == null) {
            return null;
        }
        
        return departments.stream()
                .map(this::toDeptInfoDto)
                .collect(Collectors.toSet());
    }

    private DeptInfoDto toDeptInfoDto(Object dept) {
        // 具体实现由各个服务提供
        return null;
    }
}
