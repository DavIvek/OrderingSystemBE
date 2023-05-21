package fer.hr.orderingsystemws.repository;

import fer.hr.orderingsystemws.models.role.ERole;
import fer.hr.orderingsystemws.models.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
