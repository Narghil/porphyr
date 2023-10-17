package combit.hu.porphyr.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * A fejlesztők adatainak nyilvántartása. <br/>
 * <br/>
 * Mezők:<br/>
 * {@code - id:} &#9;&#9;&#9; Egyedi azonosító <br/>
 * {@code - name:} &#9;&#9;&#9; A fejlesztő neve. Egyedi. <br/>
 * {@code - developerProjects:} &#9; A projektek listája, amelyekhez a fejlesztő hozzá van rendelve. <br/>
 * <br/>
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

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "developerEntity")
    @JsonBackReference
    private @NonNull List<ProjectDeveloperEntity> developerProjects;

    public DeveloperEntity(final @NonNull String name) {
        this.name = name;
        this.developerProjects = new ArrayList<>();
    }

    public DeveloperEntity() {
        name = "";
        developerProjects = new ArrayList<>();
    }
}

