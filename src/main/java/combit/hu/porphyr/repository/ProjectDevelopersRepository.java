package combit.hu.porphyr.repository;

import java.util.List;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectDevelopersEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectDevelopersRepository extends CrudRepository<ProjectDevelopersEntity, Long>  {
    @NonNull List<ProjectDevelopersEntity> findAll();
    @NonNull List<ProjectDevelopersEntity> findAllByProjectEntity(ProjectEntity projectEntity);
    @NonNull List<ProjectDevelopersEntity> findAllByDeveloperEntity(DeveloperEntity developerEntity);
}
