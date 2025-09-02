package xtt.cloud.oa.platform.interfaces.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xtt.cloud.oa.platform.application.PermissionService;
import xtt.cloud.oa.platform.domain.entity.Permission;

import java.util.List;

@RestController
@RequestMapping("/api/platform/permissions")
public class PermissionController {
    private final PermissionService permissionService;
    public PermissionController(PermissionService permissionService) { this.permissionService = permissionService; }

    @GetMapping
    public List<Permission> list() { return permissionService.list(); }

    @GetMapping("/{id}")
    public ResponseEntity<Permission> get(@PathVariable Long id) {
        return permissionService.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Permission create(@RequestBody Permission permission) { return permissionService.save(permission); }

    @PutMapping("/{id}")
    public ResponseEntity<Permission> update(@PathVariable Long id, @RequestBody Permission permission) {
        permission.setId(id);
        return ResponseEntity.ok(permissionService.save(permission));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { permissionService.delete(id); }
}


