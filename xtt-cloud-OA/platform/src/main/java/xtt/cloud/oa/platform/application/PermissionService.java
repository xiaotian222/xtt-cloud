package xtt.cloud.oa.platform.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtt.cloud.oa.platform.domain.entity.Permission;
import xtt.cloud.oa.platform.domain.repository.PermissionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;
    public PermissionService(PermissionRepository permissionRepository) { this.permissionRepository = permissionRepository; }

    public List<Permission> list() { return permissionRepository.findAll(); }
    public Optional<Permission> get(Long id) { return permissionRepository.findById(id); }

    @Transactional
    public Permission save(Permission permission) { return permissionRepository.save(permission); }

    @Transactional
    public void delete(Long id) { permissionRepository.deleteById(id); }
}


