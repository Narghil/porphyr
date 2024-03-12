package combit.hu.porphyr.repository;

import java.util.List;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import combit.hu.porphyr.domain.ProjectEntity;

@Repository
public interface ProjectRepository extends CrudRepository<ProjectEntity, Long> {

    @NonNull List<ProjectEntity> findAll();
    @Nullable ProjectEntity findAllById(final @NonNull Long id);
    @NonNull List<ProjectEntity> findAllByName(final @NonNull String name);
    @NonNull List<ProjectEntity> findAllByNameAndIdNot(final @NonNull String name, final @NonNull Long id);
    void saveAndFlush( final @NonNull ProjectEntity projectEntity);

    @Query(
        "SELECT COALESCE(SUM( ProjectTaskDeveloper.spendTime ),0) " +
            "FROM ProjectTaskEntity ProjectTask, ProjectTaskDeveloperEntity ProjectTaskDeveloper " +
            "WHERE " +
            "ProjectTask.projectEntity.id = :projectId and " +
            "ProjectTaskDeveloper.projectTaskEntity.id = ProjectTask.id "
    )
    @NonNull Long sumSpendTimeByProjectId( final @NonNull Long projectId );
}
