package combit.hu.porphyr.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
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

@Entity
@Table(name = "PROJECTS")
@NoArgsConstructor
@Data
public class ProjectEntity {
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private @Nullable Long id;
    @Column
    private @NonNull String name;
    @Column(columnDefinition = "CLOB")
    private @Nullable String description;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "projectEntity")
    @JsonBackReference
    private List<ProjectTasksEntity> projectTasks;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "projectEntity")
    @JsonBackReference
    private List<ProjectDevelopersEntity> projectDevelopers;

    public ProjectEntity(final @NonNull String name, final @Nullable String description) {
        this.name = name;
        this.description = description;
        this.projectTasks = new ArrayList<>();
    }

}

