package combit.hu.porphyr.service;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.repository.DeveloperRepository;
import combit.hu.porphyr.repository.ProjectDeveloperRepository;
import combit.hu.porphyr.repository.ProjectRepository;
import combit.hu.porphyr.repository.ProjectTaskDeveloperRepository;
import lombok.NonNull;
import lombok.Setter;
import lombok.Synchronized;
import org.jetbrains.annotations.Nullable;
//-- import org.springframework.beans.factory.annotation.Autowired --
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class ProjectDeveloperService {

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull EntityManager entityManager;

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull ProjectDeveloperRepository projectDeveloperRepository;

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull ProjectRepository projectRepository;

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull ProjectTaskDeveloperRepository projectTaskDeveloperRepository;

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull DeveloperRepository developerRepository;

    @Autowired
    public ProjectDeveloperService(
        final @NonNull EntityManager entityManager,
        final @NonNull ProjectDeveloperRepository projectDeveloperRepository,
        final @NonNull ProjectRepository projectRepository,
        final @NonNull ProjectTaskDeveloperRepository projectTaskDeveloperRepository,
        final @NonNull DeveloperRepository developerRepository
    ) {
        this.entityManager = entityManager;
        this.projectDeveloperRepository = projectDeveloperRepository;
        this.projectRepository = projectRepository;
        this.projectTaskDeveloperRepository = projectTaskDeveloperRepository;
        this.developerRepository = developerRepository;
    }

    private static final @NonNull ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    /**
     * Új projekt - Developer összerendelés felvétele <br />
     * Hibalehetőségek <br />
     * - projectEntity = null <br />
     * - projectEntity.id = null <br />
     * - projectEntity nincs az adatbázisban <br />
     * - developerEntity = null <br />
     * - developerEntity.id = null <br />
     * - developerEntity nincs az adatbázisban <br />
     * - már van ilyen tétel <br />
     */
    public synchronized void insertNewProjectDeveloper(final @NonNull ProjectDeveloperEntity newProjectDeveloperEntity)
        throws ExecutionException, InterruptedException {
        final class RunnableCore implements Runnable {
            private final @NonNull ProjectDeveloperEntity newProjectDeveloperEntity;

            public RunnableCore(final @NonNull ProjectDeveloperEntity newProjectDeveloperEntity) {
                this.newProjectDeveloperEntity = newProjectDeveloperEntity;
            }

            @Override
            public void run() {
                @Nullable
                PorphyrServiceException porphyrServiceException = null;
                entityManager.detach(newProjectDeveloperEntity);

                if (newProjectDeveloperEntity.getProjectEntity().getId() == null) {
                    porphyrServiceException = new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECTDEVELOPER_INSERT_PROJECT_NOT_SAVED);
                } else if (projectRepository.findAllById(newProjectDeveloperEntity.getProjectEntity()
                    .getId()) == null) {
                    porphyrServiceException = new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECTDEVELOPER_INSERT_PROJECT_NOT_EXISTS);
                } else if (newProjectDeveloperEntity.getDeveloperEntity().getId() == null) {
                    porphyrServiceException = new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECTDEVELOPER_INSERT_DEVELOPER_NOT_SAVED);
                } else if (developerRepository.findAllById(newProjectDeveloperEntity.getDeveloperEntity()
                    .getId()) == null) {
                    porphyrServiceException = new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECTDEVELOPER_INSERT_DEVELOPER_NOT_EXISTS);
                } else if (projectDeveloperRepository.findAllByProjectEntityAndDeveloperEntity(
                    newProjectDeveloperEntity.getProjectEntity(),
                    newProjectDeveloperEntity.getDeveloperEntity()
                ) != null) {
                    porphyrServiceException = new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECTDEVELOPER_INSERT_EXISTING_DATA);
                }
                if (porphyrServiceException != null) {
                    throw porphyrServiceException;
                } else {
                    projectDeveloperRepository.saveAndFlush(newProjectDeveloperEntity);
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(newProjectDeveloperEntity)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
    }

    /**
     * Project - Developer összerendelés törlése <br />
     * Hibalehetőségek:
     * - Az entity még nincs elmentve<br />
     * - nem törölhető az az összerendelés, amihez tartozik ProjectTaskDeveloper bejegyzés<br />
     */
    public synchronized void deleteProjectDeveloper(final @NonNull ProjectDeveloperEntity projectDeveloperEntity)
        throws ExecutionException, InterruptedException {
        final class RunnableCore implements Runnable {
            private final @NonNull ProjectDeveloperEntity projectDeveloperEntity;

            public RunnableCore(final @NonNull ProjectDeveloperEntity projectDeveloperEntity) {
                this.projectDeveloperEntity = projectDeveloperEntity;
            }

            @Override
            public void run() {
                if (projectDeveloperEntity.getId() == null) {
                    throw new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECTDEVELOPER_DELETE_NOT_SAVED);
                } else if (!projectTaskDeveloperRepository.findAllByDeveloperEntity(projectDeveloperEntity.getDeveloperEntity())
                    .isEmpty()) {
                    throw new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECTDEVELOPER_DELETE_ASSIGNED_TO_TASK);
                } else {
                    projectDeveloperRepository.deleteById(projectDeveloperEntity.getId());
                    entityManager.flush();
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(projectDeveloperEntity)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
    }

    // ------------------------------------------- Lekérdezések --------------------------------------------------------------

    /**
     * Project - Developer összerendelés lekérdezése ID szerint.
     */
    public synchronized @Nullable ProjectDeveloperEntity getProjectDeveloperById(final @NonNull Long id)
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<ProjectDeveloperEntity> {
            private final @NonNull Long id;

            public CallableCore(final @NonNull Long id) {
                this.id = id;
            }

            @Override
            public ProjectDeveloperEntity call() {
                return projectDeveloperRepository.findAllById(id);
            }
        }
        @Nullable
        ProjectDeveloperEntity result = null;
        try {
            result = forkJoinPool.submit(new CallableCore(id)).get();
            if (result != null) {
                getDeveloperFullTimeInProject(result);
            }
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Project - Developer összerendelés lekérdezése Project és Developer szerint.
     */
    public synchronized @Nullable ProjectDeveloperEntity getProjectDeveloperByProjectAndDeveloper(
        final @NonNull ProjectEntity project,
        final @NonNull DeveloperEntity developer
    ) throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<ProjectDeveloperEntity> {
            private final @NonNull ProjectEntity project;
            private final @NonNull DeveloperEntity developer;

            public CallableCore(final @NonNull ProjectEntity project, final @NonNull DeveloperEntity developer) {
                this.project = project;
                this.developer = developer;
            }

            @Override
            public ProjectDeveloperEntity call() {
                return projectDeveloperRepository.findAllByProjectEntityAndDeveloperEntity(project, developer);
            }
        }
        @Nullable
        ProjectDeveloperEntity result = null;
        try {
            result = forkJoinPool.submit(new CallableCore(project, developer)).get();
            if (result != null) {
                getDeveloperFullTimeInProject(result);
            }
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Valamennyi Project - Developer összerendelés lekérdezése.
     */
    public synchronized @NonNull List<ProjectDeveloperEntity> getProjectDevelopers()
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<List<ProjectDeveloperEntity>> {
            @Override
            public List<ProjectDeveloperEntity> call() {
                return projectDeveloperRepository.findAll();
            }
        }
        @NonNull
        List<ProjectDeveloperEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore()).get();
            for (ProjectDeveloperEntity projectDeveloper : result) {
                getDeveloperFullTimeInProject(projectDeveloper);
            }
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Egy Developer-hez tartozó valamennyi Project - Developer összerendelés lekérdezése.
     */
    public synchronized @NonNull List<ProjectDeveloperEntity> getProjectDevelopersByDeveloper(final @NonNull DeveloperEntity developer)
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<List<ProjectDeveloperEntity>> {
            final @NonNull DeveloperEntity developer;

            public CallableCore(final @NonNull DeveloperEntity developer) {
                this.developer = developer;
            }

            @Override
            public List<ProjectDeveloperEntity> call() {
                return projectDeveloperRepository.findAllByDeveloperEntity(developer);
            }
        }
        @NonNull
        List<ProjectDeveloperEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore(developer)).get();
            for (ProjectDeveloperEntity projectDeveloper : result) {
                getDeveloperFullTimeInProject(projectDeveloper);
            }
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Egy Project-hez tartozó valamennyi Project - Developer összerendelés lekérdezése.
     */
    public synchronized @NonNull List<ProjectDeveloperEntity> getProjectDevelopersByProject(final @NonNull ProjectEntity project)
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<List<ProjectDeveloperEntity>> {
            final @NonNull ProjectEntity project;

            public CallableCore(final @NonNull ProjectEntity project) {
                this.project = project;
            }

            @Override
            public List<ProjectDeveloperEntity> call() {
                return projectDeveloperRepository.findAllByProjectEntity(project);
            }
        }
        @NonNull
        List<ProjectDeveloperEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore(project)).get();
            for (ProjectDeveloperEntity projectDeveloper : result) {
                getDeveloperFullTimeInProject(projectDeveloper);
            }
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Egy developer egy project-re fordított teljes munkaideje
     */
    public synchronized @NonNull Long getDeveloperFullTimeInProject(
        final @NonNull ProjectDeveloperEntity projectDeveloper
    )
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<Long> {
            private final @NonNull ProjectDeveloperEntity projectDeveloper;

            public CallableCore(final @NonNull ProjectDeveloperEntity projectDeveloper) {
                this.projectDeveloper = projectDeveloper;
            }

            @Override
            public @NonNull Long call() {
                @NonNull
                Long result;
                @Nullable
                Long developerId = projectDeveloper.getDeveloperEntity().getId();
                @Nullable
                Long projectId = projectDeveloper.getProjectEntity().getId();
                result = (developerId == null || projectId == null)
                         ? 0L
                         : projectTaskDeveloperRepository.sumSpendTimeByDeveloperIdAndProjectId(developerId, projectId);
                projectDeveloper.setSpendTime(result);

                return result;
            }
        }
        @NonNull
        Long result = 0L;
        try {
            result = forkJoinPool.submit(new CallableCore(projectDeveloper)).get();
        } catch (ExecutionException ee) {
            PorphyrServiceException.handleExecutionException(ee);
        }
        return result;
    }
}
