package fer.hr.orderingsystemws.payload.request;

import lombok.Data;

@Data
public class ConfirmPatientAttendanceRequest {
    Long appointmentId;
    boolean attended;
}
