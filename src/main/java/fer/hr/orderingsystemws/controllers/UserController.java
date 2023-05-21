package fer.hr.orderingsystemws.controllers;

import com.google.gson.Gson;
import fer.hr.orderingsystemws.payload.request.UpdateUserRequest;
import fer.hr.orderingsystemws.payload.response.MessageResponse;
import fer.hr.orderingsystemws.security.services.UserService;
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
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final Gson gson = new Gson();
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
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

    @GetMapping("/users/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUser() {
        logger.info("UserController.getUser()");

        try {
            return ResponseEntity.ok(userService.getUser());
        } catch (Error e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/users/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UpdateUserRequest updateUserRequest) {
        logger.info("UserController.updateUser({})", gson.toJson(updateUserRequest));

        try {
            return ResponseEntity.ok(userService.updateUser(updateUserRequest));
        } catch (Error e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // Appointments

    @GetMapping("/users/me/appointments")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<?> getMyAppointments() {
        logger.info("UserController.getMyAppointments()");

        try {
            return ResponseEntity.ok(userService.getMyAppointments());
        } catch (Error e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/users/me/available-appointments")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<?> getAvailableAppointments() {
        logger.info("UserController.getAvailableAppointments()");

        try {
            return ResponseEntity.ok(userService.getAvailableAppointments());
        } catch (Error e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }


    @GetMapping("/users/me/available-appointments/nurse")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getAvailableAppointmentsForMedicalService() {
        logger.info("UserController.getAvailableAppointmentsForMedicalService()");

        try {
            return ResponseEntity.ok(userService.getAvailableAppointmentsForNurse());
        } catch (Error e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
