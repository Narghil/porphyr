package combit.hu.porphyr.service;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.PostEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import combit.hu.porphyr.repository.DeveloperRepository;
import combit.hu.porphyr.repository.ProjectDeveloperRepository;
import combit.hu.porphyr.repository.PostRepository;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

@Service
@Transactional
@ThreadSafe
public class PostService {

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull EntityManager entityManager;

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull PostRepository postRepository;

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull ProjectTaskRepository projectTaskRepository;

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull DeveloperRepository developerRepository;

    @Setter(onMethod_ = {@Synchronized})
    @GuardedBy("this")
    private @NonNull ProjectDeveloperRepository projectDeveloperRepository;

    @Autowired
    public PostService(
        final @NonNull EntityManager entityManager,
        final @NonNull PostRepository postRepository,
        final @NonNull ProjectTaskRepository projectTaskRepository,
        final @NonNull DeveloperRepository developerRepository,
        final @NonNull ProjectDeveloperRepository projectDeveloperRepository
    ) {
        this.entityManager = entityManager;
        this.postRepository = postRepository;
        this.projectTaskRepository = projectTaskRepository;
        this.developerRepository = developerRepository;
        this.projectDeveloperRepository = projectDeveloperRepository;
    }

    private final @NonNull ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    /**
     * Post felvétele.
     * Hibalehetőségek: <br />
     * - projectTaskEntity nincs elmentve  <br />
     * - projectTaskEntity nincs az adatbázisban  <br />
     * - developerEntity nincs elmentve  <br />
     * - developerEntity nincs az adatbázisban  <br />
     * - A projectTaskEntity projektje nincs a developer projektjei között <br />
     * - A hozzászólás üres <br />
     */
    public synchronized void insertNewPost(
        final @NonNull PostEntity postEntity
    ) throws ExecutionException, InterruptedException {
        try {
            forkJoinPool.submit(new InsertRunnableCore(postEntity)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
    }

    final class InsertRunnableCore implements Runnable {
        final @NonNull PostEntity postEntity;

        public InsertRunnableCore(final @NonNull PostEntity postEntity) {
            this.postEntity = postEntity;
        }

        @Override
        public void run() {
            @Nullable
            PorphyrServiceException porphyrServiceException = null;
            final @NonNull ProjectTaskEntity projectTaskEntity = postEntity.getProjectTaskEntity();
            final @NonNull DeveloperEntity developerEntity = postEntity.getDeveloperEntity();
            entityManager.detach(postEntity);
            if (projectTaskEntity.getId() == null) {
                porphyrServiceException = new PorphyrServiceException(PorphyrServiceException.Exceptions.POST_INSERT_PROJECTTASK_NOT_SAVED);
            } else if (projectTaskRepository.findAllById(projectTaskEntity.getId()) == null) {
                porphyrServiceException = new PorphyrServiceException(PorphyrServiceException.Exceptions.POST_INSERT_PROJECTTASK_NOT_EXISTS);
            } else if (developerEntity.getId() == null) {
                porphyrServiceException = new PorphyrServiceException(PorphyrServiceException.Exceptions.POST_INSERT_DEVELOPER_NOT_SAVED);
            } else if (developerRepository.findAllById(developerEntity.getId()) == null) {
                porphyrServiceException = new PorphyrServiceException(PorphyrServiceException.Exceptions.POST_INSERT_DEVELOPER_NOT_EXISTS);
            } else {
                boolean isNotInSameProject = projectDeveloperRepository.findAllByProjectEntityAndDeveloperEntity(
                    projectTaskEntity.getProjectEntity(), developerEntity) == null;
                if (isNotInSameProject) {
                    porphyrServiceException = new PorphyrServiceException(PorphyrServiceException.Exceptions.POST_INSERT_TASK_OR_DEVELOPER_NOT_IN_PROJECT);
                } else if (postEntity.getDescription() == null || postEntity.getDescription().isEmpty()) {
                    porphyrServiceException = new PorphyrServiceException(PorphyrServiceException.Exceptions.POST_INSERT_DESCRIPTION_IS_NULL);
                }
            }
            if (porphyrServiceException != null) {
                throw porphyrServiceException;
            } else {
                postRepository.saveAndFlush(postEntity);
            }
        }
    }

    /**
     * Post módosítása (csak a beírt szöveg módosítható)
     * Hibalehetőségek: <br />
     * - postEntity nincs elmentve  <br />
     * - A szöveg üres. <br />
     */
    public synchronized void modifyPost(
        final @NonNull PostEntity postEntity
    ) throws ExecutionException, InterruptedException {
        final class RunnableCore implements Runnable {
            final @NonNull PostEntity postEntity;

            public RunnableCore(final @NonNull PostEntity postEntity) {
                this.postEntity = postEntity;
            }

            @Override
            public void run() {
                if (postEntity.getId() == null) {
                    throw new PorphyrServiceException(PorphyrServiceException.Exceptions.POST_MODIFY_NOT_SAVED);
                } else if (postEntity.getDescription() == null || postEntity.getDescription().isEmpty()) {
                    throw new PorphyrServiceException(PorphyrServiceException.Exceptions.POST_MODIFY_DESCRIPTION_IS_NULL);
                } else {
                    postRepository.saveAndFlush(postEntity);
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(postEntity)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
    }

    /**
     * Post törlése
     * Hibalehetőségek: <br />
     * - postEntity nincs elmentve  <br />
     */
    public synchronized void deletePost(
        final @NonNull PostEntity postEntity
    ) throws ExecutionException, InterruptedException {
        final class RunnableCore implements Runnable {
            final @NonNull PostEntity postEntity;

            public RunnableCore(final @NonNull PostEntity postEntity) {
                this.postEntity = postEntity;
            }

            @Override
            public void run() {
                if (postEntity.getId() == null) {
                    throw new PorphyrServiceException(PorphyrServiceException.Exceptions.POST_DELETE_NOT_SAVED);
                } else {
                    postRepository.deleteById(postEntity.getId());
                    entityManager.flush();
                }
            }
        }
        try {
            forkJoinPool.submit(new RunnableCore(postEntity)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
    }

    // ---- Lekérdezések ----

    /**
     * Post lekérdezése ID szerint.
     */
    public synchronized @Nullable PostEntity getPostById(final @NonNull Long id)
        throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<PostEntity> {
            final @NonNull Long id;

            public CallableCore(final @NonNull Long id) {
                this.id = id;
            }

            @Override
            public PostEntity call() {
                return postRepository.findAllById(id);
            }
        }
        @Nullable
        PostEntity result = null;
        try {
            result = forkJoinPool.submit(new CallableCore(id)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }

    /**
     * Egy ProjectTask-hoz tartozó Post-ok lekérdezése, időben csökkenő sorrendben
     */
    public synchronized @NonNull List<PostEntity> getPostsByProjectTask(
        final @NonNull ProjectTaskEntity projectTaskEntity
    ) throws ExecutionException, InterruptedException {
        final class CallableCore implements Callable<List<PostEntity>> {
            final @NonNull ProjectTaskEntity projectTaskEntity;

            public CallableCore(final @NonNull ProjectTaskEntity projectTaskEntity) {
                this.projectTaskEntity = projectTaskEntity;
            }

            @Override
            public List<PostEntity> call() {
                return postRepository.findAllByProjectTaskEntityOrderByCreatedDesc(projectTaskEntity);
            }
        }
        @NonNull
        List<PostEntity> result = new ArrayList<>();
        try {
            result = forkJoinPool.submit(new CallableCore(projectTaskEntity)).get();
        } catch (ExecutionException executionException) {
            PorphyrServiceException.handleExecutionException(executionException);
        }
        return result;
    }
}
