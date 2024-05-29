package combit.hu.porphyr;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.PostEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import combit.hu.porphyr.repository.DeveloperRepository;
import combit.hu.porphyr.repository.PostRepository;
import combit.hu.porphyr.repository.ProjectDeveloperRepository;
import combit.hu.porphyr.repository.ProjectTaskRepository;
import combit.hu.porphyr.service.PorphyrServiceException;
import combit.hu.porphyr.service.PostService;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class PostTests {
    private final @NonNull EntityManager entityManager;
    private final @NonNull PostRepository postRepository;
    private final @NonNull ProjectTaskRepository projectTaskRepository;
    private final @NonNull DeveloperRepository developerRepository;
    private final PostRepository spyPostRepository;
    private final PostService spiedPostService;

    @Autowired
    public PostTests(
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
        this.spiedPostService = new PostService(
            this.entityManager,
            this.postRepository,
            this.projectTaskRepository,
            this.developerRepository,
            projectDeveloperRepository
        );
        this.spyPostRepository = Mockito.mock(
            PostRepository.class, AdditionalAnswers.delegatesTo(this.postRepository)
        );
        this.spiedPostService.setPostRepository(this.spyPostRepository);

        this.spiedPostService.setEntityManager(this.entityManager);
        this.spiedPostService.setProjectTaskRepository(this.projectTaskRepository);
        this.spiedPostService.setDeveloperRepository(this.developerRepository);

        PorphyrServiceException.initExceptionsCounter();
    }

    @BeforeEach
    void setupEach() {
        entityManager.clear();
        Mockito.clearInvocations(spyPostRepository);
    }

    @Test
    @Transactional
    @Rollback
    void postRepositoryTest() {
        ProjectTaskEntity projectTaskEntity;
        DeveloperEntity developerEntity;
        final PostEntity postEntity = new PostEntity();
        // Felvitel: projectTask.id = null (constraint ellenőrzés)
        projectTaskEntity = new ProjectTaskEntity();
        developerEntity = Objects.requireNonNull(developerRepository.findAllById(1L));
        postEntity.setProjectTaskAndDeveloper(projectTaskEntity, developerEntity);
        assertThrows(
            DataIntegrityViolationException.class,
            () -> spyPostRepository.save(postEntity)
        );
        entityManager.clear();
        // Felvitel: developer.id = null (constraint ellenőrzés)
        projectTaskEntity = Objects.requireNonNull(projectTaskRepository.findAllById(1L));
        developerEntity = new DeveloperEntity();
        postEntity.setProjectTaskAndDeveloper(projectTaskEntity, developerEntity);
        assertThrows(
            DataIntegrityViolationException.class,
            () -> spyPostRepository.save(postEntity)
        );
        entityManager.clear();
        // Új post felvétele
        projectTaskEntity = Objects.requireNonNull(projectTaskRepository.findAllById(1L));
        developerEntity = Objects.requireNonNull(developerRepository.findAllById(3L));
        postEntity.setProjectTaskAndDeveloper(projectTaskEntity, developerEntity);
        postEntity.setDescription("Új post.");
        assertDoesNotThrow(() -> spyPostRepository.saveAndFlush(postEntity));
        Long postId = postEntity.getId();
        assertNotNull(postId);
        entityManager.clear();
        // - Visszaolvasás
        assertNotNull(spyPostRepository.findAllById(postId));
        // - Módosítás
        postEntity.setDescription("Módosított post.");
        assertDoesNotThrow(() -> spyPostRepository.saveAndFlush(postEntity));
        // - Tétel törlése
        spyPostRepository.deleteById(postId);
        assertDoesNotThrow(entityManager::flush);
        entityManager.clear();
    }

    @Test
    @Transactional
    @Rollback
    void postServiceInsertTest() {
        PostEntity newPost = new PostEntity();
        ProjectTaskEntity projectTaskEntity;
        DeveloperEntity developerEntity;
        // Felvitel: projectTaskEntity nincs elmentve
        projectTaskEntity = new ProjectTaskEntity();
        developerEntity = Objects.requireNonNull(developerRepository.findAllById(1L));
        newPost.setProjectTaskAndDeveloper(projectTaskEntity, developerEntity);
        assertEquals(
            PorphyrServiceException.Exceptions.POST_INSERT_PROJECTTASK_NOT_SAVED.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedPostService.insertNewPost(newPost)
            ).getMessage()
        );
        entityManager.clear();
        // Felvitel: projectTaskEntity nincs az adatbázisban
        projectTaskEntity = projectTaskRepository.findAllById(5L);
        assertNotNull(projectTaskEntity);
        newPost.setProjectTaskAndDeveloper(projectTaskEntity, developerEntity);
        projectTaskRepository.delete(projectTaskEntity);
        entityManager.flush();
        assertEquals(
            PorphyrServiceException.Exceptions.POST_INSERT_PROJECTTASK_NOT_EXISTS.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedPostService.insertNewPost(newPost)
            ).getMessage()
        );
        entityManager.clear();
        // Felvitel: developerEntity nincs elmentve
        projectTaskEntity = Objects.requireNonNull(projectTaskRepository.findAllById(1L));
        developerEntity = new DeveloperEntity();
        newPost.setProjectTaskAndDeveloper(projectTaskEntity, developerEntity);
        assertEquals(
            PorphyrServiceException.Exceptions.POST_INSERT_DEVELOPER_NOT_SAVED.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedPostService.insertNewPost(newPost)
            ).getMessage()
        );
        entityManager.clear();
        // Felvitel: developerEntity nincs az adatbázisban
        projectTaskEntity = Objects.requireNonNull(projectTaskRepository.findAllById(1L));
        developerEntity = new DeveloperEntity("User will be Deleted");
        developerRepository.saveAndFlush(developerEntity);
        entityManager.flush();
        developerEntity = developerRepository.findAllById(5L);
        assertNotNull(developerEntity);
        newPost.setProjectTaskAndDeveloper(projectTaskEntity, developerEntity);
        developerRepository.delete(developerEntity);
        entityManager.flush();
        assertEquals(
            PorphyrServiceException.Exceptions.POST_INSERT_DEVELOPER_NOT_EXISTS.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedPostService.insertNewPost(newPost)
            ).getMessage()
        );
        entityManager.clear();
        // Felvitel: A projectTaskEntity és a developerEntity más - más projekthez tartozik
        projectTaskEntity = Objects.requireNonNull(projectTaskRepository.findAllById(1L));
        developerEntity = Objects.requireNonNull(developerRepository.findAllById(4L));
        newPost.setProjectTaskAndDeveloper(projectTaskEntity, developerEntity);
        assertEquals(
            PorphyrServiceException.Exceptions.POST_INSERT_TASK_OR_DEVELOPER_NOT_IN_PROJECT.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedPostService.insertNewPost(newPost)
            ).getMessage()
        );
        entityManager.clear();
        // Felvitel: üres post
        projectTaskEntity = Objects.requireNonNull(projectTaskRepository.findAllById(1L));
        developerEntity = Objects.requireNonNull(developerRepository.findAllById(2L));
        newPost.setProjectTaskAndDeveloper(projectTaskEntity, developerEntity);
        // -- NULL
        newPost.setDescription(null);
        assertEquals(
            PorphyrServiceException.Exceptions.POST_INSERT_DESCRIPTION_IS_NULL.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedPostService.insertNewPost(newPost)
            ).getMessage()
        );
        // -- nem NULL, de üres
        newPost.setDescription("");
        assertEquals(
            PorphyrServiceException.Exceptions.POST_INSERT_DESCRIPTION_IS_NULL.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedPostService.insertNewPost(newPost)
            ).getMessage()
        );
        entityManager.clear();
        // Felvitel: minden adat jó, felvitel és visszaolvasás
        projectTaskEntity = Objects.requireNonNull(projectTaskRepository.findAllById(1L));
        developerEntity = Objects.requireNonNull(developerRepository.findAllById(1L));
        newPost.setProjectTaskAndDeveloper(projectTaskEntity, developerEntity);
        newPost.setDescription("Új post.");
        assertDoesNotThrow(() -> spiedPostService.insertNewPost(newPost));
        verify(spyPostRepository, times(1)).saveAndFlush(newPost);
        assertNotNull(newPost.getId());
        assertEquals(
            newPost, postRepository.findAllById(newPost.getId())
        );
        entityManager.clear();
        // - Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> PorphyrServiceException.isAllExceptionsThrown(PorphyrServiceException.ExceptionGroups.POSTS_INSERT));
    }

    @Test
    @Transactional
    @Rollback
    void postServiceModifyTest() throws ExecutionException, InterruptedException {
        // -  postEntity nincs elmentve
        PostEntity newPost = new PostEntity();
        assertEquals(
            PorphyrServiceException.Exceptions.POST_MODIFY_NOT_SAVED.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedPostService.modifyPost(newPost)
            ).getMessage()
        );
        entityManager.clear();
        // -  Üres post
        PostEntity modifyPost = postRepository.findAllById(1L);
        assertNotNull(modifyPost);
        modifyPost.setDescription(null);
        assertEquals(
            PorphyrServiceException.Exceptions.POST_MODIFY_DESCRIPTION_IS_NULL.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedPostService.modifyPost(modifyPost)
            ).getMessage()
        );
        modifyPost.setDescription("");
        assertEquals(
            PorphyrServiceException.Exceptions.POST_MODIFY_DESCRIPTION_IS_NULL.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedPostService.modifyPost(modifyPost)
            ).getMessage()
        );
        entityManager.clear();
        // - jó adatokkal
        modifyPost.setDescription("Módosított post");
        assertDoesNotThrow(() -> spiedPostService.modifyPost(modifyPost));
        assertDoesNotThrow(entityManager::flush);
        verify(spyPostRepository, times(1)).saveAndFlush(modifyPost);
        entityManager.clear();
        assertEquals(modifyPost, spiedPostService.getPostById(1L));
        // - Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> PorphyrServiceException.isAllExceptionsThrown(PorphyrServiceException.ExceptionGroups.POSTS_MODIFY));
    }

    @Test
    @Transactional
    @Rollback
    void postServiceDeleteTest() {
        // -  postEntity nincs elmentve
        PostEntity newPost = new PostEntity();
        assertEquals(
            PorphyrServiceException.Exceptions.POST_DELETE_NOT_SAVED.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedPostService.deletePost(newPost)
            ).getMessage()
        );
        entityManager.clear();
        // -  Törlés megfelelő adatokkal
        PostEntity deletePost = postRepository.findAllById(1L);
        assertNotNull(deletePost);
        assertDoesNotThrow(() -> spiedPostService.deletePost(deletePost));
        entityManager.clear();
        Long deletedEntityId = deletePost.getId();
        assertNotNull(deletedEntityId);
        verify(spyPostRepository, times(1)).deleteById(deletedEntityId);
        assertNull(spyPostRepository.findAllById(1L));
        // -  Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> PorphyrServiceException.isAllExceptionsThrown(PorphyrServiceException.ExceptionGroups.POSTS_DELETE));
    }

    @Test
    @Transactional
    @Rollback
    void postServiceQueriesTest() throws ExecutionException, InterruptedException {
        ProjectTaskEntity projectTaskEntity = projectTaskRepository.findAllById(1L);
        assertNotNull(projectTaskEntity);
        // -  getPostById
        assertNotNull(spiedPostService.getPostById(1L));
        assertNotNull(spiedPostService.getPostById(2L));
        // -  getPostsByProjectTask
        assertEquals(2, spiedPostService.getPostsByProjectTask(projectTaskEntity).size());
    }
}
