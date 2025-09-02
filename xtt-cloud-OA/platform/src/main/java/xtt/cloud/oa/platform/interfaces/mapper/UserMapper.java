package xtt.cloud.oa.platform.interfaces.mapper;

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
@Component
public class UserMapper {

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
        
        return dto;
    }

    public Set<RoleInfoDto> toRoleInfoDtos(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        
        return roles.stream()
                .map(this::toRoleInfoDto)
                .collect(Collectors.toSet());
    }

    private RoleInfoDto toRoleInfoDto(Role role) {
        if (role == null) {
            return null;
        }
        
        RoleInfoDto dto = new RoleInfoDto();
        dto.setId(role.getId());
        dto.setCode(role.getCode());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        
        return dto;
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
