package fer.hr.orderingsystemws.payload.request;

import lombok.Data;

@Data
public class ReservePredefinedServiceRequest {
    private Long nurseId;
    private Long predefinedServiceId;
}
