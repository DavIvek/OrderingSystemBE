package fer.hr.orderingsystemws.models.users;

import fer.hr.orderingsystemws.models.role.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("doctor")
@NoArgsConstructor
@Data
public class Doctor extends MedicalPerson {

    @Column
    private int reservationRule;

    public Doctor(
            String firstname,
            String lastname,
            String oib,
            String username,
            String email,
            String phoneNumber,
            String password,
            String gender,
            Role role,
            int reservationRule
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
        this.reservationRule = reservationRule;
    }

}
