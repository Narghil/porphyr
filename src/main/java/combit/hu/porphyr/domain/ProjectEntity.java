package combit.hu.porphyr.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private @Nullable Long id;
    @Column
    private @NonNull String name;
    @Column(columnDefinition = "CLOB")
    private @Nullable String description;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "projectEntity")
    private List<ProjectTasksEntity> tasks;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "projectEntity")
    private List<ProjectDevelopersEntity> developers;

    public ProjectEntity(final @NonNull String name, final @Nullable String description) {
        this.name = name;
        this.description = description;
        this.tasks = new ArrayList<>();
    }

}

