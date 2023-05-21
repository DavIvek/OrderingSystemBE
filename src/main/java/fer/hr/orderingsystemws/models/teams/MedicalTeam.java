package fer.hr.orderingsystemws.models.teams;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "medical_teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalTeam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Date validFrom;

    private Long doctorId;

    private Long nurseId;

    public MedicalTeam(String name, Date validFrom, Long doctorId, Long nurseId) {
        this.name = name;
        this.validFrom = validFrom;
        this.doctorId = doctorId;
        this.nurseId = nurseId;
    }
}
