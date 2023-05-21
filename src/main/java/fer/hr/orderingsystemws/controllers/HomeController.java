package fer.hr.orderingsystemws.controllers;

import fer.hr.orderingsystemws.models.medicalService.MedicalService;
import fer.hr.orderingsystemws.models.role.ERole;
import fer.hr.orderingsystemws.models.users.User;
import fer.hr.orderingsystemws.repository.MedicalServiceRepository;
import fer.hr.orderingsystemws.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private final UserRepository userRepository;
    private final MedicalServiceRepository medicalServiceRepository;

    @Autowired
    public HomeController(UserRepository userRepository, MedicalServiceRepository medicalServiceRepository) {
        this.userRepository = userRepository;
        this.medicalServiceRepository = medicalServiceRepository;
    }

    @GetMapping("/doctors")
    public ResponseEntity<?> getDoctors() {
        logger.info("HomeController.getDoctors()");

        List<User> doctors = userRepository.findAllByRoleIs(ERole.DOCTOR.name());

        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/nurses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getNurses() {
        logger.info("HomeController.getNurses()");

        List<User> nurses = userRepository.findAllByRoleIs(ERole.NURSE.name());

        return ResponseEntity.ok(nurses);
    }

    @GetMapping("/services")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<?> getServices() {
        logger.info("HomeController.getServices()");

        List<MedicalService> services = medicalServiceRepository.findAll();
        return ResponseEntity.ok(services);
    }


    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getUsers() {
        logger.info("HomeController.getUsers()");

        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
}
