package fer.hr.orderingsystemws.models.users;


import fer.hr.orderingsystemws.models.role.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@NoArgsConstructor
@DiscriminatorValue("admin")
@Data
public class Admin extends User {
    public Admin(
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
