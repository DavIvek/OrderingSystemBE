package fer.hr.orderingsystemws.models.users;


import fer.hr.orderingsystemws.models.role.Role;
import fer.hr.orderingsystemws.models.teams.MedicalTeam;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
@DiscriminatorValue("medical_type")
@NoArgsConstructor
@Data
public abstract class MedicalPerson extends User {

    @JoinColumn(name = "medical_team_id")
    @OneToOne
    private MedicalTeam medicalTeam;

    public MedicalPerson(
            String firstname,
            String lastname,
            String oib,
            String username,
            String email,
            String phoneNumber,
            String password,
            String gender,
            Role role
    ) {
        super(
                firstname,
                lastname,
                oib,
                username,
                email,
                phoneNumber,
                password,
                gender,
                role
        );
    }
}
