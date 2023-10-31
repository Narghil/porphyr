package combit.hu.porphyr.service;

import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskDeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import combit.hu.porphyr.repository.ProjectDeveloperRepository;
import combit.hu.porphyr.repository.ProjectTaskDeveloperRepository;
import combit.hu.porphyr.repository.ProjectTaskRepository;
import lombok.NonNull;
import lombok.Setter;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

@Service
@Transactional
@ThreadSafe
public class ProjectTaskDeveloperService {

    @Autowired
    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    EntityManager entityManager;

    @Autowired
    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    ProjectTaskDeveloperRepository projectTaskDeveloperRepository;

    @Autowired
    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    ProjectTaskRepository projectTaskRepository;

    @Autowired
    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    ProjectDeveloperRepository projectDeveloperRepository;

    private final @NonNull ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    /**
     * ProjectTask - ProjectDeveloper összerendelés felvétele.
     * Hibalehetőségek: <br />
     * - projectTaskEntity nincs elmentve  <br />
     * - projectTaskEntity nincs az adatbázisban  <br />
     * - projectDeveloperEntity nincs elmentve  <br />
     * - projectDeveloperEntity nincs az adatbázisban  <br />
     * - A projectTaskEntity és a projectDeveloperEntity más-más projekthez tartozik <br />
     * - Már létezik ilyen összerendelés  <br />
     */
    public synchronized void insertNewProjectTaskDeveloper(
        final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloperEntity
    ) throws ExecutionException, InterruptedException {
        try {
            forkJoinPool.submit(new InsertRunnableCore(projectTaskDeveloperEntity)).get();
        } catch (ExecutionException executionException) {
            ServiceException.handleExecutionException(executionException);
        }
    }

    final class InsertRunnableCore implements Runnable {
        final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloperEntity;

        public InsertRunnableCore(final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloperEntity) {
            this.projectTaskDeveloperEntity = projectTaskDeveloperEntity;
        }

        @Override
        public void run() {
            @Nullable ServiceException serviceException = null;
            final @NonNull ProjectTaskEntity projectTaskEntity = projectTaskDeveloperEntity.getProjectTaskEntity();
            final @NonNull ProjectDeveloperEntity projectDeveloperEntity = projectTaskDeveloperEntity.getProjectDeveloperEntity();
            entityManager.detach(projectTaskDeveloperEntity);
            if (projectTaskEntity.getId() == null) {
                serviceException = new ServiceException(ServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_PROJECTTASK_NOT_SAVED);
            } else if (projectTaskRepository.findAllById(projectTaskEntity.getId()) == null) {
                serviceException = new ServiceException(ServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_PROJECTTASK_NOT_EXISTS);
            } else if (projectDeveloperEntity.getId() == null) {
                serviceException = new ServiceException(ServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_PROJECTDEVELOPER_NOT_SAVED);
            } else if (projectDeveloperRepository.findAllById(projectDeveloperEntity.getId()) == null) {
                serviceException = new ServiceException(ServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_PROJECTDEVELOPER_NOT_EXISTS);
            } else {
                Long taskProjectId = projectTaskEntity.getProjectEntity().getId();
                Long developerProjectId = projectDeveloperEntity.getProjectEntity().getId();
                if (taskProjectId == null || developerProjectId == null) {
                    serviceException = new ServiceException(ServiceException.Exceptions.NULL_VALUE);
                } else if (!taskProjectId.equals(developerProjectId)) {
                    serviceException = new ServiceException(ServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_TASK_OR_DEVELOPER_NOT_IN_PROJECT);
                } else if (projectTaskDeveloperRepository.findAllByProjectTaskEntityAndProjectDeveloperEntity(
                    projectTaskEntity, projectDeveloperEntity
                ) != null) {
                    serviceException = new ServiceException(ServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_EXISTING_DATA);
                }
            }

            if (serviceException != null) {
                throw serviceException;
            } else {
                projectTaskDeveloperRepository.saveAndFlush(projectTaskDeveloperEntity);
            }
        }
    }

