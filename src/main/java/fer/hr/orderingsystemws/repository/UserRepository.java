package fer.hr.orderingsystemws.repository;


import fer.hr.orderingsystemws.models.users.MedicalPerson;
import fer.hr.orderingsystemws.models.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    @Query(
            value = "SELECT * " +
                    "FROM users u " +
                        "JOIN userrole r ON u.role_id = r.id " +
                    "WHERE r.name = :roleName",
            nativeQuery = true
    )
    List<User> findAllByRoleIs(@Param("roleName") String roleName);

    List<User> findAll();

    @Query(
            value = "SELECT * " +
                    "FROM users u " +
                    "WHERE team_id = :teamId",
            nativeQuery = true
    )
    List<MedicalPerson> findAllByTeamId(Long teamId);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Boolean existsByOib(String oib);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE users " +
                    "SET firstname = :firstname " +
                    "WHERE id = :id",
            nativeQuery = true
    )
    int updateFirstnameById(String firstname, Long id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE users " +
                    "SET lastname = :lastname " +
                    "WHERE id = :id",
            nativeQuery = true
    )
    int updateLastnameById(String lastname, Long id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE users " +
                    "SET oib = :oib " +
                    "WHERE id = :id",
            nativeQuery = true
    )
    int updateOibById(String oib, Long id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE users " +
                    "SET username = :username " +
                    "WHERE id = :id",
            nativeQuery = true
    )
    int updateUsernameById(String username, Long id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE users " +
                    "SET email = :email " +
                    "WHERE id = :id",
            nativeQuery = true
    )
    int updateEmailById(String email, Long id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE users " +
                    "SET phone_number = :phoneNumber " +
                    "WHERE id = :id",
            nativeQuery = true
    )
    int updatePhoneNumberById(String phoneNumber, Long id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE users " +
                    "SET password = :password " +
                    "WHERE id = :id",
            nativeQuery = true
    )
    int updatePasswordById(String password, Long id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE users " +
                    "SET gender = :gender " +
                    "WHERE id = :id",
            nativeQuery = true
    )
    int updateGenderById(String gender, Long id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE users " +
                    "SET notification = :notificationSettings " +
                    "WHERE id = :id",
            nativeQuery = true
    )
    int updateNotificationSettingsById(int notificationSettings, Long id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE users " +
                    "SET reservation_rule = :reservationRule " +
                    "WHERE id = :id",
            nativeQuery = true
    )
    int updateReservationRuleById(int reservationRule, Long id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE users " +
                    "SET medical_team_id = :medicalTeamId " +
                    "WHERE id = :id",
            nativeQuery = true
    )
    int updateMedicalTeamIdById(Long medicalTeamId, Long id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE users " +
                    "SET disappearances = :newDisappearances " +
                    "WHERE id = :id",
            nativeQuery = true
    )
    int updateDisappearanceById(int newDisappearances, Long id);
}
