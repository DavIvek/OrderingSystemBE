package fer.hr.orderingsystemws.security.services;

import fer.hr.orderingsystemws.models.appointments.Appointment;
import fer.hr.orderingsystemws.models.role.ERole;
import fer.hr.orderingsystemws.models.role.Role;
import fer.hr.orderingsystemws.models.users.Doctor;
import fer.hr.orderingsystemws.models.users.Nurse;
import fer.hr.orderingsystemws.models.users.Patient;
import fer.hr.orderingsystemws.models.users.User;
import fer.hr.orderingsystemws.payload.request.UpdateUserRequest;
import fer.hr.orderingsystemws.payload.response.JwtResponse;
import fer.hr.orderingsystemws.payload.response.MessageResponse;
import fer.hr.orderingsystemws.repository.AppointmentRepository;
import fer.hr.orderingsystemws.repository.UserRepository;
import fer.hr.orderingsystemws.security.jwt.JwtUtils;
import fer.hr.orderingsystemws.services.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class UserService {
    MailService mailService;
    UserRepository userRepository;
    AuthenticationManager authenticationManager;
    PasswordEncoder encoder;
    AppointmentRepository appointmentRepository;
    JwtUtils jwtUtils;

    @Autowired
    public UserService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder encoder,
            JwtUtils jwtUtils,
            MailService mailService,
            AppointmentRepository appointmentRepository
    ) {
        this.mailService = mailService;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.appointmentRepository = appointmentRepository;
    }

    public User getUser() throws Error {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        if (userRepository.findByUsername(userDetails.getUsername()).isPresent()) {
            return userRepository.findByUsername(userDetails.getUsername()).get();
        } else {
            throw new Error("Greška: Korisnik ne postoji!");
        }
    }

    public Object updateUser(UpdateUserRequest updateUserRequest) throws Error {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long id = userDetails.getUser().getId();
        User user = userRepository.findById(id).get();
        Role role = user.getRole();

        if (role.getName() == ERole.ADMIN) {
            throw new Error("Greška: Admin ne može ažurirati svoje podatke!");
        }

        if (updateUserRequest.getFirstname() != null && !updateUserRequest.getFirstname().equals(user.getFirstname())) {
            userRepository.updateFirstnameById(updateUserRequest.getFirstname(), id);
        }

        if (updateUserRequest.getLastname() != null && !updateUserRequest.getLastname().equals(user.getLastname())) {
            userRepository.updateLastnameById(updateUserRequest.getLastname(), id);
        }

        if (updateUserRequest.getOib() != null && !updateUserRequest.getOib().equals(user.getOib())) {
            if (userRepository.existsByOib(updateUserRequest.getOib())) {
                throw new Error("Greška: Korisnik s ovim OIB-om već postoji!");
            } else {
                userRepository.updateOibById(updateUserRequest.getOib(), id);
            }
        }

        if (updateUserRequest.getUsername() != null && !updateUserRequest.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(updateUserRequest.getUsername())) {
                throw new Error("Greška: Korisnik s ovim korisničkim imenom već postoji!");
            } else {
                userRepository.updateUsernameById(updateUserRequest.getUsername(), id);
            }
        }

        if (updateUserRequest.getEmail() != null && !updateUserRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateUserRequest.getEmail())) {
                throw new Error("Greška: Korisnik s ovom e-mail adresom već postoji!");
            } else {
                userRepository.updateEmailById(updateUserRequest.getEmail(), id);
            }
        }

        if (updateUserRequest.getPhoneNumber() != null && !updateUserRequest.getPhoneNumber().equals(user.getPhoneNumber())) {
            userRepository.updatePhoneNumberById(updateUserRequest.getPhoneNumber(), id);
        }

        if (updateUserRequest.getPassword() != null && !encoder.matches(updateUserRequest.getPassword(), user.getPassword())) {
            userRepository.updatePasswordById(encoder.encode(updateUserRequest.getPassword()), id);
        }

        if (updateUserRequest.getGender() != null && !updateUserRequest.getGender().equals(user.getGender())) {
            userRepository.updateGenderById(updateUserRequest.getGender(), id);
        }

        if (role.getName() == ERole.PATIENT && updateUserRequest.getNotification() != null) {
            userRepository.updateNotificationSettingsById(updateUserRequest.getNotification(), id);
        }

        if (role.getName() == ERole.DOCTOR && updateUserRequest.getReservationRule() != null) {
            userRepository.updateReservationRuleById(updateUserRequest.getReservationRule(), id);
        }

        if (updateUserRequest.getUsername() != null && !updateUserRequest.getUsername().equals(user.getUsername())
                || updateUserRequest.getPassword() != null && !encoder.matches(updateUserRequest.getPassword(), user.getPassword())) {
            String jwt = jwtUtils.generateJwtTokenAfterChangingCredentials(updateUserRequest);

            List<String> roles = Collections.singletonList("ROLE_" + role.getName());

            return new JwtResponse(
                    jwt,
                    id,
                    updateUserRequest.getUsername(),
                    userDetails.getUser().getEmail(),
                    roles
            );
        }
        return new MessageResponse("Uspješno ažurirani podaci!");
    }

    public List<Appointment> getMyAppointments() throws Error {
        User user = this.getUser();

        if (user.getRole().getName().equals(ERole.PATIENT)) {
            return appointmentRepository.findAllPatientAppointments(user.getId());
        } else if (user instanceof Nurse nurse) {
            List<Appointment> myAppointments = appointmentRepository.findAllMedicalPersonAppointments(nurse.getId());

            if (nurse.getMedicalTeam() != null) {
                List<Appointment> medicalTeamAppointments = appointmentRepository.findAllMedicalTeamAppointments(nurse.getMedicalTeam().getId());
                myAppointments.addAll(medicalTeamAppointments);
            }

            return myAppointments;
        }

        return appointmentRepository.findAllMedicalPersonAppointments(user.getId());
    }

    public List<Appointment> getAvailableAppointments() throws Error {
        if (this.getUser() instanceof  Patient patient) {
            return appointmentRepository.findAvailableAppointmentsForMedicalPerson(patient.getDoctor().getId());
        } else if (this.getUser() instanceof Doctor doctor) {
            return appointmentRepository.findAvailableAppointmentsForMedicalPerson(doctor.getId());
        } else if (this.getUser() instanceof Nurse nurse) {
            List<Appointment> myAppointments = appointmentRepository.findAvailableAppointmentsForMedicalPerson(nurse.getId());

            if (nurse.getMedicalTeam() != null) {
                List<Appointment> medicalTeamAppointments = appointmentRepository.findAvailableAppointmentsForMedicalTeam(nurse.getMedicalTeam().getId());
                myAppointments.addAll(medicalTeamAppointments);
            }

            return myAppointments;
        }
        throw new Error("Greška: Korisnik nema pravo pristupa!");
    }

    public List<Appointment> getAvailableAppointmentsForNurse() {
        List<User> nurses = userRepository.findAllByRoleIs(ERole.NURSE.name());
        List<Appointment> appointments = new ArrayList<>();

        for (User nurse : nurses) {
            appointments.addAll(appointmentRepository.findAvailableAppointmentsForMedicalPerson(nurse.getId()));
        }

        return appointments;
    }
}
