package xtt.cloud.oa.platform.interfaces.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xtt.cloud.oa.platform.application.RoleService;
import xtt.cloud.oa.platform.domain.entity.Role;

import java.util.List;

@RestController
@RequestMapping("/api/platform/roles")
public class RoleController {
    private final RoleService roleService;
    public RoleController(RoleService roleService) { this.roleService = roleService; }

    @GetMapping
    public List<Role> list() { return roleService.list(); }

    @GetMapping("/{id}")
    public ResponseEntity<Role> get(@PathVariable Long id) {
        return roleService.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Role create(@RequestBody Role role) { return roleService.save(role); }

    @PutMapping("/{id}")
    public ResponseEntity<Role> update(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        return ResponseEntity.ok(roleService.save(role));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { roleService.delete(id); }

    @PostMapping("/{id}/permissions")
    public Role grantPermissions(@PathVariable("id") Long roleId, @RequestBody java.util.List<Long> permIds) {
      return roleService.grantPermissions(roleId, permIds);
    }
}


