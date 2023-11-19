package combit.hu.porphyr;

import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskDeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import combit.hu.porphyr.repository.ProjectDeveloperRepository;
import combit.hu.porphyr.repository.ProjectTaskDeveloperRepository;
import combit.hu.porphyr.repository.ProjectTaskRepository;
import combit.hu.porphyr.service.ProjectTaskDeveloperService;
import combit.hu.porphyr.service.ServiceException;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeAll;
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

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("service_test")
class ProjectTaskDeveloperTest {
    @Autowired
    private @NonNull EntityManager entityManager;

    @Autowired
    private @NonNull ProjectTaskDeveloperRepository projectTaskDeveloperRepository;

    @Autowired
    private @NonNull ProjectTaskRepository projectTaskRepository;

    @Autowired
    private @NonNull ProjectDeveloperRepository projectDeveloperRepository;

    private ProjectTaskDeveloperRepository spyProjectTaskDeveloperRepository;
    private ProjectTaskDeveloperService spiedProjectTaskDeveloperService;

    @BeforeAll
    void setupAll() {
        spiedProjectTaskDeveloperService = new ProjectTaskDeveloperService(
            entityManager, projectTaskDeveloperRepository, projectTaskRepository, projectDeveloperRepository
        );
        spyProjectTaskDeveloperRepository = Mockito.mock(
            ProjectTaskDeveloperRepository.class, AdditionalAnswers.delegatesTo(projectTaskDeveloperRepository)
        );
        spiedProjectTaskDeveloperService.setProjectTaskDeveloperRepository(spyProjectTaskDeveloperRepository);

        spiedProjectTaskDeveloperService.setEntityManager(entityManager);
        spiedProjectTaskDeveloperService.setProjectTaskRepository(projectTaskRepository);
        spiedProjectTaskDeveloperService.setProjectDeveloperRepository(projectDeveloperRepository);

        ServiceException.initExceptionsCounter();
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
        projectTaskDeveloperEntity.setProjectTaskAndProjectDeveloper(projectTaskEntity, projectDeveloperEntity);
        assertThrows(
            DataIntegrityViolationException.class,
            () -> spyProjectTaskDeveloperRepository.save(projectTaskDeveloperEntity)
        );
        entityManager.clear();
        // - projectDeveloper.id = null (constraint ellenőrzés)
        projectTaskEntity = projectTaskRepository.findAllById(1L);
        projectDeveloperEntity = new ProjectDeveloperEntity();
        projectTaskDeveloperEntity.setProjectTaskAndProjectDeveloper(projectTaskEntity, projectDeveloperEntity);
        assertThrows(
            DataIntegrityViolationException.class,
            () -> spyProjectTaskDeveloperRepository.save(projectTaskDeveloperEntity)
        );
        entityManager.clear();
        // - Már létező tétel felvétele
        projectTaskEntity = projectTaskRepository.findAllById(1L);
        projectDeveloperEntity = projectDeveloperRepository.findAllById(1L);
        projectTaskDeveloperEntity.setProjectTaskAndProjectDeveloper(projectTaskEntity, projectDeveloperEntity);
        assertThrows(
            DataIntegrityViolationException.class,
            () -> spyProjectTaskDeveloperRepository.save(projectTaskDeveloperEntity)
        );
        entityManager.clear();
        // - Nem létező tétel felvétele
        projectTaskEntity = projectTaskRepository.findAllById(1L);
        projectDeveloperEntity = projectDeveloperRepository.findAllById(3L);
        projectTaskDeveloperEntity.setProjectTaskAndProjectDeveloper(projectTaskEntity, projectDeveloperEntity);
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
        assertDoesNotThrow(() -> entityManager.flush());
        entityManager.clear();
    }

