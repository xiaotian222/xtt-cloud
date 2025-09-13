package xtt.cloud.oa.platform.interfaces.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import xtt.cloud.oa.common.dto.DeptInfoDto;
import xtt.cloud.oa.common.dto.RoleInfoDto;
import xtt.cloud.oa.common.dto.UserInfoDto;
import xtt.cloud.oa.platform.domain.entity.Department;
import xtt.cloud.oa.platform.domain.entity.Role;
import xtt.cloud.oa.platform.domain.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户映射器
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Component("userDtoMapper")
public class UserMapper {
    
    private final RoleMapper roleMapper;
    
    @Autowired
    public UserMapper(@Qualifier("roleDtoMapper") RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    public UserInfoDto toUserInfoDto(User user) {
        if (user == null) {
            return null;
        }
        
        UserInfoDto dto = new UserInfoDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        // 映射角色信息
        dto.setRoles(toRoleInfoDtos(user.getRoles()));
        
        // 映射部门信息
        dto.setDepartments(toDeptInfoDtos(user.getDepartments()));
        
        return dto;
    }

    public Set<RoleInfoDto> toRoleInfoDtos(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        
        return roles.stream()
                .map(roleMapper::toRoleInfoDto)
                .collect(Collectors.toSet());
    }

    public Set<DeptInfoDto> toDeptInfoDtos(Set<Department> departments) {
        if (departments == null) {
            return null;
        }
        
        return departments.stream()
                .map(this::toDeptInfoDto)
                .collect(Collectors.toSet());
    }

    private DeptInfoDto toDeptInfoDto(Department dept) {
        if (dept == null) {
            return null;
        }
        
        DeptInfoDto dto = new DeptInfoDto();
        dto.setId(dept.getId());
        dto.setParentId(dept.getParentId());
        dto.setName(dept.getName());
        dto.setSortNo(dept.getSortNo());
        dto.setCreatedAt(dept.getCreatedAt());
        dto.setUpdatedAt(dept.getUpdatedAt());
        
        return dto;
    }
}
