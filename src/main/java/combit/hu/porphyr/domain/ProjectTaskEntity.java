package combit.hu.porphyr.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * A projektekhez tartozó feladatok nyilvántartása <br />
 * <br />
 * Mezők: <br />
 * {@code - id:} &#9;&#9;&#9; Egyedi azonosító <br />
 * {@code - projectEntity:} &#9;&#9; A projekt, amihez a feladat tartozik <br />
 * {@code - name:} &#9;&#9;&#9; A projekt neve. A projectEntity-n belül egyedi <br />
 * {@code - description:} &#9;&#9; A projekt leírása <br />
 * {@code - projectTaskDevelopers:} &#9; Az aktuális feladathoz tartozó ProjectDeveloperEntity összerendelések.
 * <br />
 * @see ProjectEntity
 * @see ProjectDeveloperEntity
 * @see ProjectTaskDeveloperEntity
 */

@Entity
@Table(name = "PROJECTTASKS")
@Data
public class ProjectTaskEntity {
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private @Nullable Long id;

    @Column
    private @NonNull String name;

    @Column(columnDefinition = "CLOB")
    private @Nullable String description;

    @Column(name = "spend_time")
    private @Nullable Long spendTime;

    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonManagedReference
    @NonNull
    private ProjectEntity projectEntity;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "projectTaskEntity")
    @JsonBackReference
    private @NonNull List<ProjectTaskDeveloperEntity> projectTaskDevelopers;

    @Setter
    @Getter
    private @NonNull Long developerSpendTime;

    public ProjectTaskEntity(
        final @NonNull ProjectEntity projectEntity,
        final @NonNull String name,
        final @Nullable String description
    ) {
        this.projectEntity = projectEntity;
        this.name = name;
        this.description = description;
        this.projectTaskDevelopers = new ArrayList<>();
        this.spendTime = 0L;
    }

    public ProjectTaskEntity() {
        this.projectTaskDevelopers = new ArrayList<>();
        projectEntity = new ProjectEntity();
        name = "";
        this.spendTime = 0L;
    }
}
