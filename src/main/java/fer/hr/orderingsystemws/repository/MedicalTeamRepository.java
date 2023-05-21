package fer.hr.orderingsystemws.repository;

import fer.hr.orderingsystemws.models.teams.MedicalTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicalTeamRepository extends JpaRepository<MedicalTeam, Long> {
        List<MedicalTeam> findAll();
        Optional<MedicalTeam> findById(Long id);
}
