package combit.hu.porphyr.repository;

import combit.hu.porphyr.domain.DeveloperEntity;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeveloperRepository extends CrudRepository<DeveloperEntity, Long> {

    @NonNull List<DeveloperEntity> findAll();
    @Nullable DeveloperEntity findAllById(final @NonNull Long id);
    @NonNull List<DeveloperEntity> findAllByName(final @NonNull String name);
    @NonNull List<DeveloperEntity> findAllByNameAndIdNot(final @NonNull String name, final @NonNull Long id);
    void saveAndFlush( final @NonNull DeveloperEntity developerEntity);

    @Query(
        "SELECT COALESCE(SUM( ProjectTaskDeveloper.spendTime ),0) " +
            "FROM ProjectTaskDeveloperEntity ProjectTaskDeveloper, ProjectDeveloperEntity ProjectDeveloper " +
            "WHERE " +
            "ProjectDeveloper.developerEntity.id = :developerId and " +
            "ProjectTaskDeveloper.projectDeveloperEntity.id = ProjectDeveloper.id "
    )
    @NonNull Long sumSpendTimeByDeveloperId( final @NonNull Long developerId );

}
