package fer.hr.orderingsystemws.services;

import fer.hr.orderingsystemws.models.SmsDetails;

public interface SmsService {
    // Send simple sms
    void sendSimpleSms(SmsDetails sms);
}
