package combit.hu.porphyr.repository;

import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskDeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectTaskDeveloperRepository extends CrudRepository<ProjectTaskDeveloperEntity, Long>  {
    @NonNull List<ProjectTaskDeveloperEntity> findAll();
    @Nullable ProjectTaskDeveloperEntity findAllById(Long id);
    @Nullable ProjectTaskDeveloperEntity findAllByProjectTaskEntityAndProjectDeveloperEntity(
        final @NonNull ProjectTaskEntity projectTaskEntity,
        final @NonNull ProjectDeveloperEntity projectDeveloperEntity
    );
    @NonNull List<ProjectTaskDeveloperEntity> findAllByProjectTaskEntity( ProjectTaskEntity projectTaskEntity);
    @NonNull List<ProjectTaskDeveloperEntity> findAllByProjectDeveloperEntity( ProjectDeveloperEntity projectDeveloperEntity);
    void saveAndFlush( final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloperEntity);
}