    /**
     * ProjectTask - ProjectDeveloper összerendelés módosítása (csak a felhasznált idő módosítható)
     * Hibalehetőségek: <br />
     * - projectTaskDeveloperEntity nincs elmentve  <br />
     * - spendTime < 0 <br />
     */
    public synchronized void modifyProjectTaskDeveloper(
        final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloperEntity
    ) throws ExecutionException, InterruptedException {
        final class RunnableCore implements Runnable {
            final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloperEntity;

            public RunnableCore(final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloperEntity) {
                this.projectTaskDeveloperEntity = projectTaskDeveloperEntity;
            }

            @Override
            public void run() {
                if (projectTaskDeveloperEntity.getId() == null) {
                    throw new ServiceException(ServiceException.Exceptions.PROJECTTASKDEVELOPER_MODIFY_NOT_SAVED);
                } else if (projectTaskDeveloperEntity.getSpendTime() < 0) {
                    throw new ServiceException(ServiceException.Exceptions.PROJECTTASKDEVELOPER_MODIFY_TIME_IS_NEGATIVE);
                } else {
                    projectTaskDeveloperRepository.saveAndFlush(projectTaskDeveloperEntity);
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(projectTaskDeveloperEntity)).get();
        } catch (ExecutionException executionException) {
            ServiceException.handleExecutionException(executionException);
        }
    }

    /**
     * ProjectTask - ProjectDeveloper összerendelés törlése
     * Hibalehetőségek: <br />
     * - projectTaskDeveloperEntity nincs elmentve  <br />
     * - A projektben eltöltött idő nem 0.<br />
     */
    public synchronized void deleteProjectTaskDeveloper(
        final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloperEntity
    ) throws ExecutionException, InterruptedException {
        final class RunnableCore implements Runnable {
            final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloperEntity;

            public RunnableCore(final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloperEntity) {
                this.projectTaskDeveloperEntity = projectTaskDeveloperEntity;
            }

            @Override
            public void run() {
                if (projectTaskDeveloperEntity.getId() == null) {
                    throw new ServiceException(ServiceException.Exceptions.PROJECTTASKDEVELOPER_DELETE_NOT_SAVED);
                } else if (projectTaskDeveloperEntity.getSpendTime() > 0) {
                    throw new ServiceException(ServiceException.Exceptions.PROJECTTASKDEVELOPER_DELETE_TIME_NOT_ZERO);
                } else {
                    projectTaskDeveloperRepository.deleteById(projectTaskDeveloperEntity.getId());
                    entityManager.flush();
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(projectTaskDeveloperEntity)).get();
        } catch (ExecutionException executionException) {
            ServiceException.handleExecutionException(executionException);
        }
    }

    // ---- Lekérdezések ----

    /**
     * Valamennyi ProjectTask - ProjectDeveloper összerendelés lekérdezése.
     */
    public synchronized @NonNull List<ProjectTaskDeveloperEntity> getProjectTaskDevelopers()
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<List<ProjectTaskDeveloperEntity>> {
            @Override
            public List<ProjectTaskDeveloperEntity> call() {
                return projectTaskDeveloperRepository.findAll();
            }
        }
        @NonNull List<ProjectTaskDeveloperEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore()).get();
        } catch (ExecutionException executionException) {
            ServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * ProjectTask - ProjectDeveloper összerendelés lekérdezése ID szerint.
     */
    public synchronized @Nullable ProjectTaskDeveloperEntity getProjectTaskDeveloperById(final @NonNull Long id)
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<ProjectTaskDeveloperEntity> {
            final @NonNull Long id;

            public CallableCore(final @NonNull Long id) {
                this.id = id;
            }

            @Override
            public ProjectTaskDeveloperEntity call() {
                return projectTaskDeveloperRepository.findAllById(id);
            }
        }
        @Nullable ProjectTaskDeveloperEntity result = null;
        try {
            result = forkJoinPool.submit(new CallableCore(id)).get();
        } catch (ExecutionException executionException) {
            ServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * ProjectTask - ProjectDeveloper összerendelés lekérdezése ProjectTask és ProjectDeveloper szerint.
     */
    public synchronized @Nullable ProjectTaskDeveloperEntity getProjectTaskDeveloperByProjectTaskAndProjectDeveloper(
        final @NonNull ProjectTaskEntity projectTaskEntity,
        final @NonNull ProjectDeveloperEntity projectDeveloperEntity
    ) throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<ProjectTaskDeveloperEntity> {
            final @NonNull ProjectTaskEntity projectTaskEntity;
            final @NonNull ProjectDeveloperEntity projectDeveloperEntity;

            public CallableCore(
                final @NonNull ProjectTaskEntity projectTaskEntity,
                final @NonNull ProjectDeveloperEntity projectDeveloperEntity
            ) {
                this.projectTaskEntity = projectTaskEntity;
                this.projectDeveloperEntity = projectDeveloperEntity;
            }

            @Override
            public ProjectTaskDeveloperEntity call() {
                return projectTaskDeveloperRepository.findAllByProjectTaskEntityAndProjectDeveloperEntity(
                    projectTaskEntity, projectDeveloperEntity);
            }
        }
        @Nullable ProjectTaskDeveloperEntity result = null;
        try {
            result = forkJoinPool.submit(new CallableCore(projectTaskEntity, projectDeveloperEntity)).get();
        } catch (ExecutionException executionException) {
            ServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * ProjectTask - ProjectDeveloper összerendelések lekérdezése ProjectTask szerint.
     */
    public synchronized @NonNull List<ProjectTaskDeveloperEntity> getProjectTaskDevelopersByProjectTask(
        final @NonNull ProjectTaskEntity projectTaskEntity
    ) throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<List<ProjectTaskDeveloperEntity>> {
            final @NonNull ProjectTaskEntity projectTaskEntity;

            public CallableCore(final @NonNull ProjectTaskEntity projectTaskEntity) {
                this.projectTaskEntity = projectTaskEntity;
            }

            @Override
            public List<ProjectTaskDeveloperEntity> call() {
                return projectTaskDeveloperRepository.findAllByProjectTaskEntity(projectTaskEntity);
            }
        }
        @NonNull List<ProjectTaskDeveloperEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore(projectTaskEntity)).get();
        } catch (ExecutionException executionException) {
            ServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * ProjectTask - ProjectDeveloper összerendelések lekérdezése ProjectDeveloper szerint.
     */
    public synchronized @NonNull List<ProjectTaskDeveloperEntity> getProjectTaskDevelopersByProjectDeveloper(
        final @NonNull ProjectDeveloperEntity projectDeveloperEntity
    ) throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<List<ProjectTaskDeveloperEntity>> {
            final @NonNull ProjectDeveloperEntity projectDeveloperEntity;

            public CallableCore(final @NonNull ProjectDeveloperEntity projectDeveloperEntity) {
                this.projectDeveloperEntity = projectDeveloperEntity;
            }

            @Override
            public List<ProjectTaskDeveloperEntity> call() {
                return projectTaskDeveloperRepository.findAllByProjectDeveloperEntity(projectDeveloperEntity);
            }
        }
        @NonNull List<ProjectTaskDeveloperEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore(projectDeveloperEntity)).get();
        } catch (ExecutionException executionException) {
            ServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * ProjectTask-ok lekérdezése, a PT-PD összerendelések közül, Project és Developer szerint.
     */
    public synchronized @NonNull List<ProjectTaskDeveloperEntity> getProjectTaskDeveloperByProjectIdAndDeveloperId(
        final @NonNull Long projectId,
        final @NonNull Long developerId
    ) throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<List<ProjectTaskDeveloperEntity>> {
            final @NonNull Long projectId;
            final @NonNull Long developerId;

            public CallableCore(
                final @NonNull Long projectId,
                final @NonNull Long developerId
            ) {
                this.projectId = projectId;
                this.developerId = developerId;
            }

            @Override
            public List<ProjectTaskDeveloperEntity> call() {
                return projectTaskDeveloperRepository.findProjectTasksDeveloperByProjectIdAndDeveloperId(projectId, developerId);
            }
        }
        @NonNull List<ProjectTaskDeveloperEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore(projectId, developerId)).get();
        } catch (ExecutionException executionException) {
            ServiceException.handleExecutionException(executionException);
        }
        return result;
    }


}
