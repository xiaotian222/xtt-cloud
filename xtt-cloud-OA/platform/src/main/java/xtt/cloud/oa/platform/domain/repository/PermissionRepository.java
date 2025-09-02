package xtt.cloud.oa.platform.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xtt.cloud.oa.platform.domain.entity.Permission;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByCode(String code);
    List<Permission> findByCodeIn(List<String> codes);
    List<Permission> findByType(String type);
}


