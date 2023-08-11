package combit.hu.porphyr.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
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
import java.util.List;

@Entity
@Table(name = "PROJECTTASKS")
@NoArgsConstructor
@Data
public class ProjectTasksEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private @Nullable Long id;
    @Column
    private @NonNull String name;
    @Column(columnDefinition = "CLOB")
    private @Nullable String description;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn( name = "project_id")
    ProjectEntity projectEntity;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "projectTasksEntity")
    private List<ProjectTaskDevelopersEntity> projectTaskDevelopers;

    public ProjectTasksEntity(final @NonNull ProjectEntity projectEntity, final @NonNull String name, final @Nullable String description) {
        this.projectEntity = projectEntity;
        this.name = name;
        this.description = description;
    }

}
