package combit.hu.porphyr.repository;

import java.util.List;

import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import combit.hu.porphyr.domain.ProjectEntity;

@Repository
public interface ProjectRepository extends CrudRepository<ProjectEntity, Long> {

    @NonNull List<ProjectEntity> findAll();
    @NonNull ProjectEntity findAllById(Long id);
    @NonNull List<ProjectEntity> findAllByName(String name);

}
