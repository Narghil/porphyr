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

/**
 * Felhasználók: nyilvántartása <br/>
 * Egy USER-hez több ROLE és több DEVELOPER is rendelhető. <br/>
 * Nem kötelező, hogy egy USER egyben DEVELOPER is legyen. Ilyen pl. az ADMIN user. <br/> <br/>
 * Mezők: <br />
 * {@code - id:} &#9;&#9; Egyedi azonosító <br />
 * {@code - email:} &#9;&#9; E-mail cím (egyedi) <br />
 * {@code - loginName:} &#9; belépési név (egyed) <br />
 * {@code - password:} &#9; jelszó (bcrypt kódolással) <br />
 * {@code - fullName:} &#9; Teljes név <br />
 * <br />
 *
 * @see RoleEntity
 */
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
    @ManyToMany(fetch = FetchType.EAGER)  //EAGER nélkül a GrantedAuthority.getAuthorities() nem működik.
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
