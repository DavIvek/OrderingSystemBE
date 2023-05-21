package fer.hr.orderingsystemws.controllers;

import fer.hr.orderingsystemws.payload.request.CreateMedicalTeamRequest;
import fer.hr.orderingsystemws.repository.MedicalTeamRepository;
import fer.hr.orderingsystemws.services.MedicalTeamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.NestedServletException;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/medical-teams")
public class MedicalTeamController {

    private static final Logger logger = LoggerFactory.getLogger(MedicalTeamController.class);

    private final MedicalTeamRepository medicalTeamRepository;
    private final MedicalTeamService medicalTeamService;

    @Autowired
    public MedicalTeamController(MedicalTeamRepository medicalTeamRepository, MedicalTeamService medicalTeamService) {
        this.medicalTeamRepository = medicalTeamRepository;
        this.medicalTeamService = medicalTeamService;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Throwable.class)
    public Map<String, String> handleValidationExceptions(Throwable throwable) {
        Map<String, String> errors = new HashMap<>();

        if (throwable instanceof MethodArgumentNotValidException exception) {
            ObjectError error = exception.getBindingResult().getAllErrors().stream().findFirst().get();
            errors.put("message", error.getDefaultMessage());
        } else if (throwable instanceof NestedServletException exception) {
            Error error = (Error) exception.getCause();
            errors.put("message", error.getMessage());
        } else {
            errors.put("message", throwable.getMessage());
        }

        return errors;
    }

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAll() {
        logger.info("MedicalTeamController.getAll()");

        try {
            return ResponseEntity.ok(medicalTeamRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getMedicalTeamById(@PathVariable Long id) {
        logger.info("MedicalTeamController.getMedicalTeamById({})", id);

        try {
            return ResponseEntity.ok(medicalTeamRepository.findById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addMedicalTeam(@Valid @RequestBody CreateMedicalTeamRequest createMedicalTeamRequest) {
        logger.info("MedicalTeamController.addMedicalTeam({})", createMedicalTeamRequest);

        try {
            return ResponseEntity.ok(medicalTeamService.createMedicalTeam(createMedicalTeamRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteMedicalTeam(@PathVariable Long id) {
        logger.info("MedicalTeamController.deleteMedicalTeam({})", id);

        try {
            return ResponseEntity.ok((medicalTeamService.deleteMedicalTeam(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
