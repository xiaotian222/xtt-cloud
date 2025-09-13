package xtt.cloud.oa.platform.interfaces.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xtt.cloud.oa.platform.application.UserService;
import xtt.cloud.oa.platform.application.PermissionService;
import xtt.cloud.oa.platform.domain.entity.User;

import java.util.List;

@RestController
@RequestMapping("/api/platform/users")
public class UserController {
    private final UserService userService;
    private final PermissionService permissionService;
    
    public UserController(UserService userService, PermissionService permissionService) { 
        this.userService = userService; 
        this.permissionService = permissionService;
    }

    @GetMapping
    public List<User> list() { return userService.list(); }

    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable Long id) {
        return userService.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public User create(@RequestBody User user) { return userService.save(user); }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return ResponseEntity.ok(userService.save(user));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { userService.delete(id); }

    @PostMapping("/{id}/roles")
    public User grantRoles(@PathVariable("id") Long userId, @RequestBody java.util.List<Long> roleIds) {
        return userService.grantRoles(userId, roleIds);
    }

    @PostMapping("/{id}/departments")
    public User grantDepartments(@PathVariable("id") Long userId, @RequestBody java.util.List<Long> departmentIds) {
        return userService.grantDepartments(userId, departmentIds);
    }

    @GetMapping("/{id}/permissions")
    public java.util.Set<String> getUserPerms(@PathVariable("id") Long userId) {
        return permissionService.getUserPermissions(userId);
    }

    @GetMapping("/permissions")
    public java.util.Set<String> getUserPermsByUsername(@RequestParam("username") String username) {
        return permissionService.getUserPermissionsByUsername(username);
    }
}


