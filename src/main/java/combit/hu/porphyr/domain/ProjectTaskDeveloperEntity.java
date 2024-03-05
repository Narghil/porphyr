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
 * - {@code projectDeveloperEntity:} &#9;           Projektfejlesztő <br/>
 * A projectTaskEntity és a projectDeveloperEntity együtt egyedi <br/>
 * <br/>
 *
 * @see ProjectTaskEntity
 * @see ProjectDeveloperEntity
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
    @JoinColumn(name = "projectdeveloper_id")
    @JsonManagedReference
    @NonNull
    private ProjectDeveloperEntity projectDeveloperEntity;

    public ProjectTaskDeveloperEntity() {
        spendTime = 0L;
        projectTaskEntity = new ProjectTaskEntity();
        projectDeveloperEntity = new ProjectDeveloperEntity();
    }

    public void setProjectTaskAndProjectDeveloper(
        ProjectTaskEntity projectTask,
        ProjectDeveloperEntity projectDeveloper
    ) {
        setProjectTaskEntity(projectTask);
        setProjectDeveloperEntity(projectDeveloper);
    }
}
