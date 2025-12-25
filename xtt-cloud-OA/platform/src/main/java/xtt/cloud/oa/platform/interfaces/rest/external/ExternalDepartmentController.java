package xtt.cloud.oa.platform.interfaces.rest.external;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xtt.cloud.oa.platform.application.DepartmentService;
import xtt.cloud.oa.platform.domain.entity.Department;

import java.util.List;

/**
 * 部门对外接口控制器
 * 为其他微服务提供部门查询接口
 */
@RestController
@RequestMapping("/api/platform/external/departments")
public class ExternalDepartmentController {
    private final DepartmentService departmentService;
    
    public ExternalDepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * 查询所有部门
     */
    @GetMapping
    public List<Department> list() {
        return departmentService.list();
    }

    /**
     * 查询部门树结构
     */
    @GetMapping("/tree")
    public List<Department> tree() {
        return departmentService.buildTree();
    }

    /**
     * 根据ID查询部门
     */
    @GetMapping("/{id}")
    public ResponseEntity<Department> get(@PathVariable Long id) {
        return departmentService.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据父ID查询部门列表
     */
    @GetMapping("/parent/{parentId}")
    public List<Department> getByParentId(@PathVariable Long parentId) {
        return departmentService.getByParentId(parentId);
    }

    /**
     * 根据部门ID列表查询部门信息
     */
    @PostMapping("/ids")
    public List<Department> getByIds(@RequestBody List<Long> ids) {
        return departmentService.getByIds(ids);
    }

    /**
     * 根据部门ID获取该部门下的用户ID列表
     * 
     * 供其他微服务调用，用于获取部门下的所有用户
     */
    @GetMapping("/{id}/users")
    public ResponseEntity<List<Long>> getUserIdsByDepartmentId(@PathVariable Long id) {
        List<Long> userIds = departmentService.getUserIdsByDepartmentId(id);
        return ResponseEntity.ok(userIds);
    }
}
