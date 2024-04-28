package combit.hu.porphyr.service;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import combit.hu.porphyr.repository.DeveloperRepository;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.Synchronized;
import org.jetbrains.annotations.Nullable;
// import org.springframework.beans.factory.annotation.Autowired
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
public class DeveloperService {

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull EntityManager entityManager;

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull DeveloperRepository developerRepository;

    @Autowired
    public DeveloperService(
        final @NonNull EntityManager entityManager,
        final @NonNull DeveloperRepository developerRepository
    ) {
        this.entityManager = entityManager;
        this.developerRepository = developerRepository;
    }

    private static final @NonNull ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    /**
     * Új fejlesztő felvétele<br/>
     * Hibalehetőségek: <br/>
     * - Nincs kitöltve a név <br/>
     * - Már van ilyen nevű fejlesztő <br/>
     */
    public synchronized void insertNewDeveloper(final @NonNull DeveloperEntity newDeveloperEntity)
        throws ExecutionException, InterruptedException {
        final class RunnableCore implements Runnable {
            private final @NonNull DeveloperEntity newDeveloperEntity;

            public RunnableCore(final @NonNull DeveloperEntity newDeveloperEntity) {
                this.newDeveloperEntity = newDeveloperEntity;
            }

            @Override
            public void run() {
                if (newDeveloperEntity.getName().isEmpty()) {
                    throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.DEVELOPER_INSERT_EMPTY_NAME));
                } else if (!developerRepository.findAllByName(newDeveloperEntity.getName()).isEmpty()) {
                    throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.DEVELOPER_INSERT_SAME_NAME));
                } else {
                    developerRepository.saveAndFlush(newDeveloperEntity);
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(newDeveloperEntity)).get();
        } catch (ExecutionException exception) {
            PorphyrServiceException.handleExecutionException(exception);
        }
    }

    /**
     * Fejlesztő módosítása.<br/>
     * Hibalehetőségek: <br/>
     * - Nincs kitöltve az ID <br/>
     * - Nincs kitöltve a név <br/>
     * - Van másik ilyen nevű fejlesztő <br/>
     */
    public synchronized void modifyDeveloper(final @NonNull DeveloperEntity modifiedDeveloperEntity)
        throws InterruptedException, ExecutionException {
        final class RunnableCore implements Runnable {
            private final @NonNull DeveloperEntity modifiedDeveloperEntity;

            public RunnableCore(final @NonNull DeveloperEntity modifiedDeveloperEntity) {
                this.modifiedDeveloperEntity = modifiedDeveloperEntity;
            }

            @SneakyThrows
            @Override
            public void run() {
                entityManager.detach(modifiedDeveloperEntity);
                if (modifiedDeveloperEntity.getId() == null) {
                    throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.DEVELOPER_MODIFY_NOT_SAVED));
                } else if (modifiedDeveloperEntity.getName().isEmpty()) {
                    throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.DEVELOPER_MODIFY_EMPTY_NAME));
                } else {
                    boolean isSameNamedDeveloper = isDeveloperWithNameAndNotId(
                        modifiedDeveloperEntity.getName(), modifiedDeveloperEntity.getId());
                    if (isSameNamedDeveloper) {
                        throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.DEVELOPER_MODIFY_SAME_NAME));
                    } else {
                        developerRepository.saveAndFlush(modifiedDeveloperEntity);
                    }
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(modifiedDeveloperEntity)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
    }

    /**
     * Fejlesztő törlése.<br/>
     * Hibalehetőségek: <br/>
     * - Nincs kitöltve az ID <br/>
     * - A fejlesztő hozzá van rendelve egy vagy több projekthez<br />
     */
    public synchronized void deleteDeveloper(final @NonNull DeveloperEntity developerEntity)
        throws ExecutionException, InterruptedException {
        final class RunnableCore implements Runnable {
            private final @NonNull DeveloperEntity developerEntity;

            public RunnableCore(final @NonNull DeveloperEntity developerEntity) {
                this.developerEntity = developerEntity;
            }

            @Override
            public void run() {
                Long developerID = developerEntity.getId();
                if (developerID == null) {
                    throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.DEVELOPER_DELETE_NOT_SAVED));
                } else {
                    DeveloperEntity actualDeveloperData = developerRepository.findAllById(developerID);
                    if (actualDeveloperData == null) {
                        throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.UNDEFINED));
                    } else if (!actualDeveloperData.getDeveloperProjects().isEmpty()) {
                        throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.DEVELOPER_DELETE_ASSIGNED_TO_PROJECTS));
                    } else {
                        developerRepository.deleteById(developerID);
                        entityManager.flush();
                    }
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(developerEntity)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
    }

    //------------- Lekérdezések -----------------
    /**
     * Fejlesztők listájának lekérdezése
     */
    public synchronized @NonNull List<DeveloperEntity> getDevelopers()
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<List<DeveloperEntity>> {
            @Override
            public List<DeveloperEntity> call() {
                return developerRepository.findAll();
            }
        }
        @NonNull List<DeveloperEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore()).get();
            for( DeveloperEntity developer : result){
                getDeveloperFullTime( developer );
            }
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Egy ProjectTask -hoz tartozó valamennyi fejlesztő lekérdezése
     */
    public synchronized @NonNull List<DeveloperEntity> getDevelopersByProjectTask(final @NonNull ProjectTaskEntity projectTaskEntity)
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<List<DeveloperEntity>> {
            private final @NonNull ProjectTaskEntity projectTaskEntity;

            public CallableCore(final @NonNull ProjectTaskEntity projectTaskEntity) {
                this.projectTaskEntity = projectTaskEntity;
            }

            @Override
            public List<DeveloperEntity> call() {
                if( projectTaskEntity.getId() == null){
                    throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE));
                }
                return developerRepository.findProjectTaskDevelopers( projectTaskEntity.getId());
            }
        }
        @NonNull List<DeveloperEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore(projectTaskEntity)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }


    /**
     * Egy fejlesztő lekérdezése ID szerint.
     */
    public synchronized @Nullable DeveloperEntity getDeveloperById(final @NonNull Long id)
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<DeveloperEntity> {
            private final @NonNull Long id;

            public CallableCore(final @NonNull Long id) {
                this.id = id;
            }

            @Override
            public DeveloperEntity call() {
                return developerRepository.findAllById(id);
            }
        }
        @Nullable DeveloperEntity result = null;
        try {
            result = forkJoinPool.submit(new CallableCore(id)).get();
            if( result != null) getDeveloperFullTime( result );
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Egy fejlesztő lekérdezése név szerint
     */
    public synchronized @Nullable DeveloperEntity getDeveloperByName(final @NonNull String name)
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<DeveloperEntity> {
            private final @NonNull String name;

            public CallableCore(final @NonNull String name) {
                this.name = name;
            }

            @Override
            public DeveloperEntity call() {
                List<DeveloperEntity> namedDevelopers = developerRepository.findAllByName(name);
                if (namedDevelopers.isEmpty()) {
                    return null;
                } else {
                    return namedDevelopers.get(0);
                }
            }
        }
        @Nullable DeveloperEntity result = null;
        try {
            result = forkJoinPool.submit(new CallableCore(name)).get();
            if( result != null) getDeveloperFullTime( result );
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Egy fejlesztő lekérdezése: Van-e már ilyen nevű fejlesztő?
     */
    public synchronized @NonNull Boolean isDeveloperWithNameAndNotId(final @NonNull String name, final @NonNull Long id)
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
                return !developerRepository.findAllByNameAndIdNot(name, id).isEmpty();
            }
        }
        @NonNull Boolean result = false;
        try {
            result = forkJoinPool.submit(new CallableCore(name, id)).get();
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
                @Nullable Long developerId = developer.getId();
                return (developerId == null)
                       ? 0L : developerRepository.sumSpendTimeByDeveloperId(developerId);
            }
        }
        @NonNull Long result = 0L;
        try {
            result = forkJoinPool.submit(new CallableCore(developer)).get();
            developer.setSpendTime( result );
        } catch (ExecutionException ee) {
            PorphyrServiceException.handleExecutionException(ee);
        }
        return result;
    }
}
