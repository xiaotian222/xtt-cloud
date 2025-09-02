package xtt.cloud.oa.platform.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xtt.cloud.oa.platform.domain.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCode(String code);
    List<Role> findByCodeIn(List<String> codes);
}