    @Test
    @Transactional
    @Rollback
    void projectTaskDeveloperServiceInsertTest() {
        ProjectTaskDeveloperEntity newProjectTaskDeveloper = new ProjectTaskDeveloperEntity();
        ProjectTaskEntity projectTaskEntity;
        ProjectDeveloperEntity projectDeveloperEntity;
        // -  projectTaskEntity nincs elmentve
        projectTaskEntity = new ProjectTaskEntity();
        projectDeveloperEntity = projectDeveloperRepository.findAllById(1L);
        newProjectTaskDeveloper.setProjectTaskAndProjectDeveloper(projectTaskEntity, projectDeveloperEntity);
        assertEquals(
            ServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_PROJECTTASK_NOT_SAVED.getDescription(),
            assertThrows(
                ServiceException.class,
                () -> spiedProjectTaskDeveloperService.insertNewProjectTaskDeveloper(newProjectTaskDeveloper)
            ).getMessage()
        );
        entityManager.clear();
        // -  projectTaskEntity nincs az adatbázisban
        projectTaskEntity = projectTaskRepository.findAllById(5L);
        assertNotNull(projectTaskEntity);
        newProjectTaskDeveloper.setProjectTaskAndProjectDeveloper(projectTaskEntity, projectDeveloperEntity);
        projectTaskRepository.delete(projectTaskEntity);
        entityManager.flush();
        assertEquals(
            ServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_PROJECTTASK_NOT_EXISTS.getDescription(),
            assertThrows(
                ServiceException.class,
                () -> spiedProjectTaskDeveloperService.insertNewProjectTaskDeveloper(newProjectTaskDeveloper)
            ).getMessage()
        );
        entityManager.clear();
        // -  projectDeveloperEntity nincs elmentve
        projectTaskEntity = projectTaskRepository.findAllById(1L);
        projectDeveloperEntity = new ProjectDeveloperEntity();
        newProjectTaskDeveloper.setProjectTaskAndProjectDeveloper(projectTaskEntity, projectDeveloperEntity);
        assertEquals(
            ServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_PROJECTDEVELOPER_NOT_SAVED.getDescription(),
            assertThrows(
                ServiceException.class,
                () -> spiedProjectTaskDeveloperService.insertNewProjectTaskDeveloper(newProjectTaskDeveloper)
            ).getMessage()
        );
        entityManager.clear();
        // -  projectDeveloperEntity nincs az adatbázisban
        projectTaskEntity = projectTaskRepository.findAllById(1L);
        projectDeveloperEntity = projectDeveloperRepository.findAllById(7L);
        assertNotNull(projectDeveloperEntity);
        newProjectTaskDeveloper.setProjectTaskAndProjectDeveloper(projectTaskEntity, projectDeveloperEntity);
        projectDeveloperRepository.delete(projectDeveloperEntity);
        entityManager.flush();
        assertEquals(
            ServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_PROJECTDEVELOPER_NOT_EXISTS.getDescription(),
            assertThrows(
                ServiceException.class,
                () -> spiedProjectTaskDeveloperService.insertNewProjectTaskDeveloper(newProjectTaskDeveloper)
            ).getMessage()
        );
        entityManager.clear();
        // -  A projectTaskEntity és a projectDeveloperEntity más// - más projekthez tartozik
        projectTaskEntity = projectTaskRepository.findAllById(4L);
        projectDeveloperEntity = projectDeveloperRepository.findAllById(1L);
        newProjectTaskDeveloper.setProjectTaskAndProjectDeveloper(projectTaskEntity, projectDeveloperEntity);
        assertEquals(
            ServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_TASK_OR_DEVELOPER_NOT_IN_PROJECT.getDescription(),
            assertThrows(
                ServiceException.class,
                () -> spiedProjectTaskDeveloperService.insertNewProjectTaskDeveloper(newProjectTaskDeveloper)
            ).getMessage()
        );
        entityManager.clear();
        // -  Már létezik ilyen összerendelés
        projectTaskEntity = projectTaskRepository.findAllById(1L);
        projectDeveloperEntity = projectDeveloperRepository.findAllById(1L);
        newProjectTaskDeveloper.setProjectTaskAndProjectDeveloper(projectTaskEntity, projectDeveloperEntity);
        assertEquals(
            ServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_EXISTING_DATA.getDescription(),
            assertThrows(
                ServiceException.class,
                () -> spiedProjectTaskDeveloperService.insertNewProjectTaskDeveloper(newProjectTaskDeveloper)
            ).getMessage()
        );
        entityManager.clear();
        // - minden adat jó, felvitel és visszaolvasás
        projectTaskEntity = projectTaskRepository.findAllById(1L);
        projectDeveloperEntity = projectDeveloperRepository.findAllById(3L);
        newProjectTaskDeveloper.setProjectTaskAndProjectDeveloper(projectTaskEntity, projectDeveloperEntity);
        assertDoesNotThrow(() -> spiedProjectTaskDeveloperService.insertNewProjectTaskDeveloper(newProjectTaskDeveloper));
        verify(spyProjectTaskDeveloperRepository, times(1)).saveAndFlush(newProjectTaskDeveloper);
        assertEquals(
            newProjectTaskDeveloper, projectTaskDeveloperRepository.findAllById(newProjectTaskDeveloper.getId())
        );
        entityManager.clear();
        // - Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> ServiceException.isAllExceptionsThrown(ServiceException.ExceptionGroups.PROJECTTASKDEVELOPERS_INSERT));
    }

