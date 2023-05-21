package fer.hr.orderingsystemws.models.appointments;

import fer.hr.orderingsystemws.models.medicalService.MedicalService;
import fer.hr.orderingsystemws.models.teams.MedicalTeam;
import fer.hr.orderingsystemws.models.users.MedicalPerson;
import fer.hr.orderingsystemws.models.users.Patient;
import lombok.Data;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Data
@Table(name = "appointments")
@Entity
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_time")
    private Date start;

    @Column(name = "end_time")
    private Date end;

    @ManyToOne
    @JoinColumn(name = "medical_person_id")
    private MedicalPerson medicalPerson;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "medical_team_id")
    private MedicalTeam medicalTeam;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "medical_service_id")
    private MedicalService medicalService;

    @Column(name = "patient_attended", columnDefinition = "boolean default NULL")
    private Boolean patientAttended;

    @Override
    public String toString() {
        SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat("dd.MM.yyyy.");
        simpleDateFormatDate.setTimeZone(TimeZone.getTimeZone("Europe/Zagreb"));
        String date = simpleDateFormatDate.format(start);

        SimpleDateFormat simpleDateFormatTime = new SimpleDateFormat("HH:mm");
        simpleDateFormatTime.setTimeZone(TimeZone.getTimeZone("Europe/Zagreb"));
        String startTime = simpleDateFormatTime.format(getStart());
        String endTime = simpleDateFormatTime.format(end);

        return date + " " + startTime + "-" + endTime;
    }
}
