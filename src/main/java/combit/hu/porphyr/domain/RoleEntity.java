package combit.hu.porphyr.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table( name="roles" )
@Data
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column
    private String role;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.EAGER )
    @JoinTable(
        name = "users_roles",
        joinColumns = {@JoinColumn(name="role_id")},
        inverseJoinColumns = {@JoinColumn(name="user_id")}
    )
    private Set<UserEntity> users = new HashSet<>();

    @Override
    public String toString() {
        return "Role [id=" + id + ", role=" + role + "]";
    }

}
