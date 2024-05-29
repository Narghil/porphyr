package combit.hu.porphyr.config.domain;

import combit.hu.porphyr.domain.DeveloperEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
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
@Table(name = "users")
@Data
@AllArgsConstructor
public class UserEntity {
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private @Nullable Long id;

    @Column(unique = true)
    private @NonNull String email;

    @Column(nullable = false)
    private @NonNull String password;

    @Column(nullable = false)
    private @NonNull String fullName;

    @Column(nullable = false, unique = true)
    private @NonNull String loginName;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.EAGER)  //EAGER nélkül a GrantedAuthority.getAuthorities() nem működik.
    @JoinTable(
        name = "users_roles",
        joinColumns = {@JoinColumn(name = "user_id")},
        inverseJoinColumns = {@JoinColumn(name = "role_id")}
    )
    private @NonNull Set<RoleEntity> roles;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany()
    @JoinTable(
        name = "users_developers",
        joinColumns = {@JoinColumn(name = "user_id")},
        inverseJoinColumns = {@JoinColumn(name = "developer_id")}
    )
    private @NonNull Set<DeveloperEntity> developers;

    @Transient
    private @Nullable String newPassword;
    @Transient
    private @Nullable String retypedPassword;

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
            ", loginName=" + loginName +
            ", fullName=" + fullName +
            ", email=" + email +
            ", password=" + password +
            ", newPassword=" + newPassword +
            ", retypedPassword=" + retypedPassword +
            "]"
            ;
    }

    @Setter
    @Getter
    public static class Pojo {
        private @Nullable Long id;
        private @Nullable String email;
        private @Nullable String password;
        private @Nullable String fullName;
        private @Nullable String loginName;
        private @Nullable String newPassword;
        private @Nullable String retypedPassword;
    }

    public UserEntity(final @NonNull Pojo pojo) {
        this.id = pojo.id;
        this.email = Objects.requireNonNull(pojo.email);
        this.password = Objects.requireNonNull(pojo.password);
        this.fullName = Objects.requireNonNull(pojo.fullName);
        this.loginName = Objects.requireNonNull(pojo.loginName);
        this.newPassword = pojo.newPassword;
        this.retypedPassword = pojo.retypedPassword;
        this.roles = new HashSet<>();
        this.developers = new HashSet<>();
    }

    public void readPojo(final @NonNull Pojo pojo) {
        this.id = pojo.id;
        this.email = Objects.requireNonNull(pojo.email);
        this.password = Objects.requireNonNull(pojo.password);
        this.fullName = Objects.requireNonNull(pojo.fullName);
        this.loginName = Objects.requireNonNull(pojo.loginName);
        this.newPassword = pojo.newPassword;
        this.retypedPassword = pojo.retypedPassword;
    }
}