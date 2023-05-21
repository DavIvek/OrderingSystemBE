package fer.hr.orderingsystemws.services;

import fer.hr.orderingsystemws.models.EmailDetails;

public interface MailService {

    // Send simple email
    void sendSimpleMail(EmailDetails details);

    // To send an email with attachment
    void sendMailWithAttachment(EmailDetails details);
}
