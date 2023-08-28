package combit.hu.porphyr.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AccessLevel;
import lombok.Data;
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
import javax.persistence.Table;

@Entity
@Table(name = "PROJECTTASKDEVELOPERS")
@NoArgsConstructor
@Data
public class ProjectTaskDeveloperEntity {
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private @Nullable Long id;
    @Column( name = "spend_time")
    private @NonNull Long spendTime;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn( name = "projecttask_id")
    @JsonManagedReference
    @NonNull
    private ProjectTaskEntity projectTaskEntity;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn( name = "projectdeveloper_id")
    @JsonManagedReference
    @NonNull
    private ProjectDeveloperEntity projectDeveloperEntity;
}
