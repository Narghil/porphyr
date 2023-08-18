package combit.hu.porphyr.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
import java.util.List;

@Entity
@Table(name = "PROJECTTASKS")
@NoArgsConstructor
@Data
public class ProjectTasksEntity {
    @Setter(AccessLevel.NONE)
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
    @JsonManagedReference
    ProjectEntity projectEntity;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "projectTasksEntity")
    @JsonBackReference
    private List<ProjectTaskDevelopersEntity> projectTaskDevelopers;

    public ProjectTasksEntity(final @NonNull ProjectEntity projectEntity, final @NonNull String name, final @Nullable String description) {
        this.projectEntity = projectEntity;
        this.name = name;
        this.description = description;
    }

}
