package combit.hu.porphyr.service;

import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import combit.hu.porphyr.repository.ProjectRepository;
import combit.hu.porphyr.repository.ProjectTaskRepository;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@Scope("prototype")
public class ProjectTaskService {

    @Autowired
    @Setter
    private EntityManager entityManager;

    @Autowired
    @Setter
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    @Setter
    private ProjectRepository projectRepository;

    /**
     * Hibalehetőségek: <br/>
     * - A project entity nem létező project -re mutat
     * - A task neve üres
     * - Ilyen nevű task már van a projekten belül!
     */
    public void insertNewProjectTask(final @NonNull ProjectTaskEntity newProjectTaskEntity) {
        ProjectEntity project = newProjectTaskEntity.getProjectEntity();
        String taskName = newProjectTaskEntity.getName();

        entityManager.detach(newProjectTaskEntity);
        if (project.getId() == null) {
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_INSERT_PROJECT_NOT_SAVED));
        } else if (projectRepository.findAllById(project.getId()) == null) {
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_INSERT_PROJECT_NOT_EXISTS));
        } else if (taskName.isEmpty()) {
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_INSERT_EMPTY_NAME));
        } else if (projectTaskRepository.findAllByProjectEntityAndName(project, taskName) != null) {
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_INSERT_SAME_PROJECT_AND_NAME));
        } else {
            projectTaskRepository.saveAndFlush(newProjectTaskEntity);
        }
    }

    /**
     * Hibalehetőségek: <br/>
     * - A task ID-je üres
     * - A project entity üres vagy nem létező project-re mutat
     * - A task neve üres
     * - Ilyen nevű task már van a projektben
     */
    public void modifyProjectTask(final @NonNull ProjectTaskEntity modifiedProjectTaskEntity) {
        Long taskId = modifiedProjectTaskEntity.getId();
        ProjectEntity project = modifiedProjectTaskEntity.getProjectEntity();
        String taskName = modifiedProjectTaskEntity.getName();

        entityManager.detach(modifiedProjectTaskEntity);
        if (taskId == null) {
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_MODIFY_NOT_SAVED));
        } else if (project.getId() == null) {
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_MODIFY_PROJECT_NOT_SAVED));
        } else if (projectRepository.findAllById(project.getId()) == null) {
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_MODIFY_PROJECT_NOT_EXISTS));
        } else if (taskName.isEmpty()) {
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_MODIFY_EMPTY_NAME));
        } else if (projectTaskRepository.findAllByProjectEntityAndNameAndIdNot(project, taskName, taskId) != null) {
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_MODIFY_SAME_PROJECT_AND_NAME));
        } else {
            projectTaskRepository.saveAndFlush(modifiedProjectTaskEntity);
        }
    }

    /**
     * Hibalehetőségek: <br/>
     * - A task ID-je üres
     * - A task-hoz még van hozzárendelve fejlesztő
     */
    public void deleteProjectTask(final @NonNull ProjectTaskEntity deleteProjectTaskEntity) {
        Long taskId = deleteProjectTaskEntity.getId();

        if (taskId == null) {
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_DELETE_NOT_SAVED));
        } else if (!Objects.requireNonNull(projectTaskRepository.findAllById(taskId))
            .getProjectTaskDevelopers()
            .isEmpty()) {
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_DELETE_DEVELOPERS_ASSIGNED));
        } else {
            projectTaskRepository.deleteById(taskId);
            entityManager.flush();
        }
    }

    public @NonNull List<ProjectTaskEntity> getProjectTasks() {
        return projectTaskRepository.findAll();
    }

    public @Nullable ProjectTaskEntity getProjectTaskById(final @NonNull Long projectTaskId) {
        return projectTaskRepository.findAllById(projectTaskId);
    }

    public @Nullable ProjectTaskEntity getProjectTaskByProjectEntityAndName(
        final @NonNull ProjectEntity projectEntity, final @NonNull String taskName
    ) {
        return projectTaskRepository.findAllByProjectEntityAndName(projectEntity, taskName);
    }

}
