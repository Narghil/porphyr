package combit.hu.porphyr.service;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTaskDeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import combit.hu.porphyr.repository.DeveloperRepository;
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
import java.util.stream.Collectors;

@Service
@Transactional
@ThreadSafe
public class ProjectTaskDeveloperService {

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull EntityManager entityManager;

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull ProjectTaskDeveloperRepository projectTaskDeveloperRepository;

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull ProjectTaskRepository projectTaskRepository;

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull DeveloperRepository developerRepository;

    @Autowired
    public ProjectTaskDeveloperService(
        final @NonNull EntityManager entityManager,
        final @NonNull ProjectTaskDeveloperRepository projectTaskDeveloperRepository,
        final @NonNull ProjectTaskRepository projectTaskRepository,
        final @NonNull DeveloperRepository developerRepository
    ) {
        this.entityManager = entityManager;
        this.projectTaskDeveloperRepository = projectTaskDeveloperRepository;
        this.projectTaskRepository = projectTaskRepository;
        this.developerRepository = developerRepository;
    }

    private final @NonNull ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    /**
     * ProjectTask - ProjectDeveloper összerendelés felvétele.
     * Hibalehetőségek: <br />
     * - projectTaskEntity nincs elmentve  <br />
     * - projectTaskEntity nincs az adatbázisban  <br />
     * - DeveloperEntity nincs elmentve  <br />
     * - DeveloperEntity nincs az adatbázisban  <br />
     * - DeveloperEntity nincs hozzárendelve a projectTaskEntity projektjéhez
     * - Már létezik ilyen összerendelés  <br />
     */
    public synchronized void insertNewProjectTaskDeveloper(
        final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloperEntity
    ) throws ExecutionException, InterruptedException {
        final class InsertRunnableCore implements Runnable {
            final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloperEntity;

            public InsertRunnableCore(final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloperEntity) {
                this.projectTaskDeveloperEntity = projectTaskDeveloperEntity;
            }

            @Override
            public void run() {

                final @Nullable ProjectTaskEntity projectTaskEntity = projectTaskDeveloperEntity.getProjectTaskEntity();
                if (projectTaskEntity.getId() == null) {
                    throw new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_PROJECTTASK_NOT_SAVED);
                }
                if (projectTaskRepository.findAllById(projectTaskEntity.getId()) == null) {
                    throw new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_PROJECTTASK_NOT_EXISTS);
                }
                final @Nullable ProjectEntity projectTaskProject = projectTaskEntity.getProjectEntity();

                final @Nullable DeveloperEntity developerEntity = projectTaskDeveloperEntity.getDeveloperEntity();
                if (developerEntity.getId() == null) {
                    throw new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_DEVELOPER_NOT_SAVED);
                }
                if (developerRepository.findAllById(developerEntity.getId()) == null) {
                    throw new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_DEVELOPER_NOT_EXISTS);
                }

                List<ProjectEntity> developerInThisProjects = developerEntity.getDeveloperProjects().stream().map(
                    ProjectDeveloperEntity::getProjectEntity).collect(Collectors.toList()
                );
                if (!developerInThisProjects.contains(projectTaskProject)) {
                    throw new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_DEVELOPER_NOT_IN_PROJECT);
                }

                entityManager.detach(projectTaskDeveloperEntity);

                if (projectTaskDeveloperRepository
                    .findAllByProjectTaskEntityAndDeveloperEntity(projectTaskEntity, developerEntity) != null) {
                    throw new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_EXISTING_DATA);
                }

                projectTaskDeveloperRepository.saveAndFlush(projectTaskDeveloperEntity);
            }
        }

        try {
            forkJoinPool.submit(new InsertRunnableCore(projectTaskDeveloperEntity)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
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
                    throw new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECTTASKDEVELOPER_MODIFY_NOT_SAVED);
                } else if (projectTaskDeveloperEntity.getSpendTime() < 0) {
                    throw new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECTTASKDEVELOPER_MODIFY_TIME_IS_NEGATIVE);
                } else {
                    projectTaskDeveloperRepository.saveAndFlush(projectTaskDeveloperEntity);
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(projectTaskDeveloperEntity)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
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
                    throw new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECTTASKDEVELOPER_DELETE_NOT_SAVED);
                } else if (projectTaskDeveloperEntity.getSpendTime() > 0) {
                    throw new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECTTASKDEVELOPER_DELETE_TIME_NOT_ZERO);
                } else {
                    projectTaskDeveloperRepository.deleteById(projectTaskDeveloperEntity.getId());
                    entityManager.flush();
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(projectTaskDeveloperEntity)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
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
        @NonNull
        List<ProjectTaskDeveloperEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore()).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
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
        @Nullable
        ProjectTaskDeveloperEntity result = null;
        try {
            result = forkJoinPool.submit(new CallableCore(id)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
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
                return projectTaskDeveloperRepository.findAllByProjectTaskEntityAndDeveloperEntity(
                    projectTaskEntity, projectDeveloperEntity.getDeveloperEntity());
            }
        }
        @Nullable
        ProjectTaskDeveloperEntity result = null;
        try {
            result = forkJoinPool.submit(new CallableCore(projectTaskEntity, projectDeveloperEntity)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
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
        @NonNull
        List<ProjectTaskDeveloperEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore(projectTaskEntity)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
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
                return projectTaskDeveloperRepository.findAllByDeveloperEntity(projectDeveloperEntity.getDeveloperEntity());
            }
        }
        @NonNull
        List<ProjectTaskDeveloperEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore(projectDeveloperEntity)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
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
                return projectTaskDeveloperRepository.findProjectTasksDeveloperByProjectIdAndDeveloperId(
                    projectId,
                    developerId
                );
            }
        }
        @NonNull
        List<ProjectTaskDeveloperEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore(projectId, developerId)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * ProjectTask-ok lekérdezése, a PT-PD összerendelések közül, Developer szerint.
     */
    public synchronized @NonNull List<ProjectTaskDeveloperEntity> getProjectTasksDeveloperByDeveloperId(
        final @NonNull Long developerId
    ) throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<List<ProjectTaskDeveloperEntity>> {
            final @NonNull Long developerId;

            public CallableCore(
                final @NonNull Long developerId
            ) {
                this.developerId = developerId;
            }

            @Override
            public List<ProjectTaskDeveloperEntity> call() {
                return projectTaskDeveloperRepository.findProjectTasksDeveloperByDeveloperId(developerId);
            }
        }
        @NonNull
        List<ProjectTaskDeveloperEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore(developerId)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Egy developer teljes munkaideje
     */
    public synchronized @NonNull Long getDeveloperFullTime(
        final @NonNull DeveloperEntity developer
    )
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<Long> {
            private final @NonNull DeveloperEntity developer;

            public CallableCore(final @NonNull DeveloperEntity developer) {
                this.developer = developer;
            }

            @Override
            public @NonNull Long call() {
                @Nullable
                Long developerId = developer.getId();

                return (developerId == null)
                       ? 0L : projectTaskDeveloperRepository.sumSpendTimeByDeveloperId(developerId);
            }
        }
        @NonNull
        Long result = 0L;
        try {
            result = forkJoinPool.submit(new CallableCore(developer)).get();
        } catch (ExecutionException ee) {
            PorphyrServiceException.handleExecutionException(ee);
        }
        return result;
    }

    /**
     * Egy project-re fordított teljes munkaidő
     */
    public synchronized @NonNull Long getProjectFullTime(final @NonNull ProjectEntity project)
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<Long> {
            private final @NonNull ProjectEntity project;

            public CallableCore(final @NonNull ProjectEntity project) {
                this.project = project;
            }

            @Override
            public @NonNull Long call() {
                @Nullable
                Long projectId = project.getId();
                return (projectId == null) ? 0L : projectTaskDeveloperRepository.sumSpendTimeByProjectId(projectId);
            }
        }
        @NonNull
        Long result = 0L;
        try {
            result = forkJoinPool.submit(new CallableCore(project)).get();
        } catch (ExecutionException ee) {
            PorphyrServiceException.handleExecutionException(ee);
        }
        return result;
    }
}
