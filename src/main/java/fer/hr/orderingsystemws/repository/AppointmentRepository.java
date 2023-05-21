package fer.hr.orderingsystemws.repository;

import fer.hr.orderingsystemws.models.appointments.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Optional<Appointment> findById(Long id);

    List<Appointment> findAll();

    @Query(
            value = "SELECT * " +
                    "FROM appointments " +
                    "WHERE patient_id = :id",
            nativeQuery = true
    )
    List<Appointment> findAllPatientAppointments(Long id);

    @Query(
            value = "SELECT * " +
                    "FROM appointments " +
                    "WHERE medical_person_id = :id AND patient_id IS NOT NULL",
            nativeQuery = true
    )
    List<Appointment> findAllMedicalPersonAppointments(Long id);

    @Query(
            value = "SELECT * " +
                    "FROM appointments " +
                    "WHERE patient_id IS NULL " +
                        "AND medical_person_id = :medicalPersonId " +
                        "AND start_time >= CURRENT_TIMESTAMP",
            nativeQuery = true
    )
    List<Appointment> findAvailableAppointmentsForMedicalPerson(Long medicalPersonId);

    @Query(
            value = "SELECT * " +
                    "FROM appointments " +
                    "WHERE patient_id IS NULL " +
                        "AND medical_team_id = :medicalTeamId " +
                        "AND start_time >= CURRENT_TIMESTAMP ",
            nativeQuery = true
    )
    List<Appointment> findAvailableAppointmentsForMedicalTeam(Long medicalTeamId);

    @Query(
            value = "SELECT * " +
                    "FROM appointments " +
                    "WHERE patient_id IS NULL " +
                        "AND start_time >= CURRENT_TIMESTAMP " +
                        "AND medical_service_id = :medicalServiceId " +
                    "ORDER BY start_time ASC",
            nativeQuery = true
    )
    List<Appointment> findAvailableAppointmentsForMedicalService(Long medicalServiceId);

    @Query(
            value = "SELECT * " +
                    "FROM appointments " +
                    "WHERE patient_id IS NULL " +
                        "AND (medical_person_id = :doctorId OR medical_person_id = :nurseId) " +
                        "AND start_time >= CURRENT_TIMESTAMP " +
                    "ORDER BY start_time DESC LIMIT 1",
            nativeQuery = true
    )
    Optional<Appointment> findLastAppointmentForMedicalPersons(Long doctorId, Long nurseId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE appointments " +
                    "SET patient_id = :patientId, medical_service_id = :medicalServiceId " +
                    "WHERE id = :appointmentId",
            nativeQuery = true
    )
    int reserveAppointment(Long appointmentId, Long patientId, Long medicalServiceId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE appointments " +
                    "SET patient_attended = :attended " +
                    "WHERE id = :appointmentId",
            nativeQuery = true
    )
    int confirmAppointment(Long appointmentId, boolean attended);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE appointments " +
                    "SET patient_id = NULL, medical_service_id = NULL , patient_attended = NULL " +
                    "WHERE id = :appointmentId",
            nativeQuery = true
    )
    int cancelDoctorAppointment(Long appointmentId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE appointments " +
                    "SET patient_id = NULL , patient_attended = NULL " +
                    "WHERE id = :appointmentId",
            nativeQuery = true
    )
    int cancelNurseAppointment(Long appointmentId);

    @Query(
            value = "SELECT * " +
                    "FROM appointments " +
                    "WHERE start_time > :start " +
                        "AND end_time <= :end " +
                        "AND patient_id IS NOT NULL",
            nativeQuery = true
    )
    List<Appointment> findAllAppointmentsBetweenTimestamps(Timestamp start, Timestamp end);

    @Query(
            value = "SELECT * " +
                    "FROM appointments " +
                    "WHERE start_time > :start " +
                        "AND end_time <= :end " +
                        "AND patient_id IS NOT NULL " +
                        "AND appointments.patient_attended = true",
            nativeQuery = true
    )
    List<Appointment> findAllAttendedAppointmentsBetweenTimestamps(Timestamp start, Timestamp end);

    @Query(
            value = "SELECT * " +
                    "FROM appointments " +
                    "WHERE start_time >= CURRENT_TIMESTAMP " +
                        "AND patient_id IS NOT NULL " +
                        "AND medical_team_id = :medicalTeamId",
            nativeQuery = true
    )
    List<Appointment> findAllMedicalTeamAppointments(Long medicalTeamId);

    @Query(
            value = "SELECT * " +
                    "FROM appointments " +
                    "WHERE end_time < CURRENT_TIMESTAMP " +
                        "AND medical_team_id = :medicalTeamId",
            nativeQuery = true
    )
    List<Appointment> findAllFinishedMedicalTeamAppointments(Long medicalTeamId);

    @Transactional
    @Modifying
    @Query(
            value = "UPDATE appointments " +
                    "SET medical_team_id = :medicalTeamId " +
                    "WHERE id = :appointmentId",
            nativeQuery = true
    )
    int updateAppointmentMedicalTeam(Long appointmentId, Long medicalTeamId);

    @Transactional
    @Modifying
    @Query(
            value = "UPDATE appointmetns " +
                    "SET patient_attended = false " +
                    "WHERE patient_id IS NOT NULL " +
                        "AND patient_attended IS NULL " +
                        "AND start_time < CURRENT_TIMESTAMP",
            nativeQuery = true
    )
    int updateUnresolvedAppointmentsAttendance();
}
