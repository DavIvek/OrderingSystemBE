package fer.hr.orderingsystemws.services;

import com.google.gson.Gson;
import fer.hr.orderingsystemws.models.appointments.Appointment;
import fer.hr.orderingsystemws.models.medicalService.MedicalService;
import fer.hr.orderingsystemws.models.users.*;
import fer.hr.orderingsystemws.payload.request.CreateAppointmentRequest;
import fer.hr.orderingsystemws.payload.request.ReserveAppointmentRequest;
import fer.hr.orderingsystemws.payload.response.MessageResponse;
import fer.hr.orderingsystemws.repository.AppointmentRepository;
import fer.hr.orderingsystemws.repository.MedicalServiceRepository;
import fer.hr.orderingsystemws.repository.MedicalTeamRepository;
import fer.hr.orderingsystemws.repository.UserRepository;
import fer.hr.orderingsystemws.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    Gson gson = new Gson();

    NotificationsService notificationsService;
    UserRepository userRepository;
    AppointmentRepository appointmentRepository;
    MedicalServiceRepository medicalServiceRepository;
    MedicalTeamRepository medicalTeamRepository;

    @Autowired
    public AppointmentService(
            NotificationsService notificationsService,
            UserRepository userRepository,
            AppointmentRepository appointmentRepository,
            MedicalServiceRepository medicalServiceRepository,
            MedicalTeamRepository medicalTeamRepository
    ) {
        this.notificationsService = notificationsService;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.medicalServiceRepository = medicalServiceRepository;
        this.medicalTeamRepository = medicalTeamRepository;
    }


    @Scheduled(cron = "0 0 * * * *")
    public void notifyAppointmentParticipants() {
        logger.debug("AppointmentService.notifyAppointmentParticipants()");

        Timestamp start = Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS));
        Timestamp end = Timestamp.from(Instant.now().plus(2, ChronoUnit.HOURS));

        List<Appointment> appointments = appointmentRepository.findAllAppointmentsBetweenTimestamps(start, end);

        for (Appointment a : appointments) {
            notificationsService.notifyPatientAboutAppointmentReservation(a);
        }
    }

    @Scheduled(cron = "59 59 23 * * *")
    public void  checkUnresolvedAppointmentsAttendance() {
        logger.debug("AppointmentService.checkUnresolvedAppointmentsAttendance()");

        appointmentRepository.updateUnresolvedAppointmentsAttendance();
    }

    public Object getAvailableAppointmentsForMedicalService(Long medicalServiceId) throws Error {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        if (!userRepository.existsByUsername(userDetails.getUsername())) {
            throw new Error("Greška: Trenutni korisnik ne postoji!");
        }

        if (!medicalServiceRepository.existsById(medicalServiceId)) {
            throw new Error("Greška: Medicinska usluga ne postoji!");
        }

        List<Appointment> availableAppointments = appointmentRepository.findAvailableAppointmentsForMedicalService(medicalServiceId);

        if (availableAppointments.isEmpty()) {
            throw new Error("Greška: Nema dostupnih termina za odabranu medicinsku uslugu!");
        }

        return availableAppointments;
    }

    public Object createAppointment(CreateAppointmentRequest createAppointmentRequest) throws Error {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        if (!userRepository.existsByUsername(userDetails.getUsername())) {
            throw new Error("Greška: Trenutni korisnik ne postoji!");
        }

        User user = userRepository.findByUsername(userDetails.getUsername()).get();

        if (createAppointmentRequest.getStart().before(new Date())) {
            throw new Error("Greška: Termin ne može početi u prošlosti!");
        }

        if (createAppointmentRequest.getEnd().before(new Date())) {
            throw new Error("Greška: Termin ne može završiti u prošlosti!");
        }

        if (createAppointmentRequest.getEnd().before(createAppointmentRequest.getStart())) {
            throw new Error("Greška: Termin ne može završiti prije nego što počne!");
        }


        Appointment appointment = new Appointment();
        appointment.setStart(createAppointmentRequest.getStart());
        appointment.setEnd(createAppointmentRequest.getEnd());
        appointment.setPatient(null);

        if (user instanceof Doctor doctor) {
                appointment.setMedicalPerson(doctor);
            if (doctor.getMedicalTeam() != null && doctor.getMedicalTeam().getValidFrom().getTime() <= createAppointmentRequest.getStart().getTime()) {
                appointment.setMedicalTeam(doctor.getMedicalTeam());
            }
        } else if (user instanceof Nurse nurse) {
            if (nurse.getMedicalTeam() != null && nurse.getMedicalTeam().getValidFrom().getTime() <= createAppointmentRequest.getStart().getTime()) {
                throw new Error("Greška: Medicinska sestra ne može kreirati termine nakon početka rada medicinskog tima kojem pripada jer te termine kreira pripadajući doktor!");
            }

            if (createAppointmentRequest.getMedicalServiceId() == null) {
                throw new Error("Greška: Nije odabrana medicinska usluga!");
            }

            if (!medicalServiceRepository.existsById(createAppointmentRequest.getMedicalServiceId())) {
                throw new Error("Greška: Odabrana medicinska usluga ne postoji!");
            }

            MedicalService medicalService = medicalServiceRepository.findById(createAppointmentRequest.getMedicalServiceId()).get();

            appointment.setMedicalPerson(nurse);
            appointment.setMedicalService(medicalService);
        } else {
            throw new Error("Greška: Trenutni korisnik nije doktor ili medicinska sestra!");
        }

        appointmentRepository.save(appointment);

        return new MessageResponse("Termin uspješno kreiran!");
    }

    public Object reserveAppointment(ReserveAppointmentRequest reserveAppointmentRequest) throws Error {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        if (!userRepository.existsByUsername(userDetails.getUsername())) {
            throw new Error("Greška: Trenutni korisnik ne postoji!");
        }

        if (!(userRepository.findByUsername(userDetails.getUsername()).get() instanceof Patient patient)) {
            throw new Error("Greška: Trenutni korisnik nije pacijent!");
        }

        if (!appointmentRepository.existsById(reserveAppointmentRequest.getId())) {
            throw new Error("Greška: Termin ne postoji!");
        }

        Appointment appointment = appointmentRepository.findById(reserveAppointmentRequest.getId()).get();

        if (appointment.getPatient() != null) {
            throw new Error("Greška: Termin je već rezerviran!");
        }

        if (!medicalServiceRepository.existsById(reserveAppointmentRequest.getMedicalServiceId())) {
            throw new Error("Greška: Odabrana medicinska usluga ne postoji!");
        }

        MedicalService medicalService = medicalServiceRepository.findById(reserveAppointmentRequest.getMedicalServiceId()).get();

        if (appointment.getMedicalPerson() instanceof Doctor doctor) {
            if (appointment.getStart().before(new Date(System.currentTimeMillis() + (long) doctor.getReservationRule() * 60 * 60 * 1000))) {
                throw new Error("Greška: Termin se ne može rezervirati manje od " + doctor.getReservationRule() + "h prije početka!");
            }

            if (!doctor.getId().equals(patient.getDoctor().getId())) {
                throw new Error("Greška: Ne možete rezervirati termin kod doktora koji nije vaš!");
            }

            List<Appointment> patientAppointments = appointmentRepository.findAllPatientAppointments(patient.getId())
                    .stream()
                    .filter(a -> a.getEnd().before(new Date()))
                    .toList();

            int missedAppointments = patientAppointments
                    .stream()
                    .filter(a -> a.getPatientAttended() != null)
                    .filter(a -> !a.getPatientAttended())
                    .toList()
                    .size();

            if (missedAppointments > patientAppointments.size() / 10) {
                throw new Error("Greška: Pacijent se nije pojavio na više od 10% zakazanih termina te ga se stoga upućuje na izravan dogovor s doktorom!");
            }
        }

        appointmentRepository.reserveAppointment(
                appointment.getId(),
                patient.getId(),
                medicalService.getId()
        );

        appointment = appointmentRepository.findById(reserveAppointmentRequest.getId()).get();

        Appointment finalAppointment = appointment;
        CompletableFuture.runAsync(() -> notificationsService.notifyPatientAboutAppointmentReservation(finalAppointment));
        CompletableFuture.runAsync(() -> notificationsService.notifyMedicalPersonAboutAppointmentReservation(finalAppointment));

        userRepository.updateDisappearanceById(patient.getDisappearances() + 1, patient.getId());

        return new MessageResponse("Termin uspješno rezerviran!");
    }

    public Object cancelAppointment(Long appointmentId) throws Error {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        if (!userRepository.existsByUsername(userDetails.getUsername())) {
            throw new Error("Greška: Trenutni korisnik ne postoji!");
        }

        if (!(userRepository.findByUsername(userDetails.getUsername()).get() instanceof Patient patient)) {
            throw new Error("Greška: Trenutni korisnik nije pacijent!");
        }

        if (!appointmentRepository.existsById(appointmentId)) {
            throw new Error("Greška: Termin ne postoji!");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId).get();

        if (appointment.getStart().before(new Date())) {
            throw new Error("Greška: Termin ne može biti otkazan jer je već počeo!");
        }

        if (appointment.getStart().before(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))) {
            throw new Error("Greška: Termin se ne može otkazati manje od 24h prije početka!");
        }

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new Error("Greška: Ne možete otkazati termin koji niste vi rezervirali!");
        }

        if (appointment.getMedicalPerson() instanceof Doctor) {
            appointmentRepository.cancelDoctorAppointment(appointmentId);
        } else {
            appointmentRepository.cancelNurseAppointment(appointmentId);
        }

        CompletableFuture.runAsync(() -> notificationsService.notifyPatientAboutAppointmentCancellation(appointment));
        CompletableFuture.runAsync(() -> notificationsService.notifyMedicalPersonAboutAppointmentCancellation(appointment));

        return new MessageResponse("Termin uspješno otkazan!");
    }

    public Object postponeAppointment(Long oldAppointmentId) throws Error {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        if (!userRepository.existsByUsername(userDetails.getUsername())) {
            throw new Error("Greška: Trenutni korisnik ne postoji!");
        }

        if (!appointmentRepository.existsById(oldAppointmentId)) {
            throw new Error("Greška: Termin ne postoji!");
        }

        Appointment oldAppointment = appointmentRepository.findById(oldAppointmentId).get();
        List<Appointment> appointments = appointmentRepository.findAvailableAppointmentsForMedicalPerson(oldAppointment.getMedicalPerson().getId());
        if(appointments.size() == 0) {
            throw new Error("Greška: Nema više dostupnih termina za odabranog medicinskog djelatnika!");
        }
        Appointment newAppointment = appointments
                .stream()
                .filter(a -> a.getStart().after(oldAppointment.getStart()))
                .min(Comparator.comparing(Appointment::getStart))
                .orElseThrow(() -> new Error("Greška: Nema slobodnih termina!"));

        if (!(userDetails.getUser() instanceof MedicalPerson medicalPerson)) {
            throw new Error("Greška: Trenutni korisnik nije medicinska osoba!");
        }

        if (!newAppointment.getMedicalPerson().getId().equals(medicalPerson.getId())) {
            throw new Error("Greška: Ne možete odgoditi tuđi termin!");
        }

        if (oldAppointment.getPatient() == null) {
            throw new Error("Greška: Ne možete odgoditi termin koji nije rezerviran!");
        }

        if (newAppointment.getPatient() != null) {
            throw new Error("Greška: Termin je već rezerviran!");
        }

        if (oldAppointment.getStart().before(new Date())) {
            throw new Error("Greška: Termin ne može biti odgođen jer je već počeo!");
        }

        if (newAppointment.getStart().before(oldAppointment.getStart())) {
            throw new Error("Greška: Termin ne može biti odgođen na raniji termin!");
        }

        if (!newAppointment.getStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().equals(oldAppointment.getStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())) {
            throw new Error("Greška: Termin ne može biti odgođen na drugi dan!");
        }

        if (oldAppointment.getMedicalPerson() instanceof Doctor) {
            appointmentRepository.cancelDoctorAppointment(oldAppointmentId);
        } else {
            appointmentRepository.cancelNurseAppointment(oldAppointmentId);
        }

        appointmentRepository.reserveAppointment(newAppointment.getId(), oldAppointment.getPatient().getId(), oldAppointment.getMedicalService().getId());
        appointmentRepository.deleteById(oldAppointmentId);

        CompletableFuture.runAsync(() -> notificationsService.notifyPatientAboutAppointmentPostpone(newAppointment));

        return new MessageResponse("Termin uspješno odgođen!");
    }

    public Object confirmAppointment(Long appointmentId, boolean attended) throws Error {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        if (!(userDetails.getUser() instanceof MedicalPerson medicalPerson)) {
            throw new Error("Greška: Trenutni korisnik nije medicinska osoba!");
        }

        if (!appointmentRepository.existsById(appointmentId)) {
            throw new Error("Greška: Termin ne postoji!");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId).get();

        if (appointment.getPatient() == null) {
            throw new Error("Greška: Termin nije rezerviran!");
        }

        if (!appointment.getMedicalPerson().getId().equals(medicalPerson.getId())) {
            throw new Error("Greška: Ne možete potvrditi tuđi termin!");
        }

        if (appointment.getEnd().after(new Date())) {
            throw new Error("Greška: Termin još nije završio!");
        }

        Date endOfTheDay = new Date(appointment.getEnd().getTime());
        endOfTheDay.setHours(23);
        endOfTheDay.setMinutes(59);

        if (appointment.getEnd().after(endOfTheDay)) {
            throw new Error("Greška: Termin se ne može potvrditi nakon završetka radnog dana termina!");
        }

        if (attended) {
            userRepository.updateDisappearanceById(appointment.getPatient().getDisappearances() - 1, appointment.getPatient().getId());
        } else {
            userRepository.updateDisappearanceById(appointment.getPatient().getDisappearances() + 1, appointment.getPatient().getId());
        }

        appointmentRepository.confirmAppointment(appointmentId, attended);

        return new MessageResponse(String.format("Potvrdili ste dolazak pacijenta %s %s na termin!",
                appointment.getPatient().getFirstname(),
                appointment.getPatient().getLastname()));
    }
}
