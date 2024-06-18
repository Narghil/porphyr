package combit.hu.porphyr.repository;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectDeveloperRepository extends CrudRepository<ProjectDeveloperEntity, Long> {
    @Nullable
    ProjectDeveloperEntity findAllById(final @NonNull Long id);

    @Nullable
    ProjectDeveloperEntity findAllByProjectEntityAndDeveloperEntity(
        final @NonNull ProjectEntity projectEntity,
        final @NonNull DeveloperEntity developerEntity
    );

    @NonNull
    List<ProjectDeveloperEntity> findAll();

    @NonNull
    List<ProjectDeveloperEntity> findAllByProjectEntity(final @NonNull ProjectEntity projectEntity);

    @NonNull
    List<ProjectDeveloperEntity> findAllByDeveloperEntity(final @NonNull DeveloperEntity developerEntity);

    void saveAndFlush(final @NonNull ProjectDeveloperEntity projectDeveloperEntity);
}
