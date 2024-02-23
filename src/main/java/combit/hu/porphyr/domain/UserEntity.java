package combit.hu.porphyr.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="users")
@Data
@AllArgsConstructor
public class UserEntity {
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private @Nullable Long id;

    @Column( unique=true)
    private @NonNull String email;

    @Column( nullable=false )
    private @NonNull String password;

    @Column( nullable=false)
    private @NonNull String fullName;

    @Column( nullable=false, unique=true)
    private @NonNull String loginName;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany()
    @JoinTable(
        name = "users_roles",
        joinColumns = {@JoinColumn(name="user_id")},
        inverseJoinColumns = {@JoinColumn(name="role_id")}
    )
    private Set<RoleEntity> roles;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany()
    @JoinTable(
        name = "users_developers",
        joinColumns = {@JoinColumn(name="user_id")},
        inverseJoinColumns = {@JoinColumn(name="developer_id")}
    )
    private Set<DeveloperEntity> developers;

    public UserEntity() {
        email = "";
        password = "";
        loginName = "";
        fullName = "";
        roles = new HashSet<>();
        developers = new HashSet<>();
    }

    @Override
    public String toString() {
        return "User [id=" + id +
            ", login="+loginName+
            ", name="+fullName+
            ", email=" + email +
            ", password=" + password +
            "]"
        ;
    }

}
