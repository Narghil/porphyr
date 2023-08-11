package combit.hu.porphyr.domain;

import lombok.Data;
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
import javax.persistence.Table;

@Entity
@Table(name = "PROJECTTASKDEVELOPERS")
@NoArgsConstructor
@Data
public class ProjectTaskDevelopersEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private @Nullable Long id;
    @Column
    private @NonNull Long spendTime;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn( name = "projecttask_id")
    ProjectTasksEntity projectTasksEntity;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn( name = "projectdevelopers_id")
    ProjectDevelopersEntity projectDevelopersEntity;

}
