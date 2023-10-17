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

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@ThreadSafe
public class ProjectTaskService {

    @Autowired
    @Setter
    @GuardedBy("this")
    private EntityManager entityManager;

    @Autowired
    @Setter
    @GuardedBy("this")
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    @Setter
    @GuardedBy("this")
    private ProjectRepository projectRepository;

    /**
     * Hibalehetőségek: <br/>
     * - A project entity nem létező project -re mutat<br />
     * - A task neve üres<br />
     * - Ilyen nevű task már van a projekten belül!<br />
     */
    public synchronized void insertNewProjectTask(final @NonNull ProjectTaskEntity newProjectTaskEntity) {
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
     * - A task ID-je üres<br />
     * - A project entity üres vagy nem létező project-re mutat<br />
     * - A task neve üres<br />
     * - Ilyen nevű task már van a projektben<br />
     */
    public synchronized void modifyProjectTask(final @NonNull ProjectTaskEntity modifiedProjectTaskEntity) {
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
     * - A task ID-je üres<br />
     * - A task-hoz még van hozzárendelve fejlesztő<br />
     */
    public synchronized void deleteProjectTask(final @NonNull ProjectTaskEntity deleteProjectTaskEntity) {
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

    public synchronized @NonNull List<ProjectTaskEntity> getProjectTasks() {
        return projectTaskRepository.findAll();
    }

    public synchronized @Nullable ProjectTaskEntity getProjectTaskById(final @NonNull Long projectTaskId) {
        return projectTaskRepository.findAllById(projectTaskId);
    }

    public synchronized @Nullable ProjectTaskEntity getProjectTaskByProjectEntityAndName(
        final @NonNull ProjectEntity projectEntity, final @NonNull String taskName
    ) {
        return projectTaskRepository.findAllByProjectEntityAndName(projectEntity, taskName);
    }

}
