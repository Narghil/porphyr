package combit.hu.porphyr.repository;

import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTasksEntity;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectTaskRepository extends CrudRepository<ProjectTasksEntity, Long> {
    @NonNull List<ProjectTasksEntity> findAll();

    @NonNull List<ProjectTasksEntity> findAllByProjectEntity(ProjectEntity projectEntity);
}


