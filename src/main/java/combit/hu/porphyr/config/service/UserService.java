package combit.hu.porphyr.config.service;

import combit.hu.porphyr.config.repository.PermitRepository;
import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.config.domain.PermitEntity;
import combit.hu.porphyr.config.domain.RoleEntity;
import combit.hu.porphyr.config.domain.UserEntity;
import combit.hu.porphyr.config.repository.UserRepository;
import combit.hu.porphyr.config.RequestsConstants;
import combit.hu.porphyr.service.PorphyrServiceException;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.Synchronized;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static combit.hu.porphyr.config.RequestsConstants.PROTECTED_REQUEST_CALLS;

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

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull PermitRepository permitRepository;

    @Autowired
    public UserService(
        final @NonNull EntityManager entityManager,
        final @NonNull UserRepository userRepository,
        final @NonNull PermitRepository permitRepository
    ) {
        this.entityManager = entityManager;
        this.userRepository = userRepository;
        this.permitRepository = permitRepository;
    }

    private static final @NonNull ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        @Nullable
        UserEntity user = null;
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
        @Nullable
        UserEntity result = null;
        try {
            result = forkJoinPool.submit(new CallableCore(loginName)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Egy felhasználó lekérdezése ID szerint
     */
    public synchronized @Nullable UserEntity getUserById(final @NonNull Long id)
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<UserEntity> {
            private final @NonNull Long id;

            public CallableCore(final @NonNull Long id) {
                this.id = id;
            }

            @Override
            public UserEntity call() {
                return userRepository.findAllById(id);
            }
        }
        @Nullable
        UserEntity result = null;
        try {
            result = forkJoinPool.submit(new CallableCore(id)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
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
        @NonNull
        List<UserEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore()).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
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
        @NonNull
        Boolean result = false;
        try {
            result = forkJoinPool.submit(new CallableCore(loginName, id)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Új user felvétele<br/>
     * Hibalehetőségek: <br/>
     * - A megadott jelszavak nem azonosak
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
                if (!Objects.equals(newUserEntity.getNewPassword(), newUserEntity.getRetypedPassword())) {
                    throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.USER_INSERT_DIFFERENT_PASSWORDS));
                } else if (userRepository.findByLoginName(newUserEntity.getLoginName()) != null) {
                    throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.USER_INSERT_SAME_LOGIN_NAME));
                } else {
                    userRepository.saveAndFlush(newUserEntity);
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(newUserEntity)).get();
        } catch (ExecutionException exception) {
            PorphyrServiceException.handleExecutionException(exception);
        }
    }

    /**
     * user módosítása.<br/>
     * Hibalehetőségek: <br/>
     * - A megadott jelszavak nem egyeznek meg
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
                if (!Objects.equals(modifiedUserEntity.getNewPassword(), modifiedUserEntity.getRetypedPassword())) {
                    throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.USER_MODIFY_DIFFERENT_PASSWORDS));
                } else if (modifiedUserEntity.getId() == null) {
                    throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.USER_MODIFY_NOT_SAVED));
                } else {
                    entityManager.clear();
                    if (isUserWithLoginNameAndNotId(
                        modifiedUserEntity.getLoginName(), modifiedUserEntity.getId())
                    ) {
                        throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.USER_MODIFY_SAME_LOGIN_NAME));
                    } else {
                        userRepository.saveAndFlush(modifiedUserEntity);
                    }
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(modifiedUserEntity)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
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
                    throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.USER_DELETE_NOT_SAVED));
                } else {
                    UserEntity actualUserData = userRepository.findAllById(userID);
                    if (actualUserData == null) {
                        throw (new PorphyrServiceException(PorphyrServiceException.Exceptions.UNDEFINED));
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
            PorphyrServiceException.handleExecutionException(executionException);
        }
    }

    /**
     * Felhasználó valamennyi permit-jének lekérdezése
     */
    public synchronized Boolean getUserPermits(
        final @NonNull UserEntity userEntity,
        final @NonNull List<String> userRoleNames,
        final @NonNull List<String> userPermitNames,
        final @NonNull List<String> userPermittedRequestCalls,
        final @NonNull List<DeveloperEntity> userDevelopers
    )
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<Boolean> {
            private final @NonNull UserEntity userEntity;
            private final @NonNull List<String> userRoleNames;
            private final @NonNull List<String> userPermitNames;
            private final @NonNull List<String> userPermittedRequestCalls;
            private final @NonNull List<DeveloperEntity> userDevelopers;

            public CallableCore(
                final @NonNull UserEntity userEntity,
                final @NonNull List<String> userRoleNames,
                final @NonNull List<String> userPermitNames,
                final @NonNull List<String> userPermittedRequestCalls,
                final @NonNull List<DeveloperEntity> userDevelopers
            ) {
                this.userEntity = userEntity;
                this.userRoleNames = userRoleNames;
                this.userPermitNames = userPermitNames;
                this.userPermittedRequestCalls = userPermittedRequestCalls;
                this.userDevelopers = userDevelopers;
            }

            @Override
            public Boolean call() throws InterruptedException, ExecutionException {
                userDevelopers.addAll(userEntity.getDevelopers());
                for (RoleEntity role : userEntity.getRoles()) {
                    for (PermitEntity permit : role.getPermits()) {
                        addPermit(permit.getName(), userPermitNames, userPermittedRequestCalls);
                    }
                }
                changeIfPermitAll(userPermitNames, userPermittedRequestCalls);
                userRoleNames.addAll(userEntity.getRoles()
                    .stream()
                    .map(RoleEntity::getRole)
                    .collect(Collectors.toList()));

                return true;
            }
        }

        @NonNull
        Boolean result = false;
        try {
            userPermitNames.clear();
            userPermittedRequestCalls.clear();
            userDevelopers.clear();
            result = forkJoinPool.submit(new CallableCore(
                userEntity,
                userRoleNames,
                userPermitNames,
                userPermittedRequestCalls,
                userDevelopers
            )).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }

        return result;
    }

    /**
     * Az aktuális felhasználó valamennyi permit-jének lekérdezése
     */
    @SneakyThrows
    public synchronized @NonNull Boolean getActualUserPermits(
        final @NonNull List<String> userRoleNames,
        final @NonNull List<String> userPermitNames,
        final @NonNull List<String> userPermittedRequestCalls,
        final @NonNull List<DeveloperEntity> userDevelopers
    ) {
        final @Nullable UserEntity userEntity = getUserByLoginName(
            SecurityContextHolder.getContext().getAuthentication().getName()
        );
        return (userEntity != null) && getUserPermits(
            userEntity,
            userRoleNames,
            userPermitNames,
            userPermittedRequestCalls,
            userDevelopers
        );
    }

    // --------------------------- Helpers --------------------
    // Egy permit és a hozzá tartozó URI-k felvétele a gyűjtőkbe
    private void addPermit(
        final @NonNull String permitName,
        final @NonNull List<String> userPermitNames,
        final @NonNull List<String> userPermittedRequestCalls
    ) throws ExecutionException, InterruptedException {
        if (!userPermitNames.contains(permitName)) {
            userPermitNames.add(permitName);
            List<String> requestCalls = PROTECTED_REQUEST_CALLS.get(permitName);
            if (requestCalls == null) {
                final @NonNull PermitService permitService = new PermitService(permitRepository);
                PermitEntity permitEntity = permitService.getPermitByName(permitName);
                if (permitEntity != null && permitEntity.getUsable()) {
                    throw new NullPointerException("Empty protected request calls on permit:" + permitName);
                }
            } else {
                for (String requestCall : requestCalls) {
                    if (!userPermittedRequestCalls.contains(requestCall)) {
                        userPermittedRequestCalls.add(requestCall);
                    }
                }
            }
        }
    }

    // Ha a user-nek van PERMIT_ALL jogosultsága, akkor minden jogosultságot megkap.
    private void changeIfPermitAll(
        final @NonNull List<String> userPermitNames,
        final @NonNull List<String> userPermittedRequestCalls
    ) {
        if (userPermitNames.contains(RequestsConstants.PERMIT_ALL)) {
            userPermitNames.clear();
            userPermittedRequestCalls.clear();
            for (Map.Entry<String, List<String>> entry : PROTECTED_REQUEST_CALLS.entrySet()) {
                userPermitNames.add(entry.getKey());
                userPermittedRequestCalls.addAll(entry.getValue());
            }
        }
    }
}