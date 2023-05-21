package fer.hr.orderingsystemws.models.medicalService;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("examination")
@Data
public class Examination extends MedicalService {

    @Column
    private String type;

}
