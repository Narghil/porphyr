package combit.hu.porphyr.repository;

import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectTaskRepository extends CrudRepository<ProjectTaskEntity, Long> {

    @NonNull List<ProjectTaskEntity> findAll();
    @Nullable ProjectTaskEntity findAllById( final @NonNull Long id);
    @Nullable ProjectTaskEntity findAllByProjectEntityAndName(final @NonNull ProjectEntity projectEntity, final @NonNull String name);
    @Nullable ProjectTaskEntity findAllByProjectEntityAndNameAndIdNot(final @NonNull ProjectEntity projectEntity, final @NonNull String name, final @NonNull Long id);
    void saveAndFlush( final @NonNull ProjectTaskEntity projectTaskEntity);
}


