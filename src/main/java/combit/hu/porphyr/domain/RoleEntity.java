package combit.hu.porphyr.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Jogosultságok nyilvántartása <br/>
 * <br />
 * Mezők: <br />
 * {@code - id:} &#9; Egyedi azonosító <br />
 * {@code - role:} &#9; A jogosultság neve (egyedi) <br />
 * <br />
 *
 * @see UserEntity
 */

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column
    private String role;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany()
    @JoinTable(
        name = "users_roles",
        joinColumns = {@JoinColumn(name = "role_id")},
        inverseJoinColumns = {@JoinColumn(name = "user_id")}
    )
    private Set<UserEntity> users = new HashSet<>();

    @Override
    public String toString() {
        return "Role [id=" + id + ", role=" + role + "]";
    }

    public RoleEntity(final @NonNull String roleName) {
        this.role = roleName;
    }
}
