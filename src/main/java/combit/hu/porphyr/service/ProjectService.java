package combit.hu.porphyr.service;

import combit.hu.porphyr.config.RequestsConstants;
import combit.hu.porphyr.controller.helpers.SessionData;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.repository.ProjectRepository;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.Synchronized;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
public class ProjectService {
    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull EntityManager entityManager;

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull ProjectRepository projectRepository;

    @Resource(name = "getSessionData")
    SessionData sessionData;

    @Autowired
    public ProjectService(
        final @NonNull EntityManager entityManager,
        final @NonNull ProjectRepository projectRepository
    ) {
        this.entityManager = entityManager;
        this.projectRepository = projectRepository;
    }

    private static final @NonNull ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    /**
     * Új project felvétele. <br/>
     * Hibalehetőségek: <br/>
     * - Nincs kitöltve a név <br/>
     * - Van másik ilyen nevű projekt <br/>
     */
    public synchronized void insertNewProject(final @NonNull ProjectEntity newProjectEntity)
        throws ExecutionException, InterruptedException {
        final class RunnableCore implements Runnable {
            private final @NonNull ProjectEntity newProjectEntity;

            public RunnableCore(final @NonNull ProjectEntity newProjectEntity) {
                this.newProjectEntity = newProjectEntity;
            }

            @Override
            public void run() {
                if (newProjectEntity.getName().isEmpty()) {
                    throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECT_INSERT_EMPTY_NAME));
                } else if (!projectRepository.findAllByName(newProjectEntity.getName()).isEmpty()) {
                    throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECT_INSERT_SAME_NAME));
                } else {
                    projectRepository.saveAndFlush(newProjectEntity);
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(newProjectEntity)).get();
        } catch (ExecutionException exception) {
            PorphyrServiceException.handleExecutionException(exception);
        }
    }

    /**
     * Project módosítása <br/>
     * Hibalehetőségek: <br/>
     * - Nincs kitöltve az ID <br/>
     * - Nincs kitöltve a név <br/>
     * - Van másik ilyen nevű projekt <br/>
     */
    public synchronized void modifyProject(final @NonNull ProjectEntity modifiedProjectEntity)
        throws ExecutionException, InterruptedException {
        final class RunnableCore implements Runnable {
            private final @NonNull ProjectEntity modifiedProjectEntity;

            public RunnableCore(final @NonNull ProjectEntity modifiedProjectEntity) {
                this.modifiedProjectEntity = modifiedProjectEntity;
            }

            @SneakyThrows
            @Override
            public void run() {
                entityManager.detach(modifiedProjectEntity);
                if (modifiedProjectEntity.getId() == null) {
                    throw new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECT_MODIFY_NOT_SAVED);
                } else if (modifiedProjectEntity.getName().isEmpty()) {
                    throw new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECT_MODIFY_EMPTY_NAME);
                } else {
                    boolean isSameNamedProject = isProjectWithNameAndNotId(
                        modifiedProjectEntity.getName(), modifiedProjectEntity.getId()
                    );
                    if (isSameNamedProject) {
                        throw new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECT_MODIFY_SAME_NAME);
                    } else {
                        projectRepository.saveAndFlush(modifiedProjectEntity);
                    }
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(modifiedProjectEntity)).get();
        } catch (ExecutionException ee) {
            PorphyrServiceException.handleExecutionException(ee);
        }
    }

    /**
     * Project törlése <br/>
     * Hibalehetőségek: <br/>
     * - Az ID nincs kitöltve<br />
     * - A projekthez még van hozzárendelt fejlesztő<br />
     * - A projekthez még tartozik feladat<br />
     */
    public synchronized void deleteProject(final @NonNull ProjectEntity projectEntity)
        throws ExecutionException, InterruptedException {

        final class RunnableCore implements Runnable {
            private final @NonNull ProjectEntity projectEntity;

            public RunnableCore(final @NonNull ProjectEntity projectEntity) {
                this.projectEntity = projectEntity;
            }

            @Override
            public void run() {
                Long projectId = projectEntity.getId();
                if (projectId == null) {
                    throw new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECT_DELETE_NOT_SAVED);
                } else {
                    ProjectEntity actualProjectData = projectRepository.findAllById(projectId);
                    if (actualProjectData == null) {
                        throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.UNDEFINED));
                    } else if (!actualProjectData.getProjectTasks().isEmpty()) {
                        throw new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECT_DELETE_TASKS_ASSIGNED);
                    } else if (!actualProjectData.getProjectDevelopers().isEmpty()) {
                        throw new PorphyrServiceException(PorphyrServiceException.Exceptions.PROJECT_DELETE_DEVELOPERS_ASSIGNED);
                    } else {
                        projectRepository.deleteById(projectEntity.getId());
                        entityManager.flush();
                    }
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(projectEntity)).get();
        } catch (ExecutionException ee) {
            PorphyrServiceException.handleExecutionException(ee);
        }
    }

    /**
     * Projektek listájának lekérdezése
     */
    public synchronized @NonNull List<ProjectEntity> getProjects() throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<List<ProjectEntity>> {
            @Override
            public List<ProjectEntity> call() {
                return projectRepository.findAll();
            }
        }
        @NonNull List<ProjectEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore()).get();
            for( ProjectEntity project : result ){ getProjectFullTime( project );}
        } catch (ExecutionException ee) {
            PorphyrServiceException.handleExecutionException(ee);
        }
        return result;
    }

    /**
     * Egy projekt lekérdezése, ID szerint
     */
    public synchronized @Nullable ProjectEntity getProjectById(final @NonNull Long id)
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<ProjectEntity> {
            private final @NonNull Long id;

            public CallableCore(final @NonNull Long id) {
                this.id = id;
            }

            @Override
            public ProjectEntity call() {
                return projectRepository.findAllById(id);
            }
        }
        @Nullable ProjectEntity result = null;
        try {
            result = forkJoinPool.submit(new CallableCore(id)).get();
            if( result != null) getProjectFullTime( result );
        } catch (ExecutionException ee) {
            PorphyrServiceException.handleExecutionException(ee);
        }
        return result;
    }

    /**
     * Egy projekt lekérdezése név szerint
     */
    public synchronized @Nullable ProjectEntity getProjectByName(final @NonNull String name)
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<ProjectEntity> {
            private final @NonNull String name;

            public CallableCore(final @NonNull String name) {
                this.name = name;
            }

            @Override
            public ProjectEntity call() {
                List<ProjectEntity> namedProjects = projectRepository.findAllByName(name);
                return namedProjects.isEmpty() ? null : namedProjects.get(0);
            }
        }
        @Nullable ProjectEntity result = null;
        try {
            result = forkJoinPool.submit(new CallableCore(name)).get();
            if( result != null) getProjectFullTime( result );
        } catch (ExecutionException ee) {
            PorphyrServiceException.handleExecutionException(ee);
        }
        return result;
    }

    /**
     * Egy projekt lekérdezése: van-e már ilyen nevű projekt?
     */
    public synchronized @NonNull Boolean isProjectWithNameAndNotId(final @NonNull String name, final @NonNull Long id)
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<Boolean> {
            private final @NonNull String name;
            private final @NonNull Long id;

            public CallableCore(final @NonNull String name, final @NonNull Long id) {
                this.name = name;
                this.id = id;
            }

            @Override
            public Boolean call() {
                return !projectRepository.findAllByNameAndIdNot(name, id).isEmpty();
            }
        }
        @NonNull Boolean result = false;
        try {
            result = forkJoinPool.submit(new CallableCore(name, id)).get();
        } catch (ExecutionException ee) {
            PorphyrServiceException.handleExecutionException(ee);
        }
        return result;
    }

    /**
     * Egy project-re fordított teljes munkaidő
     */
    public synchronized @NonNull Long getProjectFullTime( final @NonNull ProjectEntity project)
        throws ExecutionException, InterruptedException{
        final class CallableCore implements Callable<Long>{
            private final @NonNull ProjectEntity project;

            public CallableCore( final @NonNull ProjectEntity project){
                this.project = project;
            }

            @Override
            public @NonNull Long call(){
                @Nullable Long projectId = project.getId();
                return (projectId == null) ? 0L : projectRepository.sumSpendTimeByProjectId( projectId );
            }
        }
        @NonNull Long result = 0L;
        try{
            result = forkJoinPool.submit( new CallableCore(project)).get();
            project.setSpendTime( result );
        } catch (ExecutionException ee){
            PorphyrServiceException.handleExecutionException(ee);
        }
        return result;
    }

    /**
     * A bejelentkezett user-hez tartozó projektek listájának lekérdezése
     * Ha PERMIT_PROJECT_NEW joga van, akkor minden projektet láthat.
     */
    public synchronized @NonNull List<ProjectEntity> getActualUserProjects() throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<List<ProjectEntity>> {
            @Override
            public List<ProjectEntity> call() {
                if (sessionData.getUserPermitNames().contains(RequestsConstants.PERMIT_PROJECT_NEW)) {
                    return projectRepository.findAll();
                } else {
                    return projectRepository.findAllByActualUserDevelopers(sessionData.getUserDevelopers());
                }
            }
        }
        @NonNull List<ProjectEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore()).get();
            for( ProjectEntity project : result ){ getProjectFullTime( project );}
        } catch (ExecutionException ee) {
            PorphyrServiceException.handleExecutionException(ee);
        }
        return result;
    }

}
