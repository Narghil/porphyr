package combit.hu.porphyr.config.service;

import combit.hu.porphyr.config.domain.PermitEntity;
import combit.hu.porphyr.config.repository.PermitRepository;
import combit.hu.porphyr.service.PorphyrServiceException;
import lombok.NonNull;
import lombok.Setter;
import lombok.Synchronized;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Service
@Transactional
@ThreadSafe
public class PermitService {

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull PermitRepository permitRepository;

    @Autowired
    public PermitService(
        final @NonNull PermitRepository permitRepository
    ) {
        this.permitRepository = permitRepository;
    }

    private static final @NonNull ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    /**
     * Egy engedély lekérdezése név szerint
     */
    public synchronized @Nullable PermitEntity getPermitByName(final @NonNull String permitName)
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<PermitEntity> {
            private final @NonNull String permitName;

            public CallableCore(final @NonNull String permitName) {
                this.permitName = permitName;
            }

            @Override
            public PermitEntity call() {
                return permitRepository.findByName(permitName);
            }
        }
        @Nullable
        PermitEntity result = null;
        try {
            result = forkJoinPool.submit(new CallableCore(permitName)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Egy engedély lekérdezése ID szerint
     */
    public synchronized @Nullable PermitEntity getPermitById(final @NonNull Long id)
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<PermitEntity> {
            private final @NonNull Long id;

            public CallableCore(final @NonNull Long id) {
                this.id = id;
            }

            @Override
            public PermitEntity call() {
                return permitRepository.findAllById(id);
            }
        }
        @Nullable
        PermitEntity result = null;
        try {
            result = forkJoinPool.submit(new CallableCore(id)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Engedélyek listájának lekérdezése
     */
    public synchronized @NonNull List<PermitEntity> getPermits()
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<List<PermitEntity>> {
            @Override
            public List<PermitEntity> call() {
                return permitRepository.findAll();
            }
        }
        @NonNull
        List<PermitEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore()).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Engedélyek listájának lekérdezése - szűrve és rendezve
     */
    public synchronized @NonNull List<PermitEntity> getValidPermits()
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<List<PermitEntity>> {
            @Override
            public List<PermitEntity> call() {
                return permitRepository.findAll();
            }
        }
        @NonNull
        List<PermitEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore()).get()
                .stream().filter(PermitEntity::getUsable)
                .sorted(Comparator.comparing(PermitEntity::getDescription))
                .collect(Collectors.toList())
            ;
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Engedély módosítása.<br/>
     */
    public synchronized void modifyPermit(final @NonNull PermitEntity modifiedPermitEntity)
        throws InterruptedException, ExecutionException {
        final class RunnableCore implements Runnable {
            private final @NonNull PermitEntity modifiedPermitEntity;

            public RunnableCore(final @NonNull PermitEntity modifiedPermitEntity) {
                this.modifiedPermitEntity = modifiedPermitEntity;
            }

            @Override
            public void run() {
                permitRepository.saveAndFlush(modifiedPermitEntity);
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(modifiedPermitEntity)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
    }

    /**
     * Új engedély felvétele<br/>
     */
    public synchronized void insertNewPermit(final @NonNull PermitEntity newPermitEntity)
        throws ExecutionException, InterruptedException {
        final class RunnableCore implements Runnable {
            private final @NonNull PermitEntity newPermitEntity;

            public RunnableCore(final @NonNull PermitEntity newPermitEntity) {
                this.newPermitEntity = newPermitEntity;
            }

            @Override
            public void run() {
                permitRepository.saveAndFlush(newPermitEntity);
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(newPermitEntity)).get();
        } catch (ExecutionException exception) {
            PorphyrServiceException.handleExecutionException(exception);
        }
    }
}
