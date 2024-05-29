package combit.hu.porphyr.repository;

import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskDeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectTaskDeveloperRepository extends CrudRepository<ProjectTaskDeveloperEntity, Long> {
    @NonNull
    List<ProjectTaskDeveloperEntity> findAll();

    @Nullable
    ProjectTaskDeveloperEntity findAllById(Long id);

    @Nullable
    ProjectTaskDeveloperEntity findAllByProjectTaskEntityAndProjectDeveloperEntity(
        final @NonNull ProjectTaskEntity projectTaskEntity,
        final @NonNull ProjectDeveloperEntity projectDeveloperEntity
    );

    @NonNull
    List<ProjectTaskDeveloperEntity> findAllByProjectTaskEntity(ProjectTaskEntity projectTaskEntity);

    @NonNull
    List<ProjectTaskDeveloperEntity> findAllByProjectDeveloperEntity(ProjectDeveloperEntity projectDeveloperEntity);

    void saveAndFlush(final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloperEntity);

    /* Native: */ /*
    @Query( value = "SELECT ProjectTasksDevelopers.* " +
        "FROM ProjectDevelopers, ProjectTaskDevelopers, ProjectTasks " +
        "WHERE ProjectDevelopers.Project_Id = :projectId and ProjectDevelopers.Developer_Id = :developerId and " +
        "   ProjectDevelopers.Id = ProjectTaskDevelopers.ProjectDeveloper_Id and " +
        "   ProjectTaskDevelopers.ProjectTask_Id = ProjectTasks.Id  and " +
        "   ProjectTasks.Project_Id = ProjectDevelopers.Project_Id ",
        nativeQuery = true )
    */ /* JPQL: */
    @Query("SELECT ProjectTaskDeveloper " +
        "FROM ProjectTaskEntity ProjectTask, ProjectDeveloperEntity ProjectDeveloper, ProjectTaskDeveloperEntity ProjectTaskDeveloper " +
        "WHERE ProjectDeveloper.projectEntity.id = :projectId and " +
        "ProjectDeveloper.developerEntity.id = :developerId and " +
        "ProjectDeveloper.id = ProjectTaskDeveloper.projectDeveloperEntity.id and " +
        "ProjectTaskDeveloper.projectTaskEntity.id = ProjectTask.id and " +
        "ProjectTask.projectEntity.id = ProjectDeveloper.projectEntity.id "
    )
    @NonNull
    List<ProjectTaskDeveloperEntity> findProjectTasksDeveloperByProjectIdAndDeveloperId(
        Long projectId,
        Long developerId
    );

    @Query("SELECT ProjectTaskDeveloper " +
        "FROM ProjectTaskEntity ProjectTask, ProjectDeveloperEntity ProjectDeveloper, ProjectTaskDeveloperEntity ProjectTaskDeveloper " +
        "WHERE " +
        "ProjectDeveloper.developerEntity.id = :developerId and " +
        "ProjectDeveloper.id = ProjectTaskDeveloper.projectDeveloperEntity.id and " +
        "ProjectTaskDeveloper.projectTaskEntity.id = ProjectTask.id and " +
        "ProjectTask.projectEntity.id = ProjectDeveloper.projectEntity.id "
    )
    @NonNull
    List<ProjectTaskDeveloperEntity> findProjectTasksDeveloperByDeveloperId(Long developerId);
}
