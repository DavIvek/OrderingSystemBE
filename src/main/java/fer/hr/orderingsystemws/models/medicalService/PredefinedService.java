package fer.hr.orderingsystemws.models.medicalService;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("predefined_service")
@Data
public class PredefinedService extends MedicalService {
    @Column
    private String type;
}
