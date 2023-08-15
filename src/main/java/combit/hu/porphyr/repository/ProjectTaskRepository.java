package combit.hu.porphyr.repository;

import combit.hu.porphyr.domain.ProjectTasksEntity;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectTaskRepository extends CrudRepository<ProjectTasksEntity, Long> {

    @NonNull List<ProjectTasksEntity> findAll();
    @Nullable ProjectTasksEntity findAllById( final @NonNull Long id);
    @NonNull List<ProjectTasksEntity> findAllByName(final @NonNull String name);
    @NonNull List<ProjectTasksEntity> findAllByNameAndIdNot(final @NonNull String name, final @NonNull Long id);
}


