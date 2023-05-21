package fer.hr.orderingsystemws.models.role;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "userrole")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ERole name;

    @Column(length = 128)
    private String description;
}