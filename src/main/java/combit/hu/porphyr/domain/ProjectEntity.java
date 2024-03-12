package combit.hu.porphyr.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * A projektek adatainak nyilvántartása.<br/>
 * <br/>
 * Mezők:<br/>
 * {@code - id:} &#9;&#9;&#9; Egyedi azonosító <br/>
 * {@code - name:} &#9;&#9;&#9; A projekt neve. Egyedi mező <br/>
 * {@code - description:} &#9;&#9; A projekt leírása <br/>
 * {@code - projectTasks:} &#9;&#9; A projekthez tartotó feladatok listája. <br/>
 * {@code - projectDevelopers:} &#9; A projekthez rendelt fejlesztők listája <br/>
 * <br/>
 *
 * @see ProjectTaskEntity
 * @see ProjectDeveloperEntity
 */

@Entity
@Table(name = "PROJECTS")
@Data
public class ProjectEntity {
    @Setter //--(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private @Nullable Long id;

    @Column
    private @NonNull String name;

    @Column(columnDefinition = "CLOB")
    private @Nullable String description;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "projectEntity")
    @JsonBackReference
    private @NonNull List<ProjectTaskEntity> projectTasks;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "projectEntity")
    @JsonBackReference
    private @NonNull List<ProjectDeveloperEntity> projectDevelopers;

    @Transient
    private @NonNull Long spendTime;

    public ProjectEntity(final @NonNull String name, final @Nullable String description) {
        this.name = name;
        this.description = description;
        this.projectTasks = new ArrayList<>();
        this.projectDevelopers = new ArrayList<>();
        this.spendTime = 0L;
    }

    public ProjectEntity() {
        name = "";
        this.projectTasks = new ArrayList<>();
        this.projectDevelopers = new ArrayList<>();
        this.spendTime = 0L;
    }
}

