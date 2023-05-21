package fer.hr.orderingsystemws.payload.request;

import lombok.Data;

@Data
public class ReserveAppointmentRequest {
    private Long id;
    private Long medicalServiceId;
}
