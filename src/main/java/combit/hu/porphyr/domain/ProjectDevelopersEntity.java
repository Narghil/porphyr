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
@Table(name = "PROJECTDEVELOPERS")
@NoArgsConstructor
@Data
public class ProjectDevelopersEntity {
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private @Nullable Long id;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn( name = "project_id")
    @JsonManagedReference
    ProjectEntity projectEntity;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn( name = "developer_id")
    @JsonManagedReference
    DeveloperEntity developerEntity;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "projectDevelopersEntity")
    @JsonBackReference
    private List<ProjectTaskDevelopersEntity> projectTaskDevelopers;

    public ProjectDevelopersEntity(final @NonNull ProjectEntity projectEntity, final @NonNull DeveloperEntity developerEntity) {
        this.projectEntity = projectEntity;
        this.developerEntity = developerEntity;
    }
}
