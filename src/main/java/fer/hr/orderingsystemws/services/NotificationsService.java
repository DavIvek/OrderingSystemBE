package fer.hr.orderingsystemws.services;

import fer.hr.orderingsystemws.models.EmailDetails;
import fer.hr.orderingsystemws.models.SmsDetails;
import fer.hr.orderingsystemws.models.appointments.Appointment;
import fer.hr.orderingsystemws.models.users.MedicalPerson;
import fer.hr.orderingsystemws.models.users.Patient;
import fer.hr.orderingsystemws.repository.AppointmentRepository;
import fer.hr.orderingsystemws.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

@Service
public class NotificationsService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationsService.class);

    private final MailService mailService;
    private final SmsService smsService;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationsService(
            MailService mailService,
            SmsService smsService,
            AppointmentRepository appointmentRepository,
            UserRepository userRepository
    ) {
        this.mailService = mailService;
        this.smsService = smsService;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    // Create report each day of month at 08:00
    @Scheduled(cron = "0 0 8 * * *")
    public void sendStatisticForLastDay() {
        logger.info("AppointmentService.sendStatisticForLastDay()");

        Calendar cal = getTodaysCalendar();

        Timestamp end = new Timestamp(cal.getTimeInMillis());

        cal.add(Calendar.DAY_OF_MONTH, -1);

        Timestamp start = new Timestamp(cal.getTimeInMillis());

        List<Appointment> appointments = appointmentRepository.findAllAppointmentsBetweenTimestamps(start, end);

        int numberOfAppointmentsForLastDay = appointments.size();

        List<Appointment> attendedAppointments = appointmentRepository.findAllAttendedAppointmentsBetweenTimestamps(start, end);

        int numberOfAttendedAppointmentsForLastDay = attendedAppointments.size();

        createXmlDocument(numberOfAttendedAppointmentsForLastDay, numberOfAppointmentsForLastDay);

    }

    // Create report each first day of month at 07:00
    @Scheduled(cron = "0 0 7 1 * *")
    public void sendStatisticForLastMonth() {
        logger.info("AppointmentService.sendStatisticForLastMonth()");

        Calendar cal = getTodaysCalendar();

        Timestamp end = new Timestamp(cal.getTimeInMillis());

        cal.add(Calendar.MONTH, -1);

        Timestamp start = new Timestamp(cal.getTimeInMillis());

        List<Appointment> appointments = appointmentRepository.findAllAppointmentsBetweenTimestamps(start, end);

        int numberOfAppointmentsForLastMonth = appointments.size();

        List<Appointment> attendedAppointments = appointmentRepository.findAllAttendedAppointmentsBetweenTimestamps(start, end);

        int numberOfAttendedAppointmentsForLastMonth = attendedAppointments.size();

        createXmlDocument(numberOfAttendedAppointmentsForLastMonth, numberOfAppointmentsForLastMonth);

    }

    public void notifyPatientAboutAppointmentReservation(Appointment appointment) {
        Patient patient = appointment.getPatient();

        if (patient != null) {
            boolean notifyOverEmail = patient.getNotification() == 0;

            logger.info("NotificationsService.notifyPatientAboutAppointmentReservation(): Notify {} {} over {}", patient.getFirstname(),
                    patient.getLastname(), notifyOverEmail ? "e-mail" : "sms");

            if (notifyOverEmail) {
                notifyPatientByEmailAboutAppointmentReservation(patient, appointment.getMedicalPerson(), appointment);
            } else {
                notifyPatientBySMSAboutAppointmentReservation(patient, appointment.getMedicalPerson(), appointment);
            }
        }
    }

    public void notifyMedicalPersonAboutAppointmentReservation(Appointment appointment) {
        if (appointment.getMedicalTeam() != null) {
            List<MedicalPerson> medicalPersons = userRepository.findAllByTeamId(appointment.getMedicalTeam().getId());
            medicalPersons.forEach(medicalPerson -> {
                logger.info("NotificationsService.notifyMedicalPersonAboutAppointmentReservation(): Notify {} {} over email", medicalPerson.getFirstname(),
                        medicalPerson.getLastname());

                notifyMedicalPersonByEmailAboutAppointmentReservation(appointment.getPatient(), medicalPerson, appointment);
            });
        } else if (appointment.getMedicalPerson() != null) {
            MedicalPerson medicalPerson = appointment.getMedicalPerson();

            logger.info("NotificationsService.notifyMedicalPersonAboutAppointmentReservation(): Notify {} {} over e-mail",
                    medicalPerson.getFirstname(), medicalPerson.getLastname());

            notifyMedicalPersonByEmailAboutAppointmentReservation(appointment.getPatient(), medicalPerson, appointment);
        }
    }

    public void notifyPatientAboutAppointmentCancellation(Appointment appointment) {
        Patient patient = appointment.getPatient();

        boolean notifyOverEmail = patient.getNotification() == 0;

        logger.info("NotificationsService.notifyPatientAboutAppointmentCancellation(): Notify {} {} over {}", patient.getFirstname(),
                patient.getLastname(), notifyOverEmail ? "e-mail" : "sms");

        if (notifyOverEmail) {
            notifyPatientByEmailAboutAppointmentCancellation(patient, appointment.getMedicalPerson(), appointment);
        } else {
            notifyPatientBySMSAboutAppointmentCancellation(patient, appointment.getMedicalPerson(), appointment);
        }
    }

    public void notifyMedicalPersonAboutAppointmentCancellation(Appointment appointment) {
        if (appointment.getMedicalTeam() != null) {
            List<MedicalPerson> medicalPersons = userRepository.findAllByTeamId(appointment.getMedicalTeam().getId());
            medicalPersons.forEach(medicalPerson -> {
                logger.info("NotificationsService.notifyMedicalPersonAboutAppointmentCancellation(): Notify {} {} over email", medicalPerson.getFirstname(),
                        medicalPerson.getLastname());

                notifyMedicalPersonByEmailAboutAppointmentCancellation(appointment.getPatient(), medicalPerson, appointment);
            });
        } else if (appointment.getMedicalPerson() != null) {
            MedicalPerson medicalPerson = appointment.getMedicalPerson();

            logger.info("NotificationsService.notifyMedicalPersonAboutAppointmentCancellation(): Notify {} {} over e-mail",
                    medicalPerson.getFirstname(), medicalPerson.getLastname());

            notifyMedicalPersonByEmailAboutAppointmentCancellation(appointment.getPatient(), medicalPerson, appointment);
        }
    }

    public void notifyPatientAboutAppointmentPostpone(Appointment appointment) {
        Patient patient = appointment.getPatient();
        boolean notifyOverEmail = patient.getNotification() == 0;

        logger.info("NotificationsService.notifyPatientAboutAppointmentPostpone(): Notify {} {} over {}", patient.getFirstname(),
                patient.getLastname(), notifyOverEmail ? "e-mail" : "sms");

        if (notifyOverEmail) {
            notifyPatientByEmailAboutAppointmentPostpone(patient, appointment.getMedicalPerson(), appointment);
        } else {
            notifyPatientBySMSAboutAppointmentPostpone(patient, appointment.getMedicalPerson(), appointment);
        }
    }


    // Email
    private void notifyPatientByEmailAboutAppointmentReservation(Patient patient, MedicalPerson medicalPerson, Appointment appointment) {
        String emailMessage = String.format("Rezervirali ste termin %s kod %s %s.", appointment.toString(),
                medicalPerson.getFirstname(), medicalPerson.getLastname());
        EmailDetails emailDetails = new EmailDetails(patient.getEmail(), emailMessage, "Rezervacija termina");
        mailService.sendSimpleMail(emailDetails);
    }

    private void notifyMedicalPersonByEmailAboutAppointmentReservation(Patient patient, MedicalPerson medicalPerson, Appointment appointment) {
        String emailMessage = String.format("Pacijent %s %s je rezervirao termin %s.", patient.getFirstname(), patient.getLastname(),
                appointment.toString());
        EmailDetails emailDetails = new EmailDetails(medicalPerson.getEmail(), emailMessage, "Rezervacija termina");
        mailService.sendSimpleMail(emailDetails);
    }

    private void notifyPatientByEmailAboutAppointmentPostpone(Patient patient, MedicalPerson medicalPerson, Appointment appointment) {
        String emailMessage = String.format("Zakazani termin Vam je pomaknut na termin %s kod %s %s. " +
                        "Ukoliko Vam termin ne odgovara nožete ga otkazati u aplikaciji", appointment.toString(),
                medicalPerson.getFirstname(), medicalPerson.getLastname());
        EmailDetails emailDetails = new EmailDetails(patient.getEmail(), emailMessage, "Promjena termina");
        mailService.sendSimpleMail(emailDetails);
    }

    private void notifyPatientByEmailAboutAppointmentCancellation(Patient patient, MedicalPerson medicalPerson, Appointment appointment) {
        String emailMessage = String.format("Otkazali ste termin %s kod %s %s.", appointment.toString(),
                medicalPerson.getFirstname(), medicalPerson.getLastname());
        EmailDetails emailDetails = new EmailDetails(patient.getEmail(), emailMessage, "Otkazivanje termina");
        mailService.sendSimpleMail(emailDetails);
    }

    private void notifyMedicalPersonByEmailAboutAppointmentCancellation(Patient patient, MedicalPerson medicalPerson, Appointment appointment) {
        String emailMessage = String.format("Pacijent %s %s je otkazao termin %s.", patient.getFirstname(), patient.getLastname(),
                appointment.toString());
        EmailDetails emailDetails = new EmailDetails(medicalPerson.getEmail(), emailMessage, "Otkazivanje termina");
        mailService.sendSimpleMail(emailDetails);
    }

    // SMS
    private void notifyPatientBySMSAboutAppointmentReservation(Patient patient, MedicalPerson medicalPerson, Appointment appointment) {
        String smsMessage = String.format("Rezervirali ste termin %s kod %s %s.", appointment.toString(),
                medicalPerson.getFirstname(), medicalPerson.getLastname());
        String newPhoneNumber = "+385" + patient.getPhoneNumber().substring(1);
        SmsDetails details = new SmsDetails(newPhoneNumber, smsMessage);
        smsService.sendSimpleSms(details);
    }

    private void notifyPatientBySMSAboutAppointmentCancellation(Patient patient, MedicalPerson medicalPerson, Appointment appointment) {
        String smsMessage = String.format("Otkazali ste termin %s kod %s %s.", appointment.toString(),
                medicalPerson.getFirstname(), medicalPerson.getLastname());
        String newPhoneNumber = "+385" + patient.getPhoneNumber().substring(1);
        SmsDetails details = new SmsDetails(newPhoneNumber, smsMessage);
        smsService.sendSimpleSms(details);
    }

    private void notifyPatientBySMSAboutAppointmentPostpone(Patient patient, MedicalPerson medicalPerson, Appointment appointment) {
        String smsMessage = String.format("Zakazani termin Vam je pomaknut na termin %s kod %s %s. " +
                        "Ukoliko Vam termin ne odgovara možete ga otkazati u aplikaciji", appointment.toString(),
                medicalPerson.getFirstname(), medicalPerson.getLastname());
        String newPhoneNumber = "+385" + patient.getPhoneNumber().substring(1);
        SmsDetails details = new SmsDetails(newPhoneNumber, smsMessage);
        smsService.sendSimpleSms(details);
    }

    // Calendar helper
    private Calendar getTodaysCalendar() {

        Instant ts = Instant.now();
        Calendar calHelper = Calendar.getInstance();
        calHelper.setTimeInMillis(ts.getEpochSecond() * 1000);
        calHelper.get(Calendar.YEAR);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(Calendar.YEAR, calHelper.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, calHelper.get(Calendar.MONTH));
        cal.set(Calendar.DAY_OF_MONTH, calHelper.get(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal;
    }

    private void createXmlDocument(int numberOfAttendedAppointments, int numberOfAppointments) {

        LocalDateTime date = LocalDateTime.now();
        date.atZone(ZoneId.systemDefault());
        DateTimeFormatter format = DateTimeFormatter.ofPattern("ddMMyyyyHHmmssSSS");

        String currentDate = date.format(format);

        try (FileOutputStream output = new FileOutputStream("/tmp/report-" + currentDate + ".xml")) {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("stats");
            doc.appendChild(rootElement);

            rootElement.setAttribute("numOfAppointments", String.valueOf(numberOfAppointments));

            rootElement.setAttribute("attendedAppointments", String.valueOf(numberOfAttendedAppointments));

            String percentage = numberOfAppointments == 0 ? "-" : String.format("%.2f%%", ((double) numberOfAttendedAppointments) / numberOfAppointments);
            rootElement.setAttribute("successfulMedicalExaminations", percentage);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(output);

            transformer.transform(source, result);
        } catch (IOException e) {
            logger.error("NotificationsService.createXmlDocument(): IOException caught: {}", e.getMessage());
        } catch (TransformerConfigurationException e) {
            logger.error("NotificationsService.createXmlDocument(): TransformerConfigurationException caught: {}", e.getMessage());
        } catch (TransformerException e) {
            logger.error("NotificationsService.createXmlDocument(): TransformerException caught: {}", e.getMessage());
        } catch (ParserConfigurationException e) {
            logger.error("NotificationsService.createXmlDocument(): ParserConfigurationException caught: {}", e.getMessage());
        }
    }

}
