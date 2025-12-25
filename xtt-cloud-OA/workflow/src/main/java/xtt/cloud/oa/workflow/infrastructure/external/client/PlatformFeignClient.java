package xtt.cloud.oa.workflow.infrastructure.external.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import xtt.cloud.oa.common.dto.DeptInfoDto;
import xtt.cloud.oa.common.dto.UserInfoDto;

import java.util.List;
import java.util.Set;

/**
 * Platform 服务 Feign 客户端
 * 
 * 用于调用 platform 服务的对外接口
 * 
 * @author xtt
 */
@FeignClient(name = "platform", path = "/api/platform/external")
public interface PlatformFeignClient {
    
    // ========== 用户相关接口 ==========
    
    /**
     * 根据用户ID获取用户信息
     */
    @GetMapping("/users/{id}")
    UserInfoDto getUserById(@PathVariable("id") Long id);
    
    /**
     * 批量获取用户信息（根据ID列表）
     */
    @PostMapping("/users/batch")
    List<UserInfoDto> getUsersByIds(@RequestBody List<Long> userIds);
    
    /**
     * 根据用户ID获取用户部门列表
     */
    @GetMapping("/users/{id}/departments")
    Set<DeptInfoDto> getUserDepartments(@PathVariable("id") Long id);
    
    // ========== 角色相关接口 ==========
    
    /**
     * 根据角色ID获取角色下的用户ID列表
     * 
     * 注意：如果 platform 服务没有此接口，需要在 platform 服务中添加
     * 建议路径：GET /api/platform/external/roles/{id}/users
     */
    @GetMapping("/roles/{id}/users")
    List<Long> getUserIdsByRoleId(@PathVariable("id") Long roleId);
    
    // ========== 部门相关接口 ==========
    
    /**
     * 根据部门ID获取部门信息
     */
    @GetMapping("/departments/{id}")
    DeptInfoDto getDepartmentById(@PathVariable("id") Long id);
    
    /**
     * 根据部门ID获取部门下的用户ID列表
     * 
     * 注意：如果 platform 服务没有此接口，需要在 platform 服务中添加
     * 建议路径：GET /api/platform/external/departments/{id}/users
     */
    @GetMapping("/departments/{id}/users")
    List<Long> getUserIdsByDepartmentId(@PathVariable("id") Long departmentId);
}