    @Test
    @Transactional
    @Rollback
    void projectTaskDeveloperServiceModifyTest() throws ExecutionException, InterruptedException {
        // -  projectTaskDeveloperEntity nincs elmentve
        ProjectTaskDeveloperEntity newProjectTaskDeveloper = new ProjectTaskDeveloperEntity();
        assertEquals(
            ServiceException.Exceptions.PROJECTTASKDEVELOPER_MODIFY_NOT_SAVED.getDescription(),
            assertThrows(
                ServiceException.class,
                () -> spiedProjectTaskDeveloperService.modifyProjectTaskDeveloper(newProjectTaskDeveloper)
            ).getMessage()
        );
        entityManager.clear();
        // -  spendTime < 0
        ProjectTaskDeveloperEntity modifyProjectTaskDeveloper = projectTaskDeveloperRepository.findAllById(1L);
        assertNotNull(modifyProjectTaskDeveloper);
        modifyProjectTaskDeveloper.setSpendTime(-1L);
        assertEquals(
            ServiceException.Exceptions.PROJECTTASKDEVELOPER_MODIFY_TIME_IS_NEGATIVE.getDescription(),
            assertThrows(
                ServiceException.class,
                () -> spiedProjectTaskDeveloperService.modifyProjectTaskDeveloper(modifyProjectTaskDeveloper)
            ).getMessage()
        );
        entityManager.clear();
        // - jó adatokkal
        modifyProjectTaskDeveloper.setSpendTime(1L);
        spiedProjectTaskDeveloperService.modifyProjectTaskDeveloper(modifyProjectTaskDeveloper);
        assertDoesNotThrow(() -> entityManager.flush());
        verify(spyProjectTaskDeveloperRepository, times(1)).saveAndFlush(modifyProjectTaskDeveloper);
        entityManager.clear();
        assertEquals(modifyProjectTaskDeveloper, spiedProjectTaskDeveloperService.getProjectTaskDeveloperById(1L));
        // - Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> ServiceException.isAllExceptionsThrown(ServiceException.ExceptionGroups.PROJECTTASKDEVELOPERS_MODIFY));
    }

    @Test
    @Transactional
    @Rollback
    void projectTaskDeveloperServiceDeleteTest() throws ExecutionException, InterruptedException {
        // -  projectTaskDeveloperEntity nincs elmentve
        ProjectTaskDeveloperEntity newProjectTaskDeveloper = new ProjectTaskDeveloperEntity();
        assertEquals(
            ServiceException.Exceptions.PROJECTTASKDEVELOPER_DELETE_NOT_SAVED.getDescription(),
            assertThrows(
                ServiceException.class,
                () -> spiedProjectTaskDeveloperService.deleteProjectTaskDeveloper(newProjectTaskDeveloper)
            ).getMessage()
        );
        entityManager.clear();
        // -  A projektben eltöltött idő nem 0.
        ProjectTaskDeveloperEntity timeNotZeroPTDEntity = projectTaskDeveloperRepository.findAllById(8L);
        assertNotNull(timeNotZeroPTDEntity);
        assert (timeNotZeroPTDEntity.getSpendTime() > 0);
        assertEquals(
            ServiceException.Exceptions.PROJECTTASKDEVELOPER_DELETE_TIME_NOT_ZERO.getDescription(),
            assertThrows(
                ServiceException.class,
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
        assertDoesNotThrow(() -> ServiceException.isAllExceptionsThrown(ServiceException.ExceptionGroups.PROJECTTASKDEVELOPERS_DELETE));
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
