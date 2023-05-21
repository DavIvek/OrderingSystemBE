package fer.hr.orderingsystemws.payload.request;

import lombok.Data;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class NurseSignupRequest {
    @NotBlank(message = "Nije uneseno ime!")
    @Size(min = 1, max = 30, message = "Neispravno ime!")
    private String firstname;

    @NotBlank(message = "Nije uneseno prezime!")
    @Size(min = 1, max = 40, message = "Neispravno prezime!")
    private String lastname;

    @NotBlank(message = "Nije unesen OIB!")
    @Size(min = 11, max = 11, message = "Nesipravan OIB!")
    private String oib;

    @NotBlank(message = "Nije uneseno korisničko ime!")
    @Size(max = 20, message = "Neispravno korisničko ime!")
    private String username;

    @NotBlank
    @Size(max = 50, message = "Neispravna email adresa!")
    @Email(message = "Neispravna email adresa!")
    private String email;

    @NotBlank(message = "Nije unesen broj mobitela!")
    @Digits(integer = 11, fraction = 0, message = "Neispravan broj mobitela!")
    private String phoneNumber;

    @NotBlank(message = "Nije unesena lozinka!")
    @Size(min = 8, max = 128, message = "Neispravna lozinka!")
    private String password;

    @NotBlank(message = "Nije unesen spol!")
    @Size(min = 1, max = 1, message = "Neispravan spol!")
    private String gender;
}
