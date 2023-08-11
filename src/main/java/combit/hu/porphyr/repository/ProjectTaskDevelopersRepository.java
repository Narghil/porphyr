package combit.hu.porphyr.repository;

import java.util.List;

import combit.hu.porphyr.domain.ProjectDevelopersEntity;
import combit.hu.porphyr.domain.ProjectTaskDevelopersEntity;
import combit.hu.porphyr.domain.ProjectTasksEntity;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectTaskDevelopersRepository extends CrudRepository<ProjectTaskDevelopersEntity, Long>  {
    @NonNull List<ProjectTaskDevelopersEntity> findAll();
    @NonNull List<ProjectTaskDevelopersEntity> findAllByProjectTasksEntity( ProjectTasksEntity projectTasksEntity);
    @NonNull List<ProjectTaskDevelopersEntity> findAllByProjectDevelopersEntity( ProjectDevelopersEntity projectDevelopersEntity);
}
