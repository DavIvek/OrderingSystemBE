package fer.hr.orderingsystemws.controllers;

import fer.hr.orderingsystemws.payload.request.DoctorSignupRequest;
import fer.hr.orderingsystemws.payload.request.LoginRequest;
import fer.hr.orderingsystemws.payload.request.NurseSignupRequest;
import fer.hr.orderingsystemws.payload.request.PatientSignupRequest;
import fer.hr.orderingsystemws.payload.response.MessageResponse;
import fer.hr.orderingsystemws.security.services.AuthService;
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
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
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

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("AuthController.authenticateUser({})", loginRequest.getUsername());

        try {
            return ResponseEntity.ok(authService.authenticateUser(loginRequest));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerPatient(@Valid @RequestBody PatientSignupRequest patientSignupRequest) {
        logger.info("AuthController.registerPatient({})", patientSignupRequest.getUsername());

        try {
            return ResponseEntity.ok(authService.registerPatient(patientSignupRequest));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/signup/doctor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerDoctor(@Valid @RequestBody DoctorSignupRequest doctorSignupRequest) {
        logger.info("AuthController.registerDoctor({})", doctorSignupRequest.getUsername());

        try {
            return ResponseEntity.ok(authService.registerDoctor(doctorSignupRequest));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/signup/nurse")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerNurse(@Valid @RequestBody NurseSignupRequest nurseSignupRequest) {
        logger.info("AuthController.registerNurse({})", nurseSignupRequest.getUsername());

        try {
            return ResponseEntity.ok(authService.registerNurse(nurseSignupRequest));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }
}