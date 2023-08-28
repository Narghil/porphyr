package combit.hu.porphyr.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A fejlesztők adatainak nyilvántartása.
 */
@Entity
@Table(name = "DEVELOPERS")
@Data
@NoArgsConstructor
public class DeveloperEntity {
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private @Nullable Long id;

    @Column
    private @NonNull String name;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "developerEntity")
    @JsonBackReference
    private @NonNull List<ProjectDeveloperEntity> developerProjects;

    public DeveloperEntity(final @NonNull String name) {
        this.id = null;
        this.name = name;
        this.developerProjects = new ArrayList<>();
    }

}

