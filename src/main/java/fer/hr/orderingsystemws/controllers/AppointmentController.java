package fer.hr.orderingsystemws.controllers;

import com.google.gson.Gson;
import fer.hr.orderingsystemws.payload.request.ConfirmPatientAttendanceRequest;
import fer.hr.orderingsystemws.payload.request.CreateAppointmentRequest;
import fer.hr.orderingsystemws.payload.request.ReserveAppointmentRequest;
import fer.hr.orderingsystemws.services.AppointmentService;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

    private final Gson gson = new Gson();
    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
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

    @PostMapping("/create")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<?> createAppointment(@Valid @RequestBody List<CreateAppointmentRequest> createAppointmentRequest) {
        logger.info("AppointmentController.createAppointment({})", gson.toJson(createAppointmentRequest));

        try {
            for(CreateAppointmentRequest request : createAppointmentRequest) {
                appointmentService.createAppointment(request);
            }
            return ResponseEntity.ok("Termini uspješno kreirani");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reserve")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> reserveAppointment(@Valid @RequestBody ReserveAppointmentRequest reserveAppointmentRequest) {
        logger.info("AppointmentController.reserveAppointment({})", gson.toJson(reserveAppointmentRequest));

        try {
            return ResponseEntity.ok(appointmentService.reserveAppointment(reserveAppointmentRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> cancelAppointment(@PathVariable("appointmentId") Long appointmentId) {
        logger.info("AppointmentController.cancelAppointment({})", appointmentId);

        try {
            return ResponseEntity.ok(appointmentService.cancelAppointment(appointmentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{appointmentId}/postpone")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<?> postponeAppointment(@PathVariable Long appointmentId) {
        logger.info("AppointmentController.postponeAppointment({})", appointmentId);

        try {
            return ResponseEntity.ok(appointmentService.postponeAppointment(appointmentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<?> confirmAppointment(@Valid @RequestBody ConfirmPatientAttendanceRequest confirmPatientAttendanceRequest) {
        logger.info("AppointmentController.confirmAppointment({})", confirmPatientAttendanceRequest.getAppointmentId());

        try {
            return ResponseEntity.ok(appointmentService.confirmAppointment(confirmPatientAttendanceRequest.getAppointmentId(), confirmPatientAttendanceRequest.isAttended()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
