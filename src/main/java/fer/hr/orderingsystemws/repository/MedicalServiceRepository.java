package fer.hr.orderingsystemws.repository;

import fer.hr.orderingsystemws.models.medicalService.MedicalService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalServiceRepository extends JpaRepository<MedicalService, Long> {
    List<MedicalService> findAll();
}
