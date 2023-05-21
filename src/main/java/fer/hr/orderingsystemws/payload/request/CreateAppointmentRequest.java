package fer.hr.orderingsystemws.payload.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class CreateAppointmentRequest {
    @NotNull(message = "Nisu uneseni datum i vrijeme početka termina!")
    private Date start;

    @NotNull(message = "Nisu uneseni datum i vrijeme kraja termina!")
    private Date end;

    private Long medicalServiceId;

}
