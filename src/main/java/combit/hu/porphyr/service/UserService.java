package combit.hu.porphyr.service;

import combit.hu.porphyr.domain.UserEntity;
import combit.hu.porphyr.repository.UserRepository;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.Synchronized;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
public class UserService implements UserDetailsService {

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull EntityManager entityManager;

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull UserRepository userRepository;

    @Autowired
    public UserService(
        final @NonNull EntityManager entityManager,
        final @NonNull UserRepository userRepository
    ) {
        this.entityManager = entityManager;
        this.userRepository = userRepository;
    }

    private static final @NonNull ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        @Nullable UserEntity user = null;
        try {
            user = getUserByLoginName(userName);
        } catch (ExecutionException executionException) {
            throw new IllegalStateException(executionException);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
        if (user == null) {
            throw new UsernameNotFoundException(userName);
        }
        return new UserDetailsImp(user);
    }

    /**
     * Egy felhasználó lekérdezése login név szerint
     */
    public synchronized @Nullable UserEntity getUserByLoginName(final @NonNull String loginName)
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<UserEntity> {
            private final @NonNull String loginName;

            public CallableCore(final @NonNull String loginName) {
                this.loginName = loginName;
            }

            @Override
            public UserEntity call() {
                return userRepository.findByLoginName(loginName);
            }
        }
        @Nullable UserEntity result = null;
        try {
            result = forkJoinPool.submit(new CallableCore(loginName)).get();
        } catch (ExecutionException executionException) {
            ServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Felhasználók listájának lekérdezése
     */
    public synchronized @NonNull List<UserEntity> getUsers()
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<List<UserEntity>> {
            @Override
            public List<UserEntity> call() {
                return userRepository.findAll();
            }
        }
        @NonNull List<UserEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore()).get();
        } catch (ExecutionException executionException) {
            ServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Egy felhasználó lekérdezése: Van-e már ilyen nevű felhasználó?
     */
    public synchronized @NonNull Boolean isUserWithLoginNameAndNotId(
        final @NonNull String loginName,
        final @NonNull Long id
    )
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<Boolean> {
            private final @NonNull String loginName;
            private final @NonNull Long id;

            public CallableCore(final @NonNull String loginName, final @NonNull Long id) {
                this.loginName = loginName;
                this.id = id;
            }

            @Override
            public Boolean call() {
                return (!userRepository.findAllByLoginNameAndIdNot(loginName, id).isEmpty());
            }
        }
        @NonNull Boolean result = false;
        try {
            result = forkJoinPool.submit(new CallableCore(loginName, id)).get();
        } catch (ExecutionException executionException) {
            ServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Új user felvétele<br/>
     * Hibalehetőségek: <br/>
     * - Már van ilyen login nevű user <br/>
     */
    public synchronized void insertNewUser(final @NonNull UserEntity newUserEntity)
        throws ExecutionException, InterruptedException {
        final class RunnableCore implements Runnable {
            private final @NonNull UserEntity newUserEntity;

            public RunnableCore(final @NonNull UserEntity newUserEntity) {
                this.newUserEntity = newUserEntity;
            }

            @Override
            public void run() {
                if (userRepository.findByLoginName(newUserEntity.getLoginName()) != null) {
                    throw (new ServiceException(ServiceException.Exceptions.USER_INSERT_SAME_LOGIN_NAME));
                } else {
                    userRepository.saveAndFlush(newUserEntity);
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(newUserEntity)).get();
        } catch (ExecutionException exception) {
            ServiceException.handleExecutionException(exception);
        }
    }

    /**
     * user módosítása.<br/>
     * Hibalehetőségek: <br/>
     * - A user még nem volt elmentve
     * - Van másik ilyen login nevű user <br/>
     */
    public synchronized void modifyUser(final @NonNull UserEntity modifiedUserEntity)
        throws InterruptedException, ExecutionException {
        final class RunnableCore implements Runnable {
            private final @NonNull UserEntity modifiedUserEntity;

            public RunnableCore(final @NonNull UserEntity modifiedUserEntity) {
                this.modifiedUserEntity = modifiedUserEntity;
            }

            @SneakyThrows
            @Override
            public void run() {
                if (modifiedUserEntity.getId() == null) {
                    throw (new ServiceException(ServiceException.Exceptions.USER_MODIFY_NOT_SAVED));
                } else {
                    entityManager.clear();
                    if (isUserWithLoginNameAndNotId(
                        modifiedUserEntity.getLoginName(), modifiedUserEntity.getId())
                    ) {
                        throw (new ServiceException(ServiceException.Exceptions.USER_MODIFY_SAME_LOGIN_NAME));
                    } else {
                        userRepository.saveAndFlush(modifiedUserEntity);
                    }
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(modifiedUserEntity)).get();
        } catch (ExecutionException executionException) {
            ServiceException.handleExecutionException(executionException);
        }
    }

    /**
     * Felhasználó törlése.<br/>
     * Hibalehetőségek: <br/>
     * - Nincs kitöltve az ID <br/>
     */
    public synchronized void deleteUser(final @NonNull UserEntity userEntity)
        throws ExecutionException, InterruptedException {
        final class RunnableCore implements Runnable {
            private final @NonNull UserEntity userEntity;

            public RunnableCore(final @NonNull UserEntity userEntity) {
                this.userEntity = userEntity;
            }

            @Override
            public void run() {
                Long userID = userEntity.getId();
                if (userID == null) {
                    throw (new ServiceException(ServiceException.Exceptions.USER_DELETE_NOT_SAVED));
                } else {
                    UserEntity actualUserData = userRepository.findAllById(userID);
                    if (actualUserData == null) {
                        throw (new ServiceException(ServiceException.Exceptions.UNDEFINED));
                    } else {
                        userRepository.deleteById(userID);
                        entityManager.flush();
                    }
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(userEntity)).get();
        } catch (ExecutionException executionException) {
            ServiceException.handleExecutionException(executionException);
        }
    }
}
