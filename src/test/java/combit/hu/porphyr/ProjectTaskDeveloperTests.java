package combit.hu.porphyr;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskDeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import combit.hu.porphyr.repository.DeveloperRepository;
import combit.hu.porphyr.repository.ProjectDeveloperRepository;
import combit.hu.porphyr.repository.ProjectRepository;
import combit.hu.porphyr.repository.ProjectTaskDeveloperRepository;
import combit.hu.porphyr.repository.ProjectTaskRepository;
import combit.hu.porphyr.service.ProjectTaskDeveloperService;
import combit.hu.porphyr.service.PorphyrServiceException;
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

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class ProjectTaskDeveloperTests {
    private final @NonNull EntityManager entityManager;
    private final @NonNull ProjectTaskDeveloperRepository projectTaskDeveloperRepository;
    private final @NonNull ProjectTaskRepository projectTaskRepository;
    private final @NonNull DeveloperRepository developerRepository;
    private final @NonNull ProjectTaskDeveloperRepository spyProjectTaskDeveloperRepository;
    private final @NonNull ProjectTaskDeveloperService spiedProjectTaskDeveloperService;
    private final @NonNull ProjectRepository projectRepository;
    private final @NonNull ProjectDeveloperRepository projectDeveloperRepository;

    @Autowired
    public ProjectTaskDeveloperTests(
        final @NonNull EntityManager entityManager,
        final @NonNull ProjectTaskDeveloperRepository projectTaskDeveloperRepository,
        final @NonNull ProjectTaskRepository projectTaskRepository,
        final @NonNull DeveloperRepository developerRepository,
        final @NonNull ProjectDeveloperRepository projectDeveloperRepository,
        final @NonNull ProjectRepository projectRepository
    ) {
        this.entityManager = entityManager;
        this.projectTaskDeveloperRepository = projectTaskDeveloperRepository;
        this.projectTaskRepository = projectTaskRepository;
        this.developerRepository = developerRepository;
        this.projectRepository = projectRepository;
        this.projectDeveloperRepository = projectDeveloperRepository;
        this.spiedProjectTaskDeveloperService = new ProjectTaskDeveloperService(
            this.entityManager,
            this.projectTaskDeveloperRepository,
            this.projectTaskRepository,
            this.developerRepository
        );
        this.spyProjectTaskDeveloperRepository = Mockito.mock(
            ProjectTaskDeveloperRepository.class, AdditionalAnswers.delegatesTo(this.projectTaskDeveloperRepository)
        );
        this.spiedProjectTaskDeveloperService.setProjectTaskDeveloperRepository(this.spyProjectTaskDeveloperRepository);
        this.spiedProjectTaskDeveloperService.setProjectTaskRepository(this.projectTaskRepository);
        this.spiedProjectTaskDeveloperService.setDeveloperRepository(this.developerRepository);
        this.spiedProjectTaskDeveloperService.setEntityManager(this.entityManager);

        PorphyrServiceException.initExceptionsCounter();
    }

    @BeforeEach
    void setupEach() {
        entityManager.clear();
        Mockito.clearInvocations(spyProjectTaskDeveloperRepository);
    }

    @Test
    @Transactional
    @Rollback
    void projectTaskDeveloperRepositoryTest() {
        ProjectTaskEntity projectTaskEntity;
        ProjectDeveloperEntity projectDeveloperEntity;
        final ProjectTaskDeveloperEntity projectTaskDeveloperEntity = new ProjectTaskDeveloperEntity();

        // - projectTask.id = null (constraint ellenőrzés)
        projectTaskEntity = new ProjectTaskEntity();
        projectDeveloperEntity = projectDeveloperRepository.findAllById(1L);

        projectTaskDeveloperEntity.setProjectTaskAndDeveloper(
            projectTaskEntity,
            Objects.requireNonNull(projectDeveloperEntity).getDeveloperEntity()
        );
        assertThrows(
            DataIntegrityViolationException.class,
            () -> spyProjectTaskDeveloperRepository.save(projectTaskDeveloperEntity)
        );
        entityManager.clear();
        // - projectDeveloper.id = null (constraint ellenőrzés)
        projectTaskEntity = projectTaskRepository.findAllById(1L);
        projectDeveloperEntity = new ProjectDeveloperEntity();
        projectTaskDeveloperEntity.setProjectTaskAndDeveloper(
            projectTaskEntity,
            Objects.requireNonNull(projectDeveloperEntity).getDeveloperEntity()
        );
        assertThrows(
            DataIntegrityViolationException.class,
            () -> spyProjectTaskDeveloperRepository.save(projectTaskDeveloperEntity)
        );
        entityManager.clear();
        // - Már létező tétel felvétele
        projectTaskEntity = projectTaskRepository.findAllById(1L);
        projectDeveloperEntity = projectDeveloperRepository.findAllById(1L);
        projectTaskDeveloperEntity.setProjectTaskAndDeveloper(
            projectTaskEntity,
            Objects.requireNonNull(projectDeveloperEntity).getDeveloperEntity()
        );
        assertThrows(
            DataIntegrityViolationException.class,
            () -> spyProjectTaskDeveloperRepository.save(projectTaskDeveloperEntity)
        );
        entityManager.clear();
        // - Nem létező tétel felvétele
        projectTaskEntity = projectTaskRepository.findAllById(1L);
        projectDeveloperEntity = projectDeveloperRepository.findAllById(3L);
        projectTaskDeveloperEntity.setProjectTaskAndDeveloper(
            projectTaskEntity,
            Objects.requireNonNull(projectDeveloperEntity).getDeveloperEntity()
        );
        projectTaskDeveloperEntity.setSpendTime(0L);
        assertDoesNotThrow(() -> spyProjectTaskDeveloperRepository.saveAndFlush(projectTaskDeveloperEntity));
        Long projectTaskDeveloperId = projectTaskDeveloperEntity.getId();
        assertNotNull(projectTaskDeveloperId);
        entityManager.clear();
        // - Visszaolvasás
        assertNotNull(spyProjectTaskDeveloperRepository.findAllById(projectTaskDeveloperId));
        // - Módosítás
        projectTaskDeveloperEntity.setSpendTime(1L);
        assertDoesNotThrow(() -> spyProjectTaskDeveloperRepository.saveAndFlush(projectTaskDeveloperEntity));
        // - Tétel törlése
        spyProjectTaskDeveloperRepository.deleteById(projectTaskDeveloperId);
        assertDoesNotThrow(entityManager::flush);
        entityManager.clear();

        //sumSpendTimeByDeveloperId
        assertEquals(0, projectTaskDeveloperRepository.sumSpendTimeByDeveloperId(1L));
        assertEquals(1, projectTaskDeveloperRepository.sumSpendTimeByDeveloperId(2L));
        assertEquals(0, projectTaskDeveloperRepository.sumSpendTimeByDeveloperId(3L));
        assertEquals(0, projectTaskDeveloperRepository.sumSpendTimeByDeveloperId(4L));
        // getDeveloperFullTime
        List<DeveloperEntity> actualDevelopers = developerRepository.findAll().stream().sorted(
            Comparator.comparing(DeveloperEntity::getName)).collect(Collectors.toList());
        try {
            assertEquals(0L, spiedProjectTaskDeveloperService.getDeveloperFullTime(actualDevelopers.get(0)));
            assertEquals(1L, spiedProjectTaskDeveloperService.getDeveloperFullTime(actualDevelopers.get(1)));
            assertEquals(0L, spiedProjectTaskDeveloperService.getDeveloperFullTime(actualDevelopers.get(2)));
            assertEquals(0L, spiedProjectTaskDeveloperService.getDeveloperFullTime(actualDevelopers.get(3)));
        } catch (ExecutionException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    @Transactional
    @Rollback
    void projectTaskDeveloperServiceInsertTest() {
        ProjectTaskDeveloperEntity newProjectTaskDeveloper = new ProjectTaskDeveloperEntity();
        ProjectTaskEntity projectTaskEntity;
        ProjectDeveloperEntity projectDeveloperEntity;
        DeveloperEntity developerEntity;
        // -  projectTaskEntity nincs elmentve
        projectTaskEntity = new ProjectTaskEntity();
        projectTaskEntity.setProjectEntity(Objects.requireNonNull(projectRepository.findAllById(1L)));
        projectDeveloperEntity = projectDeveloperRepository.findAllById(1L);
        newProjectTaskDeveloper.setProjectTaskAndDeveloper(
            projectTaskEntity,
            Objects.requireNonNull(projectDeveloperEntity).getDeveloperEntity()
        );
        assertEquals(
            PorphyrServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_PROJECTTASK_NOT_SAVED.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedProjectTaskDeveloperService.insertNewProjectTaskDeveloper(newProjectTaskDeveloper)
            ).getMessage()
        );
        entityManager.clear();
        // -  projectTaskEntity nincs az adatbázisban
        projectTaskEntity = projectTaskRepository.findAllById(5L);
        projectDeveloperEntity = projectDeveloperRepository.findAllById(1L);
        assertNotNull(projectTaskEntity);
        newProjectTaskDeveloper.setProjectTaskAndDeveloper(projectTaskEntity, Objects.requireNonNull(
            projectDeveloperEntity).getDeveloperEntity());
        projectTaskRepository.delete(projectTaskEntity);
        entityManager.flush();
        assertEquals(
            PorphyrServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_PROJECTTASK_NOT_EXISTS.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedProjectTaskDeveloperService.insertNewProjectTaskDeveloper(newProjectTaskDeveloper)
            ).getMessage()
        );
        entityManager.clear();
        // -  developerEntity nincs elmentve
        projectTaskEntity = projectTaskRepository.findAllById(1L);
        developerEntity = new DeveloperEntity();
        newProjectTaskDeveloper.setProjectTaskAndDeveloper(projectTaskEntity, developerEntity);
        assertEquals(
            PorphyrServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_DEVELOPER_NOT_SAVED.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedProjectTaskDeveloperService.insertNewProjectTaskDeveloper(newProjectTaskDeveloper)
            ).getMessage()
        );
        entityManager.clear();
        // -  developerEntity nincs az adatbázisban
        projectTaskEntity = projectTaskRepository.findAllById(1L);
        developerEntity = developerRepository.findAllById(5L);
        assertNotNull(developerEntity);
        newProjectTaskDeveloper.setProjectTaskAndDeveloper(projectTaskEntity, developerEntity);
        developerRepository.delete(developerEntity);
        entityManager.flush();
        assertEquals(
            PorphyrServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_DEVELOPER_NOT_EXISTS.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedProjectTaskDeveloperService.insertNewProjectTaskDeveloper(newProjectTaskDeveloper)
            ).getMessage()
        );
        entityManager.clear();
        // -  A DeveloperEntity nincs hozzárendelve a ProjectTaskEntity ProjectEntity - hez
        projectTaskEntity = projectTaskRepository.findAllById(4L);
        developerEntity = developerRepository.findAllById(1L);
        newProjectTaskDeveloper.setProjectTaskAndDeveloper(projectTaskEntity, developerEntity);
        assertEquals(
            PorphyrServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_DEVELOPER_NOT_IN_PROJECT.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedProjectTaskDeveloperService.insertNewProjectTaskDeveloper(newProjectTaskDeveloper)
            ).getMessage()
        );
        entityManager.clear();
        // -  Már létezik ilyen összerendelés
        projectTaskEntity = projectTaskRepository.findAllById(1L);
        projectDeveloperEntity = projectDeveloperRepository.findAllById(1L);
        newProjectTaskDeveloper.setProjectTaskAndDeveloper(
            projectTaskEntity,
            Objects.requireNonNull(projectDeveloperEntity).getDeveloperEntity()
        );
        assertEquals(
            PorphyrServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_EXISTING_DATA.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedProjectTaskDeveloperService.insertNewProjectTaskDeveloper(newProjectTaskDeveloper)
            ).getMessage()
        );
        entityManager.clear();
        // - minden adat jó, felvitel és visszaolvasás
        projectTaskEntity = projectTaskRepository.findAllById(1L);
        projectDeveloperEntity = projectDeveloperRepository.findAllById(3L);
        newProjectTaskDeveloper.setProjectTaskAndDeveloper(
            projectTaskEntity,
            Objects.requireNonNull(projectDeveloperEntity).getDeveloperEntity()
        );
        assertDoesNotThrow(() -> spiedProjectTaskDeveloperService.insertNewProjectTaskDeveloper(newProjectTaskDeveloper));
        verify(spyProjectTaskDeveloperRepository, times(1)).saveAndFlush(newProjectTaskDeveloper);
        assertEquals(
            newProjectTaskDeveloper, projectTaskDeveloperRepository.findAllById(newProjectTaskDeveloper.getId())
        );
        entityManager.clear();
        // - Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> PorphyrServiceException.isAllExceptionsThrown(PorphyrServiceException.ExceptionGroups.PROJECTTASKDEVELOPERS_INSERT));
    }

    @Test
    @Transactional
    @Rollback
    void projectTaskDeveloperServiceModifyTest() throws ExecutionException, InterruptedException {
        // -  projectTaskDeveloperEntity nincs elmentve
        ProjectTaskDeveloperEntity newProjectTaskDeveloper = new ProjectTaskDeveloperEntity();
        assertEquals(
            PorphyrServiceException.Exceptions.PROJECTTASKDEVELOPER_MODIFY_NOT_SAVED.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedProjectTaskDeveloperService.modifyProjectTaskDeveloper(newProjectTaskDeveloper)
            ).getMessage()
        );
        entityManager.clear();
        // -  spendTime < 0
        ProjectTaskDeveloperEntity modifyProjectTaskDeveloper = projectTaskDeveloperRepository.findAllById(1L);
        assertNotNull(modifyProjectTaskDeveloper);
        modifyProjectTaskDeveloper.setSpendTime(-1L);
        assertEquals(
            PorphyrServiceException.Exceptions.PROJECTTASKDEVELOPER_MODIFY_TIME_IS_NEGATIVE.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedProjectTaskDeveloperService.modifyProjectTaskDeveloper(modifyProjectTaskDeveloper)
            ).getMessage()
        );
        entityManager.clear();
        // - jó adatokkal
        modifyProjectTaskDeveloper.setSpendTime(1L);
        spiedProjectTaskDeveloperService.modifyProjectTaskDeveloper(modifyProjectTaskDeveloper);
        assertDoesNotThrow(entityManager::flush);
        verify(spyProjectTaskDeveloperRepository, times(1)).saveAndFlush(modifyProjectTaskDeveloper);
        entityManager.clear();
        assertEquals(modifyProjectTaskDeveloper, spiedProjectTaskDeveloperService.getProjectTaskDeveloperById(1L));
        // - Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> PorphyrServiceException.isAllExceptionsThrown(PorphyrServiceException.ExceptionGroups.PROJECTTASKDEVELOPERS_MODIFY));
    }

    @Test
    @Transactional
    @Rollback
    void projectTaskDeveloperServiceDeleteTest() throws ExecutionException, InterruptedException {
        // -  projectTaskDeveloperEntity nincs elmentve
        ProjectTaskDeveloperEntity newProjectTaskDeveloper = new ProjectTaskDeveloperEntity();
        assertEquals(
            PorphyrServiceException.Exceptions.PROJECTTASKDEVELOPER_DELETE_NOT_SAVED.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedProjectTaskDeveloperService.deleteProjectTaskDeveloper(newProjectTaskDeveloper)
            ).getMessage()
        );
        entityManager.clear();
        // -  A projektben eltöltött idő nem 0.
        ProjectTaskDeveloperEntity timeNotZeroPTDEntity = projectTaskDeveloperRepository.findAllById(8L);
        assertNotNull(timeNotZeroPTDEntity);
        assert (timeNotZeroPTDEntity.getSpendTime() > 0);
        assertEquals(
            PorphyrServiceException.Exceptions.PROJECTTASKDEVELOPER_DELETE_TIME_NOT_ZERO.getDescription(),
            assertThrows(
                PorphyrServiceException.class,
                () -> spiedProjectTaskDeveloperService.deleteProjectTaskDeveloper(timeNotZeroPTDEntity)
            ).getMessage()
        );
        entityManager.clear();
        // - Törlés megfelelő adatokkal
        ProjectTaskDeveloperEntity deleteProjectTaskDeveloper = projectTaskDeveloperRepository.findAllById(7L);
        assertNotNull(deleteProjectTaskDeveloper);
        assertEquals(0, deleteProjectTaskDeveloper.getSpendTime());
        spiedProjectTaskDeveloperService.deleteProjectTaskDeveloper(deleteProjectTaskDeveloper);
        entityManager.clear();
        Long deletedEntityId = deleteProjectTaskDeveloper.getId();
        assertNotNull(deletedEntityId);
        verify(spyProjectTaskDeveloperRepository, times(1)).deleteById(deletedEntityId);
        assertNull(spyProjectTaskDeveloperRepository.findAllById(7L));
        // - Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> PorphyrServiceException.isAllExceptionsThrown(PorphyrServiceException.ExceptionGroups.PROJECTTASKDEVELOPERS_DELETE));
    }

    @Test
    @Transactional
    @Rollback
    void projectTaskDeveloperServiceQueriesTest() throws ExecutionException, InterruptedException {
        ProjectTaskEntity projectTaskEntity = projectTaskRepository.findAllById(1L);
        ProjectDeveloperEntity projectDeveloperEntity = projectDeveloperRepository.findAllById(1L);
        assertNotNull(projectTaskEntity);
        assertNotNull(projectDeveloperEntity);
        // -  getProjectTaskDevelopers
        assertEquals(8, spiedProjectTaskDeveloperService.getProjectTaskDevelopers().size());
        // -  getProjectTaskDeveloperById
        assertNotNull(spiedProjectTaskDeveloperService.getProjectTaskDeveloperById(1L));
        // -  getProjectTaskDeveloperByProjectTaskAndProjectDeveloper
        assertNotNull(spiedProjectTaskDeveloperService.getProjectTaskDeveloperByProjectTaskAndProjectDeveloper(
            projectTaskEntity, projectDeveloperEntity
        ));
        // -  getProjectTaskDevelopersByProjectTask
        assertEquals(
            2,
            spiedProjectTaskDeveloperService.getProjectTaskDevelopersByProjectTask(projectTaskEntity).size()
        );
        // -  getProjectTaskDevelopersByProjectDeveloper
        assertEquals(
            1,
            spiedProjectTaskDeveloperService.getProjectTaskDevelopersByProjectDeveloper(projectDeveloperEntity).size()
        );
    }
}
