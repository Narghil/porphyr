package combit.hu.porphyr.service;

import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTasksEntity;
import combit.hu.porphyr.repository.ProjectTaskRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Scope("prototype")
public class ProjectTaskService {

    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    public void setTaskRepository(ProjectTaskRepository projectTaskRepository) {
        this.projectTaskRepository = projectTaskRepository;
    }

    public @NonNull List<ProjectTasksEntity> getAllTasks() {
        return projectTaskRepository.findAll();
    }

    public @NonNull List<ProjectTasksEntity> getProjectTasks( ProjectEntity projectEntity) {
        return projectTaskRepository.findAllByProjectEntity( projectEntity );
    }

    public void insertNewTask(final @NonNull ProjectTasksEntity newProjectTasksEntity) {
        projectTaskRepository.save(newProjectTasksEntity);
    }

    public void deleteTask(final @NonNull Long taskID) {
        projectTaskRepository.deleteById(taskID);
    }

    public void deleteAllTasks() {
        projectTaskRepository.deleteAll();
    }

    public void modifyTask(final @NonNull ProjectTasksEntity modifiedTask) {
        projectTaskRepository.save(modifiedTask);
    }
}
