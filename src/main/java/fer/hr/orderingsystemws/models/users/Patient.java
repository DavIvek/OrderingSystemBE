package fer.hr.orderingsystemws.models.users;

import fer.hr.orderingsystemws.models.role.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@DiscriminatorValue("patient")
@NoArgsConstructor
public class Patient extends User {

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column
    private int disappearances;

    @Column
    private int notification; //make enum later

    public Patient(
            String firstname,
            String lastname,
            String oib,
            String username,
            String email,
            String phoneNumber,
            String password,
            String gender,
            Role role,
            Doctor doctor,
            int notification,
            int disappearances
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
        this.doctor = doctor;
        this.disappearances = disappearances;
        this.notification = notification;
    }
}
