package xtt.cloud.oa.platform.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xtt.cloud.oa.platform.domain.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByUsernameIn(List<String> usernames);
}


