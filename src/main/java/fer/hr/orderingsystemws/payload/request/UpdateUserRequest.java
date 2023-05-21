package fer.hr.orderingsystemws.payload.request;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
public class UpdateUserRequest {
    @Size(min = 1, max = 30, message = "Neispravno ime!")
    private String firstname;

    @Size(min = 1, max = 40, message = "Neispravno prezime!")
    private String lastname;

    @Size(min = 11, max = 11, message = "Neispravan OIB!")
    private String oib;

    @Size(max = 20, message = "Neispravno korisničko ime!")
    private String username;

    @Size(max = 50, message = "Neispravna email adresa!")
    @Email(message = "Neispravna email adresa!")
    private String email;

    @Digits(integer = 11, fraction = 0, message = "Neispravan broj mobitela!")
    private String phoneNumber;

    @Size(min = 8, max = 128, message = "Neispravna lozinka!")
    private String password;

    @Size(min = 1, max = 1, message = "Neispravan spol!")
    private String gender;

    @Range(min = 0, max = 1, message = "Neispravne postavke obavijesti!")
    private Integer notification;

    private Integer reservationRule;
}
