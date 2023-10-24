package combit.hu.porphyr.service;

import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import combit.hu.porphyr.repository.ProjectRepository;
import combit.hu.porphyr.repository.ProjectTaskRepository;
import lombok.NonNull;
import lombok.Setter;
import lombok.Synchronized;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

@Service
@Transactional
@ThreadSafe
public class ProjectTaskService {
    @Autowired
    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private EntityManager entityManager;

    @Autowired
    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private ProjectRepository projectRepository;

    private final @NonNull ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    /**
     * ProjectTask felvétele. <br/>
     * Hibalehetőségek: <br/>
     * - A project entity nem létező project -re mutat<br />
     * - A task neve üres<br />
     * - Ilyen nevű task már van a projekten belül!<br />
     */
    public synchronized void insertNewProjectTask(final @NonNull ProjectTaskEntity newProjectTaskEntity)
        throws ExecutionException, InterruptedException {
        final class RunnableCore implements Runnable {
            final @NonNull ProjectTaskEntity newProjectTaskEntity;

            public RunnableCore(final @NonNull ProjectTaskEntity newProjectTaskEntity) {
                this.newProjectTaskEntity = newProjectTaskEntity;
            }

            @Override
            public void run() {
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
        }
        try {
            forkJoinPool.submit(new RunnableCore(newProjectTaskEntity)).get();
        } catch (ExecutionException executionException) {
            ServiceException.handleExecutionException(executionException);
        }
    }

    /**
     * ProjectTask módosítása. <br/>
     * Hibalehetőségek: <br/>
     * - A task ID-je üres<br />
     * - A project entity üres vagy nem létező project-re mutat<br />
     * - A task neve üres<br />
     * - Ilyen nevű task már van a projektben<br />
     */
    public synchronized void modifyProjectTask(final @NonNull ProjectTaskEntity modifiedProjectTaskEntity)
        throws ExecutionException, InterruptedException {
        final class RunnableCore implements Runnable {
            final @NonNull ProjectTaskEntity modifiedProjectTaskEntity;

            public RunnableCore(final @NonNull ProjectTaskEntity modifiedProjectTaskEntity) {
                this.modifiedProjectTaskEntity = modifiedProjectTaskEntity;
            }

            @Override
            public void run() {
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
                } else if (projectTaskRepository.findAllByProjectEntityAndNameAndIdNot(
                    project,
                    taskName,
                    taskId
                ) != null) {
                    throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_MODIFY_SAME_PROJECT_AND_NAME));
                } else {
                    projectTaskRepository.saveAndFlush(modifiedProjectTaskEntity);
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(modifiedProjectTaskEntity)).get();
        } catch (ExecutionException executionException) {
            ServiceException.handleExecutionException(executionException);
        }
    }

    /**
     * ProjectTask törlése. <br/>
     * Hibalehetőségek: <br/>
     * - A task ID-je üres<br />
     * - A task-hoz még van hozzárendelve fejlesztő<br />
     */
    public synchronized void deleteProjectTask(final @NonNull ProjectTaskEntity deleteProjectTaskEntity)
        throws ExecutionException, InterruptedException {
        final class RunnableCore implements Runnable {
            final @NonNull ProjectTaskEntity deleteProjectTaskEntity;

            public RunnableCore(final @NonNull ProjectTaskEntity deleteProjectTaskEntity) {
                this.deleteProjectTaskEntity = deleteProjectTaskEntity;
            }

            @Override
            public void run() {
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
        }
        try {
            forkJoinPool.submit(new RunnableCore(deleteProjectTaskEntity)).get();
        } catch (ExecutionException executionException) {
            ServiceException.handleExecutionException(executionException);
        }
    }

    /**
     * Valamennyi ProjectTask lekérdezése
     */
    public synchronized @NonNull List<ProjectTaskEntity> getProjectTasks()
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<List<ProjectTaskEntity>> {
            @Override
            public List<ProjectTaskEntity> call() {
                return projectTaskRepository.findAll();
            }
        }
        @NonNull List<ProjectTaskEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore()).get();
        } catch (ExecutionException executionException) {
            ServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Egy ProjectTask lekérdezése ID szerint
     */
    public synchronized @Nullable ProjectTaskEntity getProjectTaskById(final @NonNull Long projectTaskId)
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<ProjectTaskEntity> {
            final @NonNull Long projectTaskId;

            public CallableCore(final @NonNull Long projectTaskId) {
                this.projectTaskId = projectTaskId;
            }

            @Override
            public @Nullable ProjectTaskEntity call() {
                return projectTaskRepository.findAllById(projectTaskId);
            }
        }
        @Nullable ProjectTaskEntity result = null;
        try {
            result = forkJoinPool.submit(new CallableCore(projectTaskId)).get();
        } catch (ExecutionException executionException) {
            ServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Egy ProjectTask lekérdezése Project és név szerint
     */
    public synchronized @Nullable ProjectTaskEntity getProjectTaskByProjectEntityAndName(
        final @NonNull ProjectEntity projectEntity, final @NonNull String taskName
    ) throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<ProjectTaskEntity> {
            final @NonNull ProjectEntity projectEntity;
            final @NonNull String taskName;

            public CallableCore(final @NonNull ProjectEntity projectEntity, final @NonNull String taskName) {
                this.projectEntity = projectEntity;
                this.taskName = taskName;
            }

            @Override
            public @Nullable ProjectTaskEntity call() {
                return projectTaskRepository.findAllByProjectEntityAndName(projectEntity, taskName);
            }
        }
        @Nullable ProjectTaskEntity result = null;
        try {
            result = forkJoinPool.submit(new CallableCore(projectEntity, taskName)).get();
        } catch (ExecutionException executionException) {
            ServiceException.handleExecutionException(executionException);
        }
        return result;
    }

}
