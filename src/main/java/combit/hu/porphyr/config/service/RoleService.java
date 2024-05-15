package combit.hu.porphyr.config.service;

import combit.hu.porphyr.config.domain.RoleEntity;
import combit.hu.porphyr.config.repository.RoleRepository;
import combit.hu.porphyr.service.PorphyrServiceException;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

@Service
@Transactional
@ThreadSafe
public class RoleService {

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull EntityManager entityManager;

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull RoleRepository roleRepository;

    @Autowired
    public RoleService(
        final @NonNull EntityManager entityManager,
        final @NonNull RoleRepository roleRepository
    ) {
        this.entityManager = entityManager;
        this.roleRepository = roleRepository;
    }

    private static final @NonNull ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    /**
     * Egy jogkör lekérdezése név szerint
     */
    public synchronized @Nullable RoleEntity getRoleByRole(final @NonNull String roleName)
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<RoleEntity> {
            private final @NonNull String roleName;

            public CallableCore(final @NonNull String roleName) {
                this.roleName = roleName;
            }

            @Override
            public RoleEntity call() {
                return roleRepository.findByRole(roleName);
            }
        }
        @Nullable RoleEntity result = null;
        try {
            result = forkJoinPool.submit(new CallableCore(roleName)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Egy jogkör lekérdezése ID szerint
     */
    public synchronized @Nullable RoleEntity getRoleById(final @NonNull Long id)
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<RoleEntity> {
            private final @NonNull Long id;

            public CallableCore(final @NonNull Long id) {
                this.id = id;
            }

            @Override
            public RoleEntity call() {
                return roleRepository.findAllById(id);
            }
        }
        @Nullable RoleEntity result = null;
        try {
            result = forkJoinPool.submit(new CallableCore(id)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Jogkörök listájának lekérdezése
     */
    public synchronized @NonNull List<RoleEntity> getRoles()
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<List<RoleEntity>> {
            @Override
            public List<RoleEntity> call() {
                return roleRepository.findAll();
            }
        }
        @NonNull List<RoleEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore()).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Egy jogkör lekérdezése: Van-e már ilyen nevű jogkör?
     */
    public synchronized @NonNull Boolean isRoleWithLoginNameAndNotId(
        final @NonNull String roleName,
        final @NonNull Long id
    )
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<Boolean> {
            private final @NonNull String roleName;
            private final @NonNull Long id;

            public CallableCore(final @NonNull String roleName, final @NonNull Long id) {
                this.roleName = roleName;
                this.id = id;
            }

            @Override
            public Boolean call() {
                return (!roleRepository.findAllByRoleAndIdNot(roleName, id).isEmpty());
            }
        }
        @NonNull Boolean result = false;
        try {
            result = forkJoinPool.submit(new CallableCore(roleName, id)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Új jogkör felvétele<br/>
     * Hibalehetőségek: <br/>
     * - Már van ilyen nevű jogkör <br/>
     */
    public synchronized void insertNewRole(final @NonNull RoleEntity newRoleEntity)
        throws ExecutionException, InterruptedException {
        final class RunnableCore implements Runnable {
            private final @NonNull RoleEntity newRoleEntity;

            public RunnableCore(final @NonNull RoleEntity newRoleEntity) {
                this.newRoleEntity = newRoleEntity;
            }

            @Override
            public void run() {
                if (roleRepository.findByRole(newRoleEntity.getRole()) != null) {
                    throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.ROLES_INSERT_SAME_NAME));
                } else {
                    roleRepository.saveAndFlush(newRoleEntity);
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(newRoleEntity)).get();
        } catch (ExecutionException exception) {
            PorphyrServiceException.handleExecutionException(exception);
        }
    }

    /**
     * Jogkör módosítása.<br/>
     * Hibalehetőségek: <br/>
     * - A jogkör még nem volt elmentve
     * - Van másik ilyen nevű jogkör <br/>
     */
    public synchronized void modifyRole(final @NonNull RoleEntity modifiedRoleEntity)
        throws InterruptedException, ExecutionException {
        final class RunnableCore implements Runnable {
            private final @NonNull RoleEntity modifiedRoleEntity;

            public RunnableCore(final @NonNull RoleEntity modifiedRoleEntity) {
                this.modifiedRoleEntity = modifiedRoleEntity;
            }

            @SneakyThrows
            @Override
            public void run() {
                if (modifiedRoleEntity.getId() == null) {
                    throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.ROLES_MODIFY_NOT_SAVED));
                } else {
                    entityManager.clear();
                    if (isRoleWithLoginNameAndNotId(
                        modifiedRoleEntity.getRole(), modifiedRoleEntity.getId())
                    ) {
                        throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.ROLES_MODIFY_SAME_NAME));
                    } else {
                        roleRepository.saveAndFlush(modifiedRoleEntity);
                    }
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(modifiedRoleEntity)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
    }

    /**
     * Jogkör törlése.<br/>
     * Hibalehetőségek: <br/>
     * - Nincs kitöltve az ID <br/>
     * - Tartozik hozzá felhasználó
     */
    public synchronized void deleteRole(final @NonNull RoleEntity roleEntity)
        throws ExecutionException, InterruptedException {
        final class RunnableCore implements Runnable {
            private final @NonNull RoleEntity roleEntity;

            public RunnableCore(final @NonNull RoleEntity roleEntity) {
                this.roleEntity = roleEntity;
            }

            @Override
            public void run() {
                Long roleID = roleEntity.getId();
                if (roleID == null) {
                    throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.ROLES_DELETE_NOT_SAVED));
                } else {
                    RoleEntity actualRoleData = roleRepository.findAllById(roleID);
                    if (actualRoleData == null) {
                        throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.UNDEFINED));
                    } else if (!actualRoleData.getUsers().isEmpty()) {
                        throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.ROLES_DELETE_ATTACHED_USERS));
                    } else {
                        roleRepository.deleteById(roleID);
                        entityManager.flush();
                    }
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(roleEntity)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
    }
}
