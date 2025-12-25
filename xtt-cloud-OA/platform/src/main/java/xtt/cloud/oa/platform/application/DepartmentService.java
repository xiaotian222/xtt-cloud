package xtt.cloud.oa.platform.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.platform.domain.entity.Department;
import xtt.cloud.oa.platform.domain.mapper.DepartmentMapper;
import xtt.cloud.oa.platform.domain.mapper.UserDepartmentMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {
    private final DepartmentMapper departmentMapper;
    private final UserDepartmentMapper userDepartmentMapper;
    
    public DepartmentService(DepartmentMapper departmentMapper, UserDepartmentMapper userDepartmentMapper) {
        this.departmentMapper = departmentMapper;
        this.userDepartmentMapper = userDepartmentMapper;
    }

    /**
     * 查询所有部门
     */
    public List<Department> list() {
        return departmentMapper.selectAll();
    }
    
    /**
     * 根据ID查询部门
     */
    public Optional<Department> get(Long id) {
        return departmentMapper.findById(id);
    }

    /**
     * 根据父ID查询部门列表
     */
    public List<Department> getByParentId(Long parentId) {
        return departmentMapper.selectByParentId(parentId);
    }

    /**
     * 根据ID列表查询部门
     */
    public List<Department> getByIds(List<Long> ids) {
        return departmentMapper.selectByIds(ids);
    }

    /**
     * 保存部门（新增或更新）
     */
    @Transactional
    public Department save(Department department) {
        if (department.getId() == null) {
            // 新增部门
            department.setCreatedAt(LocalDateTime.now());
            department.setUpdatedAt(LocalDateTime.now());
            departmentMapper.insert(department);
        } else {
            // 更新部门
            department.setUpdatedAt(LocalDateTime.now());
            departmentMapper.update(department);
        }
        return department;
    }

    /**
     * 删除部门
     */
    @Transactional
    public void delete(Long id) {
        departmentMapper.deleteById(id);
    }

    /**
     * 构建部门树结构
     */
    public List<Department> buildTree() {
        List<Department> allDepts = departmentMapper.selectAll();
        return buildTreeRecursive(allDepts, null);
    }

    /**
     * 递归构建部门树
     */
    private List<Department> buildTreeRecursive(List<Department> allDepts, Long parentId) {
        return allDepts.stream()
                .filter(dept -> (parentId == null && dept.getParentId() == null) || 
                               (parentId != null && parentId.equals(dept.getParentId())))
                .peek(dept -> {
                    List<Department> children = buildTreeRecursive(allDepts, dept.getId());
                    if (!children.isEmpty()) {
                        dept.setChildren(children);
                    }
                })
                .toList();
    }

    /**
     * 根据部门ID获取该部门下的用户ID列表
     */
    public List<Long> getUserIdsByDepartmentId(Long departmentId) {
        return userDepartmentMapper.selectUserIdsByDepartmentId(departmentId);
    }
}
