package fer.hr.orderingsystemws.security.services;

import fer.hr.orderingsystemws.models.role.ERole;
import fer.hr.orderingsystemws.models.users.Doctor;
import fer.hr.orderingsystemws.models.users.Nurse;
import fer.hr.orderingsystemws.models.users.Patient;
import fer.hr.orderingsystemws.payload.request.DoctorSignupRequest;
import fer.hr.orderingsystemws.payload.request.LoginRequest;
import fer.hr.orderingsystemws.payload.request.NurseSignupRequest;
import fer.hr.orderingsystemws.payload.request.PatientSignupRequest;
import fer.hr.orderingsystemws.payload.response.JwtResponse;
import fer.hr.orderingsystemws.payload.response.MessageResponse;
import fer.hr.orderingsystemws.repository.RoleRepository;
import fer.hr.orderingsystemws.repository.UserRepository;
import fer.hr.orderingsystemws.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    AuthenticationManager authenticationManager;
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder encoder;
    JwtUtils jwtUtils;

    @Autowired
    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder encoder,
            JwtUtils jwtUtils
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    public Object authenticateUser(LoginRequest loginRequest) throws Error {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());


        return new JwtResponse(
                jwt,
                userDetails.getUser().getId(),
                userDetails.getUser().getUsername(),
                userDetails.getUser().getEmail(),
                roles
        );
    }

    public Object registerPatient(PatientSignupRequest patientSignupRequest) throws Error {
        if (userRepository.existsByUsername(patientSignupRequest.getUsername())) {
            throw new Error("Greška: Korisničko ime se već koristi!");
        }

        if (userRepository.existsByEmail(patientSignupRequest.getEmail())) {
            throw new Error("Greška: Korisnik s ovom email adresom već postoji!");
        }

        if (userRepository.existsByOib(patientSignupRequest.getOib())) {
            throw new Error("Greška: Korisnik s ovim OIB-om već postoji!");
        }

        if (!userRepository.existsById(patientSignupRequest.getDoctorId()) || !userRepository.findById(patientSignupRequest.getDoctorId()).get().getRole().getName().equals(ERole.DOCTOR)) {
            throw new Error("Greška: Ne postoji odabrani doktor!");
        }

        if (!(userRepository.getById(patientSignupRequest.getDoctorId()) instanceof Doctor doctor)) {
            throw new Error("Greška: Korisnik nije doktor!");
        }

        // Create new patient's account
        Patient patient = new Patient(
                patientSignupRequest.getFirstname(),
                patientSignupRequest.getLastname(),
                patientSignupRequest.getOib(),
                patientSignupRequest.getUsername(),
                patientSignupRequest.getEmail(),
                patientSignupRequest.getPhoneNumber(),
                encoder.encode(patientSignupRequest.getPassword()),
                patientSignupRequest.getGender(),
                roleRepository.findByName(ERole.PATIENT).get(),
                doctor,
                patientSignupRequest.getNotificationsSettings(),
                0
        );

        userRepository.save(patient);

        return new MessageResponse("Registracija uspješna!");
    }

    public Object registerDoctor(DoctorSignupRequest doctorSignupRequest) throws Error {
        if (userRepository.existsByUsername(doctorSignupRequest.getUsername())) {
            throw new Error("Greška: Korisničko ime se već koristi!");
        }

        if (userRepository.existsByEmail(doctorSignupRequest.getEmail())) {
            throw new Error("Greška: Korisnik s ovom email adresom već postoji!");
        }

        if (userRepository.existsByOib(doctorSignupRequest.getOib())) {
            throw new Error("Greška: Korisnik s ovim OIB-om već postoji!");
        }

        // Create new doctor's account
        Doctor doctor = new Doctor(
                doctorSignupRequest.getFirstname(),
                doctorSignupRequest.getLastname(),
                doctorSignupRequest.getOib(),
                doctorSignupRequest.getUsername(),
                doctorSignupRequest.getEmail(),
                doctorSignupRequest.getPhoneNumber(),
                encoder.encode(doctorSignupRequest.getPassword()),
                doctorSignupRequest.getGender(),
                roleRepository.findByName(ERole.DOCTOR).get(),
                doctorSignupRequest.getReservationRule()
        );

        userRepository.save(doctor);

        return new MessageResponse("Registracija uspješna!");
    }

    public Object registerNurse(NurseSignupRequest nurseSignupRequest) {
        if (userRepository.existsByUsername(nurseSignupRequest.getUsername())) {
            throw new Error("Greška: Korisničko ime se već koristi!");
        }

        if (userRepository.existsByEmail(nurseSignupRequest.getEmail())) {
            throw new Error("Greška: Korisnik s ovom email adresom već postoji!");
        }

        if (userRepository.existsByOib(nurseSignupRequest.getOib())) {
            throw new Error("Greška: Korisnik s ovim OIB-om već postoji!");
        }

        // Create new nurse's account
        Nurse nurse = new Nurse(
                nurseSignupRequest.getFirstname(),
                nurseSignupRequest.getLastname(),
                nurseSignupRequest.getOib(),
                nurseSignupRequest.getUsername(),
                nurseSignupRequest.getEmail(),
                nurseSignupRequest.getPhoneNumber(),
                encoder.encode(nurseSignupRequest.getPassword()),
                nurseSignupRequest.getGender(),
                roleRepository.findByName(ERole.NURSE).get()
        );

        userRepository.save(nurse);

        return new MessageResponse("Registracija uspješna!");
    }
}
