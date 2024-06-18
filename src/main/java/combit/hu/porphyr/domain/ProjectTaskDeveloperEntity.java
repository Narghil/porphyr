package combit.hu.porphyr.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * A projektfeladatok és a projektfejlesztők összerendelése <br/>
 * <br/>
 * Mezők:  <br/>
 * - {@code id:} &#9;&#9;&#9                        Egyedi azonosító <br/>
 * - {@code projectTaskEntity:} &#9;&#9;            Projektfeladat <br/>
 * - {@code developerEntity:} &#9;                  Fejlesztő <br/>
 * A projectTaskEntity és a developerEntity együtt egyedi <br/>
 * <br/>
 *
 * @see ProjectTaskEntity
 * @see DeveloperEntity
 */

@Entity
@Table(name = "PROJECTTASKDEVELOPERS")
@Data
public class ProjectTaskDeveloperEntity {
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private @Nullable Long id;
    @Column(name = "spend_time")
    private @NonNull Long spendTime;

    @ManyToOne
    @JoinColumn(name = "projecttask_id")
    @JsonManagedReference
    @NonNull
    private ProjectTaskEntity projectTaskEntity;

    @ManyToOne
    @JoinColumn(name = "developer_id")
    @JsonManagedReference
    @NonNull
    private DeveloperEntity developerEntity;

    public ProjectTaskDeveloperEntity() {
        spendTime = 0L;
        projectTaskEntity = new ProjectTaskEntity();
        developerEntity = new DeveloperEntity();
    }

    public void setProjectTaskAndDeveloper(
        ProjectTaskEntity projectTask,
        DeveloperEntity developer
    ) {
        setProjectTaskEntity(projectTask);
        setDeveloperEntity(developer);
    }
}
