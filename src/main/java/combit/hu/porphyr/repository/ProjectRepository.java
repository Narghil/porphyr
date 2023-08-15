package combit.hu.porphyr.repository;

import java.util.List;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import combit.hu.porphyr.domain.ProjectEntity;

@Repository
public interface ProjectRepository extends CrudRepository<ProjectEntity, Long> {

    @NonNull List<ProjectEntity> findAll();
    @Nullable ProjectEntity findAllById(final @NonNull Long id);
    @NonNull List<ProjectEntity> findAllByName(final @NonNull String name);
    @NonNull List<ProjectEntity> findAllByNameAndIdNot(final @NonNull String name, final @NonNull Long id);
}
