package combit.hu.porphyr.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * Bejegyzések (post-ok) nyilvántartása <br/>
 * Mezők: <br />
 * {@code - id:} &#9;&#9;&#9; Egyedi azonosító <br />
 * {@code - created:} &#9;&#9; A bejegyzés létrehozásának létrehozása <br />
 * {@code - projectTaskEntity:} &#9; A feladat, amihez a bejegyzés tartozik <br />
 * {@code - projectTaskDeveloper:} &#9; A fejlesztő, aki a bejegyzést létrehozta <br />
 * {@code - description:} &#9;&#9; A bejegyzés szövege <br />
 * <br />
 *
 * @see RoleEntity
 */
@Entity
@Table(name = "posts")
@Data
@AllArgsConstructor
public class PostEntity {
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private @Nullable Long id;

    @Column(nullable = false)
    private @NonNull Timestamp created;

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

    @Column(columnDefinition = "CLOB")
    private @Nullable String description;

    public PostEntity() {
        this.created = Timestamp.from(Instant.now());
        this.projectTaskEntity = new ProjectTaskEntity();
        this.developerEntity = new DeveloperEntity();
        this.description = "";
    }

    @Override
    public String toString() {
        return "Post [id=" + id +
            ", created=" + created +
            ", projectTask=" + projectTaskEntity.getName() +
            ", developer=" + developerEntity.getName() +
            ", description=" + description +
            "]"
            ;
    }

    public void setProjectTaskAndDeveloper( final @NonNull ProjectTaskEntity projectTaskEntity, final @NonNull DeveloperEntity developerEntity){
        setProjectTaskEntity(projectTaskEntity);
        setDeveloperEntity(developerEntity);
    }
}
