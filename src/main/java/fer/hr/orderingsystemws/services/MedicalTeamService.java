package fer.hr.orderingsystemws.services;

import fer.hr.orderingsystemws.models.appointments.Appointment;
import fer.hr.orderingsystemws.models.teams.MedicalTeam;
import fer.hr.orderingsystemws.models.users.Doctor;
import fer.hr.orderingsystemws.models.users.Nurse;
import fer.hr.orderingsystemws.models.users.User;
import fer.hr.orderingsystemws.payload.request.CreateMedicalTeamRequest;
import fer.hr.orderingsystemws.payload.response.MessageResponse;
import fer.hr.orderingsystemws.repository.AppointmentRepository;
import fer.hr.orderingsystemws.repository.MedicalTeamRepository;
import fer.hr.orderingsystemws.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

@Service
public class MedicalTeamService {
    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);
    UserRepository userRepository;
    AppointmentRepository appointmentRepository;
    MedicalTeamRepository medicalTeamRepository;

    @Autowired
    public MedicalTeamService(UserRepository userRepository, AppointmentRepository appointmentRepository, MedicalTeamRepository medicalTeamRepository) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.medicalTeamRepository = medicalTeamRepository;
    }

    public Object createMedicalTeam(CreateMedicalTeamRequest createMedicalTeamRequest) {

        logger.info("MedicalTeamService.createMedicalTeam({})", createMedicalTeamRequest);
        if (!userRepository.existsById(createMedicalTeamRequest.getDoctorId())) {
            throw new Error("Greška: Doktor ne postoji");
        }

        if (!userRepository.existsById(createMedicalTeamRequest.getNurseId())) {
            throw new Error("Greška: Medicinska sestra ne postoji");
        }

        if (!(userRepository.findById(createMedicalTeamRequest.getDoctorId()).get() instanceof Doctor doctor)) {
            throw new Error("Greška: Korisnik sa danim ID-em nije doktor");
        }

        if (!(userRepository.findById(createMedicalTeamRequest.getNurseId()).get() instanceof Nurse nurse)) {
            throw new Error("Greška: Korisnik sa danim ID-em nije medicinska sestra");
        }

        if (doctor.getMedicalTeam() != null) {
            throw new Error("Greška: Doktor je već dio nekog medicinskog tima");
        }

        if (nurse.getMedicalTeam() != null) {
            throw new Error("Greška: Medicinska sestra je već dio nekog medicinskog tima");
        }

        Optional<Appointment> optionalAppointment = appointmentRepository.findLastAppointmentForMedicalPersons(doctor.getId(), nurse.getId());

        Date validFrom = new Date();

        if (optionalAppointment.isPresent()) {
            // Medical team is valid from the day after the last appointment
            validFrom = new Date(optionalAppointment.get().getEnd().getTime() + 1);
        }

        MedicalTeam medicalTeam = new MedicalTeam(createMedicalTeamRequest.getName(), validFrom, doctor.getId(), nurse.getId());
        medicalTeamRepository.save(medicalTeam);
        userRepository.updateMedicalTeamIdById(medicalTeam.getId(), doctor.getId());
        userRepository.updateMedicalTeamIdById(medicalTeam.getId(), nurse.getId());

        SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat("dd.MM.yyyy. HH:mm");
        simpleDateFormatDate.setTimeZone(TimeZone.getTimeZone("Europe/Zagreb"));
        String date = simpleDateFormatDate.format(validFrom);

        return new MessageResponse("Medicinski tim je uspješno kreiran i počinje s radom " + date + " .");
    }

    public Object deleteMedicalTeam(Long id){
        logger.info("MedicalTeamService.deleteMedicalTeam({})", id);
        if (!medicalTeamRepository.existsById(id)) {
            throw new Error("Greška: Medicinski tim ne postoji");
        }

        List<Appointment> appointmentList = appointmentRepository.findAllMedicalTeamAppointments(id);
        if(appointmentList.isEmpty()){
            List<Appointment> finishedAppointmentList = appointmentRepository.findAllFinishedMedicalTeamAppointments(id);
            for(Appointment appointment : finishedAppointmentList){
                appointmentRepository.updateAppointmentMedicalTeam(appointment.getId(), null);
            }
            MedicalTeam medicalTeam = medicalTeamRepository.findById(id).get();
            Optional<User> nurse = userRepository.findById(medicalTeam.getNurseId());
            Optional<User> doctor = userRepository.findById(medicalTeam.getDoctorId());
            userRepository.updateMedicalTeamIdById(null, nurse.get().getId());
            userRepository.updateMedicalTeamIdById(null, doctor.get().getId());
            medicalTeamRepository.deleteById(id);

            return new MessageResponse("Medicinski tim je uspješno obrisan.");
        }
        else{
            throw new Error("Greška: Medicinski tim ima zakazane termine");
        }
    }

}
