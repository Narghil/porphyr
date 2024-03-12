package combit.hu.porphyr.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * A projektek és a fejlesztők összerendelése <br/>
 * <br/>
 * Mezők: <br/>
 * {@code - id:} &#9;&#9;&#9; Egyedi azonosító <br/>
 * {@code - projectEntity:} &#9;&#9; A projekt. <br/>
 * {@code - developerEntity:} &#9;&#9; A fejlesztő. <br/>
 * {@code - projectTaskDevelopers:} &#9; Az aktuális összerendeléshez tartozó projectTask hozzárendelések. <br/>
 * A projectEntity és a developerEntity együtt egyedi. <br/>
 * <br/>
 *
 * @see ProjectEntity
 * @see DeveloperEntity
 * @see ProjectTaskEntity
 * @see ProjectTaskDeveloperEntity
 */

@Entity
@Table(name = "PROJECTDEVELOPERS")
@Data
public class ProjectDeveloperEntity {
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private @Nullable Long id;

    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonManagedReference
    @NonNull
    ProjectEntity projectEntity;

    @ManyToOne
    @JoinColumn(name = "developer_id")
    @JsonManagedReference
    @NonNull
    DeveloperEntity developerEntity;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "projectDeveloperEntity")
    @JsonBackReference
    private @NonNull List<ProjectTaskDeveloperEntity> projectTaskDevelopers;

    @Transient
    private @NonNull Long spendTime;

    public ProjectDeveloperEntity(final @NonNull ProjectEntity project, final @NonNull DeveloperEntity developer) {
        projectEntity = project;
        developerEntity = developer;
        projectTaskDevelopers = new ArrayList<>();
        spendTime = 0L;
    }

    public ProjectDeveloperEntity() {
        projectEntity = new ProjectEntity();
        developerEntity = new DeveloperEntity();
        projectTaskDevelopers = new ArrayList<>();
        spendTime = 0L;
    }

    public void setProjectAndDeveloper(
        final @NonNull ProjectEntity projectEntity,
        final @NonNull DeveloperEntity developerEntity
    ) {
        setProjectEntity(projectEntity);
        setDeveloperEntity(developerEntity);
    }
}
