package fer.hr.orderingsystemws.payload.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CreateMedicalTeamRequest {
    @NotNull(message = "Nije uneseno ime tima")
    private String name;

    @NotNull(message = "Nije unesen ID doktora")
    private Long doctorId;

    @NotNull(message = "Nije unesen ID medicinske sestre")
    private Long nurseId;
}
