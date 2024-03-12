package combit.hu.porphyr.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A fejlesztők adatainak nyilvántartása. <br/>
 * <br/>
 * Mezők:<br/>
 * {@code - id:} &#9;&#9;&#9; Egyedi azonosító <br/>
 * {@code - name:} &#9;&#9;&#9; A fejlesztő neve. Egyedi. <br/>
 * {@code - developerProjects:} &#9; A projektek listája, amelyekhez a fejlesztő hozzá van rendelve. <br/>
 * <br/>
 *
 * @see ProjectDeveloperEntity
 */

@Entity
@Table(name = "DEVELOPERS")
@Data
public class DeveloperEntity {
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private @Nullable Long id;

    @Column
    private @NonNull String name;

    @JsonBackReference
    @OneToMany(mappedBy = "developerEntity")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private @NonNull List<ProjectDeveloperEntity> developerProjects;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_developers",
        joinColumns = {@JoinColumn(name = "developer_id")},
        inverseJoinColumns = {@JoinColumn(name = "user_id")}
    )
    private Set<UserEntity> users = new HashSet<>();

    @Transient
    private @NonNull Long spendTime;

    public DeveloperEntity(final @NonNull String name) {
        this.name = name;
        this.developerProjects = new ArrayList<>();
        this.spendTime = 0L;
    }

    public DeveloperEntity() {
        name = "";
        developerProjects = new ArrayList<>();
        this.spendTime = 0L;
    }
}

