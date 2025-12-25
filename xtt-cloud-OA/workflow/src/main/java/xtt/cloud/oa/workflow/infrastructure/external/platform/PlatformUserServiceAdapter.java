package xtt.cloud.oa.workflow.infrastructure.external.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xtt.cloud.oa.common.dto.DeptInfoDto;
import xtt.cloud.oa.common.dto.UserInfoDto;
import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;
import xtt.cloud.oa.workflow.domain.flow.service.ApproverProvider;
import xtt.cloud.oa.workflow.infrastructure.cache.org.DepartmentCacheService;
import xtt.cloud.oa.workflow.infrastructure.cache.org.RoleCacheService;
import xtt.cloud.oa.workflow.infrastructure.cache.org.UserCacheService;
import xtt.cloud.oa.workflow.infrastructure.external.client.PlatformFeignClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 平台用户服务适配器
 * 
 * 实现 ApproverProvider 接口，封装对 platform 模块服务的调用
 * 提供统一的接口给领域服务使用
 * 
 * @author xtt
 */
@Component
public class PlatformUserServiceAdapter implements ApproverProvider {
    
    private static final Logger log = LoggerFactory.getLogger(PlatformUserServiceAdapter.class);
    
    private final PlatformFeignClient platformFeignClient;
    private final UserCacheService userCacheService;
    private final RoleCacheService roleCacheService;
    private final DepartmentCacheService departmentCacheService;
    
    public PlatformUserServiceAdapter(
            PlatformFeignClient platformFeignClient,
            UserCacheService userCacheService,
            RoleCacheService roleCacheService,
            DepartmentCacheService departmentCacheService) {
        this.platformFeignClient = platformFeignClient;
        this.userCacheService = userCacheService;
        this.roleCacheService = roleCacheService;
        this.departmentCacheService = departmentCacheService;
    }
    
    
    /**
     * 根据用户ID列表转换为审批人列表
     */
    @Override
    public List<Approver> convertToApprovers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            // 先从缓存中获取
            List<UserInfoDto> cachedUsers = userCacheService.getUsersByIds(userIds);
            List<Long> missingUserIds = userIds.stream()
                    .filter(userId -> cachedUsers.stream().noneMatch(u -> u.getId().equals(userId)))
                    .collect(Collectors.toList());
            
            List<UserInfoDto> users = new ArrayList<>(cachedUsers);
            
            // 如果缓存中有缺失的用户，从远程服务获取并缓存
            if (!missingUserIds.isEmpty()) {
                try {
                    List<UserInfoDto> remoteUsers = platformFeignClient.getUsersByIds(missingUserIds);
                    if (remoteUsers != null) {
                        users.addAll(remoteUsers);
                        // 将新获取的用户存入缓存
                        userCacheService.cacheUsers(remoteUsers);
                    }
                } catch (Exception e) {
                    log.error("从远程服务获取用户信息失败，用户ID列表: {}", missingUserIds, e);
                }
            }
            
            return users.stream()
                    .filter(user -> user.getStatus() != null && user.getStatus() == 1) // 只返回启用状态的用户
                    .map(this::convertToApprover)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("批量获取用户信息失败，用户ID列表: {}", userIds, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 根据角色ID列表获取所有用户（去重）
     */
    @Override
    public List<Approver> getUsersByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> allUserIds = new ArrayList<>();
        for (Long roleId : roleIds) {
            try {
                // 先从缓存中获取
                List<Long> userIds = roleCacheService.getUserIdsByRoleId(roleId)
                        .orElse(null);
                
                // 如果缓存中没有，从远程服务获取并缓存
                if (userIds == null) {
                    userIds = platformFeignClient.getUserIdsByRoleId(roleId);
                    if (userIds != null) {
                        roleCacheService.cacheUserIdsByRoleId(roleId, userIds);
                    }
                }
                
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
     * 根据部门ID列表获取所有部门负责人（去重）
     * 
     * 注意：由于部门实体中没有 leaderId 字段，通过以下方式查找负责人：
     * 1. 查找部门下具有"部门负责人"角色的用户（如果存在该角色）
     * 2. 如果没有找到，返回部门下的第一个用户（作为临时方案）
     * 
     * TODO: 根据实际业务规则调整负责人查找逻辑
     */
    @Override
    public List<Approver> getDeptLeadersByDeptIds(List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> allUserIds = new ArrayList<>();
        for (Long deptId : deptIds) {
            try {
                // 先从缓存中获取
                List<Long> userIds = departmentCacheService.getUserIdsByDepartmentId(deptId)
                        .orElse(null);
                
                // 如果缓存中没有，从远程服务获取并缓存
                if (userIds == null) {
                    userIds = platformFeignClient.getUserIdsByDepartmentId(deptId);
                    if (userIds != null && !userIds.isEmpty()) {
                        departmentCacheService.cacheUserIdsByDepartmentId(deptId, userIds);
                    }
                }
                
                if (userIds != null && !userIds.isEmpty()) {
                    allUserIds.addAll(userIds);
                }
            } catch (Exception e) {
                log.error("根据部门ID获取用户ID列表失败，部门ID: {}", deptId, e);
            }
        }
        
        if (allUserIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 去重
        allUserIds = allUserIds.stream().distinct().collect(Collectors.toList());
        
        // 获取用户信息并转换为审批人
        // 注意：这里返回部门下的所有用户，实际业务中可能需要根据"部门负责人"角色过滤
        // TODO: 如果系统中有"部门负责人"角色，可以通过角色代码查找并取交集
        List<Approver> approvers = convertToApprovers(allUserIds);
        
        // 如果没有找到负责人角色，返回部门下的第一个用户（作为临时方案）
        if (!approvers.isEmpty()) {
            log.debug("未找到部门负责人角色，返回部门下第一个用户作为负责人，部门ID列表: {}", deptIds);
            return List.of(approvers.get(0));
        }
        
        return approvers;
    }
    
    /**
     * 将 UserInfoDto 转换为 Approver 值对象
     */
    private Approver convertToApprover(UserInfoDto user) {
        if (user == null) {
            return null;
        }
        
        // 获取用户的主要部门（如果有多个部门，取第一个）
        String deptName = null;
        Long deptId = null;
        if (user.getDepartments() != null && !user.getDepartments().isEmpty()) {
            DeptInfoDto firstDept = user.getDepartments().iterator().next();
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

