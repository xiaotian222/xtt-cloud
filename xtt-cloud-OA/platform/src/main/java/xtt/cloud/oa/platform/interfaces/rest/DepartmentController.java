package xtt.cloud.oa.platform.interfaces.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xtt.cloud.oa.platform.application.DepartmentService;
import xtt.cloud.oa.platform.domain.entity.Department;

import java.util.List;

@RestController
@RequestMapping("/api/platform/departments")
public class DepartmentController {
    private final DepartmentService departmentService;
    
    public DepartmentController(DepartmentService departmentService) {
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
     * 创建部门
     */
    @PostMapping
    public Department create(@RequestBody Department department) {
        return departmentService.save(department);
    }

    /**
     * 更新部门
     */
    @PutMapping("/{id}")
    public ResponseEntity<Department> update(@PathVariable Long id, @RequestBody Department department) {
        department.setId(id);
        return ResponseEntity.ok(departmentService.save(department));
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        departmentService.delete(id);
    }
}
