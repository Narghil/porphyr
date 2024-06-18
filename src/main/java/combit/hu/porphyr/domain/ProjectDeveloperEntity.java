package combit.hu.porphyr.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;

/**
 * A projektek és a fejlesztők összerendelése <br/>
 * <br/>
 * Mezők: <br/>
 * {@code - id:} &#9;&#9;&#9; Egyedi azonosító <br/>
 * {@code - projectEntity:} &#9;&#9; A projekt. <br/>
 * {@code - developerEntity:} &#9;&#9; A fejlesztő. <br/>
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    @JsonManagedReference
    @NonNull
    ProjectEntity projectEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "developer_id")
    @JsonManagedReference
    @NonNull
    DeveloperEntity developerEntity;

    @Transient
    private @NonNull Long spendTime;

    public ProjectDeveloperEntity(final @NonNull ProjectEntity project, final @NonNull DeveloperEntity developer) {
        projectEntity = project;
        developerEntity = developer;
        spendTime = 0L;
    }

    public ProjectDeveloperEntity() {
        projectEntity = new ProjectEntity();
        developerEntity = new DeveloperEntity();
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
