package xtt.cloud.oa.workflow.infrastructure.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xtt.cloud.oa.platform.application.UserService;
import xtt.cloud.oa.platform.application.DepartmentService;
import xtt.cloud.oa.platform.domain.entity.User;
import xtt.cloud.oa.platform.domain.entity.Department;
import xtt.cloud.oa.platform.domain.mapper.UserRoleMapper;
import xtt.cloud.oa.platform.domain.mapper.UserDepartmentMapper;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 平台用户服务适配器
 * 
 * 封装对 platform 模块服务的调用，提供统一的接口给领域服务使用
 * 
 * @author xtt
 */
@Component
public class PlatformUserServiceAdapter {
    
    private static final Logger log = LoggerFactory.getLogger(PlatformUserServiceAdapter.class);
    
    private final UserService userService;
    private final DepartmentService departmentService;
    private final UserRoleMapper userRoleMapper;
    private final UserDepartmentMapper userDepartmentMapper;
    
    public PlatformUserServiceAdapter(
            UserService userService,
            DepartmentService departmentService,
            UserRoleMapper userRoleMapper,
            UserDepartmentMapper userDepartmentMapper) {
        this.userService = userService;
        this.departmentService = departmentService;
        this.userRoleMapper = userRoleMapper;
        this.userDepartmentMapper = userDepartmentMapper;
    }
    
    /**
     * 根据用户ID获取用户信息
     */
    public Optional<User> getUserById(Long userId) {
        if (userId == null || userId <= 0) {
            return Optional.empty();
        }
        try {
            return userService.get(userId);
        } catch (Exception e) {
            log.error("获取用户信息失败，用户ID: {}", userId, e);
            return Optional.empty();
        }
    }
    
    /**
     * 根据用户ID列表批量获取用户信息
     */
    public List<User> getUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return userService.findByIds(userIds);
        } catch (Exception e) {
            log.error("批量获取用户信息失败，用户ID列表: {}", userIds, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 根据用户ID列表转换为审批人列表
     */
    public List<Approver> convertToApprovers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<User> users = getUsersByIds(userIds);
        return users.stream()
                .filter(user -> user.getStatus() != null && user.getStatus() == 1) // 只返回启用状态的用户
                .map(this::convertToApprover)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据角色ID获取该角色下的所有用户
     */
    public List<Approver> getUsersByRoleId(Long roleId) {
        if (roleId == null || roleId <= 0) {
            return new ArrayList<>();
        }
        
        try {
            // 1. 根据角色ID查询用户ID列表
            List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(roleId);
            if (userIds == null || userIds.isEmpty()) {
                log.debug("角色下没有用户，角色ID: {}", roleId);
                return new ArrayList<>();
            }
            
            // 2. 批量获取用户信息并转换为审批人
            return convertToApprovers(userIds);
        } catch (Exception e) {
            log.error("根据角色ID获取用户失败，角色ID: {}", roleId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 根据角色ID列表获取所有用户（去重）
     */
    public List<Approver> getUsersByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> allUserIds = new ArrayList<>();
        for (Long roleId : roleIds) {
            try {
                List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(roleId);
                if (userIds != null) {
                    allUserIds.addAll(userIds);
                }
            } catch (Exception e) {
                log.error("根据角色ID获取用户ID列表失败，角色ID: {}", roleId, e);
            }
        }
        
        // 去重
        allUserIds = allUserIds.stream().distinct().collect(Collectors.toList());
        
        // 转换为审批人
        return convertToApprovers(allUserIds);
    }
    
    /**
     * 根据部门ID获取部门负责人
     * 
     * 注意：由于部门实体中没有 leaderId 字段，通过以下方式查找负责人：
     * 1. 查找部门下具有"部门负责人"角色的用户（如果存在该角色）
     * 2. 如果没有找到，返回部门下的第一个用户（作为临时方案）
     * 
     * TODO: 根据实际业务规则调整负责人查找逻辑
     */
    public List<Approver> getDeptLeadersByDeptId(Long deptId) {
        if (deptId == null || deptId <= 0) {
            return new ArrayList<>();
        }
        
        try {
            // 1. 获取部门信息
            Optional<Department> deptOpt = departmentService.get(deptId);
            if (deptOpt.isEmpty()) {
                log.warn("部门不存在，部门ID: {}", deptId);
                return new ArrayList<>();
            }
            
            // 2. 获取部门下的所有用户ID
            List<Long> userIds = userDepartmentMapper.selectUserIdsByDepartmentId(deptId);
            if (userIds == null || userIds.isEmpty()) {
                log.debug("部门下没有用户，部门ID: {}", deptId);
                return new ArrayList<>();
            }
            
            // 3. 获取用户信息
            List<User> users = getUsersByIds(userIds);
            if (users.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 4. 尝试查找具有"部门负责人"角色的用户
            // TODO: 如果系统中有"部门负责人"角色，可以通过角色代码查找
            // String deptLeaderRoleCode = "DEPT_LEADER";
            // Optional<Role> leaderRoleOpt = roleService.findByCode(deptLeaderRoleCode);
            // if (leaderRoleOpt.isPresent()) {
            //     Long leaderRoleId = leaderRoleOpt.get().getId();
            //     List<Long> leaderUserIds = userRoleMapper.selectUserIdsByRoleId(leaderRoleId);
            //     // 取交集：既是部门用户，又有负责人角色
            //     List<Long> deptLeaderIds = userIds.stream()
            //             .filter(leaderUserIds::contains)
            //             .collect(Collectors.toList());
            //     if (!deptLeaderIds.isEmpty()) {
            //         return convertToApprovers(deptLeaderIds);
            //     }
            // }
            
            // 5. 如果没有找到负责人角色，返回部门下的第一个用户（作为临时方案）
            // 实际业务中可能需要返回部门管理员或根据其他规则确定
            User firstUser = users.get(0);
            Approver leader = convertToApprover(firstUser);
            log.debug("未找到部门负责人角色，返回部门下第一个用户作为负责人，部门ID: {}, 用户ID: {}", 
                    deptId, firstUser.getId());
            
            return List.of(leader);
        } catch (Exception e) {
            log.error("获取部门负责人失败，部门ID: {}", deptId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 根据部门ID列表获取所有部门负责人（去重）
     */
    public List<Approver> getDeptLeadersByDeptIds(List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Approver> allLeaders = new ArrayList<>();
        for (Long deptId : deptIds) {
            List<Approver> leaders = getDeptLeadersByDeptId(deptId);
            allLeaders.addAll(leaders);
        }
        
        // 去重（根据用户ID）
        return allLeaders.stream()
                .collect(Collectors.toMap(
                        Approver::getUserId,
                        approver -> approver,
                        (existing, replacement) -> existing))
                .values()
                .stream()
                .collect(Collectors.toList());
    }
    
    /**
     * 将 User 实体转换为 Approver 值对象
     */
    private Approver convertToApprover(User user) {
        if (user == null) {
            return null;
        }
        
        // 获取用户的主要部门（如果有多个部门，取第一个）
        String deptName = null;
        Long deptId = null;
        if (user.getDepartments() != null && !user.getDepartments().isEmpty()) {
            Department firstDept = user.getDepartments().iterator().next();
            deptId = firstDept.getId();
            deptName = firstDept.getName();
        }
        
        return new Approver(
                user.getId(),
                deptId,
                user.getNickname() != null ? user.getNickname() : user.getUsername(),
                deptName
        );
    }
}

