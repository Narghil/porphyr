package combit.hu.porphyr.repository;

import combit.hu.porphyr.domain.DeveloperEntity;
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
    ProjectTaskDeveloperEntity findAllByProjectTaskEntityAndDeveloperEntity(
        final @NonNull ProjectTaskEntity projectTaskEntity,
        final @NonNull DeveloperEntity developerEntity
    );

    @NonNull
    List<ProjectTaskDeveloperEntity> findAllByProjectTaskEntity(ProjectTaskEntity projectTaskEntity);

    @NonNull
    List<ProjectTaskDeveloperEntity> findAllByDeveloperEntity(DeveloperEntity developerEntity);

    void saveAndFlush(final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloperEntity);

    /* Native: */ /*
    @Query( value = "SELECT ProjectTasksDevelopers.* " +
        "FROM ProjectTaskDevelopers, ProjectTasks " +
        "WHERE ProjectTaskDevelopers.Developer_Id = :developerId and " +
        "   ProjectTaskDevelopers.ProjectTask_Id = ProjectTasks.Id  and " +
        "   ProjectTasks.Project_Id = :projectID ",
        nativeQuery = true )
    */ /* JPQL: */
    @Query("SELECT ProjectTaskDeveloper " +
        "FROM ProjectTaskEntity ProjectTask, ProjectTaskDeveloperEntity ProjectTaskDeveloper " +
        "WHERE " +
        "ProjectTaskDeveloper.developerEntity.id = :developerId and " +
        "ProjectTaskDeveloper.projectTaskEntity.id = ProjectTask.id and " +
        "ProjectTask.projectEntity.id = :projectId"
    )
    @NonNull
    List<ProjectTaskDeveloperEntity> findProjectTasksDeveloperByProjectIdAndDeveloperId(
        Long projectId,
        Long developerId
    );

    @Query("SELECT ProjectTaskDeveloper " +
        "FROM ProjectTaskDeveloperEntity ProjectTaskDeveloper " +
        "WHERE " +
        "ProjectTaskDeveloper.developerEntity.id = :developerId "
    )
    @NonNull
    List<ProjectTaskDeveloperEntity> findProjectTasksDeveloperByDeveloperId(Long developerId);

    //---------- SPEND_TIME sums ----------------------------------------------------------

    @Query(
        "SELECT COALESCE(SUM( ProjectTaskDeveloper.spendTime ),0) " +
            "FROM ProjectTaskEntity ProjectTask, ProjectTaskDeveloperEntity ProjectTaskDeveloper " +
            "WHERE " +
            "ProjectTask.projectEntity.id = :projectId and " +
            "ProjectTaskDeveloper.projectTaskEntity.id = ProjectTask.id and " +
            "ProjectTaskDeveloper.developerEntity.id = :developerId "
    )
    @NonNull
    Long sumSpendTimeByDeveloperIdAndProjectId(final @NonNull Long developerId, final @NonNull Long projectId);

    @Query(
        "SELECT COALESCE(SUM( ProjectTaskDeveloper.spendTime ),0) " +
            "FROM ProjectTaskEntity ProjectTask, ProjectTaskDeveloperEntity ProjectTaskDeveloper " +
            "WHERE " +
            "ProjectTask.projectEntity.id = :projectId and " +
            "ProjectTaskDeveloper.projectTaskEntity.id = ProjectTask.id "
    )
    @NonNull
    Long sumSpendTimeByProjectId(final @NonNull Long projectId);

    @Query(
        "SELECT COALESCE(SUM( ProjectTaskDeveloper.spendTime ),0) " +
            "FROM ProjectTaskDeveloperEntity ProjectTaskDeveloper " +
            "WHERE " +
            "ProjectTaskDeveloper.developerEntity.id = :developerId  "
    )
    @NonNull
    Long sumSpendTimeByDeveloperId(final @NonNull Long developerId);
}
