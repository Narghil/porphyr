package combit.hu.porphyr;

import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import combit.hu.porphyr.repository.ProjectRepository;
import combit.hu.porphyr.repository.ProjectTaskRepository;
import combit.hu.porphyr.service.ProjectTaskService;
import combit.hu.porphyr.service.ServiceException;
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
import javax.persistence.PersistenceException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("service_test")
class ProjectTaskTests {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private ProjectTaskRepository spyProjectTaskRepository;

    final private ProjectTaskService spiedProjectTaskService = new ProjectTaskService();

    @BeforeAll
    void setupAll() {
        spyProjectTaskRepository = Mockito.mock(
            ProjectTaskRepository.class, AdditionalAnswers.delegatesTo(projectTaskRepository)
        );

        spiedProjectTaskService.setProjectTaskRepository(spyProjectTaskRepository);
        spiedProjectTaskService.setProjectRepository(projectRepository);
        spiedProjectTaskService.setEntityManager(entityManager);

        ServiceException.initExceptionsCounter();
    }

    @BeforeEach
    void setupEach() {
        entityManager.clear();
        Mockito.clearInvocations(spyProjectTaskRepository);
    }

    @Test
    @Transactional
    @Rollback
    void projectTaskRepositoryTest() {
        ProjectEntity projectEntity = new ProjectEntity();
        final ProjectTaskEntity testProjectTask;
        ProjectTaskEntity actualProjectTask;
        // ------------------ ProjectTask felvétele ----------------
        // -- projectID = null (constraint ellenőrzése)
        testProjectTask = new ProjectTaskEntity(projectEntity, "New ProjectTask", "");
        assertThrows(DataIntegrityViolationException.class, () -> spyProjectTaskRepository.save(testProjectTask));
        // -- projectID + name már létezik (constraint ellenőrzése)
        entityManager.clear();
        projectEntity = projectRepository.findAllById(1L);
        assertNotNull(projectEntity);
        testProjectTask.setProjectEntity(projectEntity);
        testProjectTask.setName("1. projekt 1. feladat");
        assertThrows(DataIntegrityViolationException.class, () -> spyProjectTaskRepository.save(testProjectTask));
        entityManager.clear();
        // -- Helyes adatok és visszaolvasás
        testProjectTask.setName("New ProjectTask");
        assertDoesNotThrow(() -> spyProjectTaskRepository.saveAndFlush(testProjectTask));
        assertNotNull(projectEntity);
        actualProjectTask = spyProjectTaskRepository.findAllByProjectEntityAndName(projectEntity, "New ProjectTask");
        assertEquals( testProjectTask , actualProjectTask);
        // ------------------ ProjectTask módosítása ---------------
        // -- Helyes adatok és visszaolvasás
        testProjectTask.setName("Modified ProjectTask");
        assertDoesNotThrow(() -> spyProjectTaskRepository.saveAndFlush(testProjectTask));
        actualProjectTask = spyProjectTaskRepository.findAllByProjectEntityAndName(projectEntity, "New ProjectTask");
        assertNull(actualProjectTask);
        actualProjectTask = spyProjectTaskRepository.findAllByProjectEntityAndName(
            projectEntity,
            "Modified ProjectTask"
        );
        assertEquals( testProjectTask , actualProjectTask);
        // ------------------- ProjectTask törlése -----------------
        // -- Helyes adatok és visszaolvasás
        assertDoesNotThrow(() -> spyProjectTaskRepository.delete(testProjectTask));
        entityManager.flush();
        actualProjectTask = spyProjectTaskRepository.findAllByProjectEntityAndName(
            projectEntity,
            "Modified ProjectTask"
        );
        assertNull(actualProjectTask);
        entityManager.clear();
        // -- Még van hozzárendelt fejlesztő (foreign key ellenőrzése)
        actualProjectTask = spyProjectTaskRepository.findAllByProjectEntityAndName(
            projectEntity,
            "1. projekt 1. feladat"
        );
        assertNotNull(actualProjectTask);
        spyProjectTaskRepository.delete(actualProjectTask);
        assertThrows(PersistenceException.class, () -> entityManager.flush());
        entityManager.clear();
    }

    @Test
    @Transactional
    @Rollback
    void projectTaskServiceInsertTest() {
        final ProjectTaskEntity newProjectTask;
        ProjectTaskEntity actualProjectTask;
        //---------------- insertNewProjectTask -----------------------------
        ProjectEntity projectEntity = new ProjectEntity();
        newProjectTask = new ProjectTaskEntity(projectEntity, "New ProjectTask", "");
        // - A project entity nincs elmentve.
        newProjectTask.setProjectEntity(projectEntity);
        assertEquals(
            ServiceException.Exceptions.PROJECTTASK_INSERT_PROJECT_NOT_SAVED.getDescription(),
            assertThrows(ServiceException.class, () -> spiedProjectTaskService.insertNewProjectTask(newProjectTask)
            ).getMessage()
        );
        // - A project entity  nincs az adatbázisban
        ProjectEntity deletedProjectEntity = new ProjectEntity("Törlendő projekt", "");
        projectRepository.saveAndFlush(deletedProjectEntity);
        projectRepository.delete(deletedProjectEntity);
        entityManager.flush();
        newProjectTask.setProjectEntity(deletedProjectEntity);
        assertEquals(
            ServiceException.Exceptions.PROJECTTASK_INSERT_PROJECT_NOT_EXISTS.getDescription(),
            assertThrows(ServiceException.class, () -> spiedProjectTaskService.insertNewProjectTask(newProjectTask)
            ).getMessage()
        );
        // - A task neve üres
        entityManager.detach(projectEntity);
        projectEntity = projectRepository.findAllById(1L);
        assertNotNull(projectEntity);
        newProjectTask.setProjectEntity(projectEntity);
        newProjectTask.setName("");
        assertEquals(
            ServiceException.Exceptions.PROJECTTASK_INSERT_EMPTY_NAME.getDescription(),
            assertThrows(ServiceException.class, () -> spiedProjectTaskService.insertNewProjectTask(newProjectTask)
            ).getMessage()
        );
        // - Ilyen nevű task már van a projekten belül!
        newProjectTask.setName("1. projekt 1. feladat");
        assertEquals(
            ServiceException.Exceptions.PROJECTTASK_INSERT_SAME_PROJECT_AND_NAME.getDescription(),
            assertThrows(ServiceException.class, () -> spiedProjectTaskService.insertNewProjectTask(newProjectTask)
            ).getMessage()
        );
        // - Minden ok.
        projectEntity = projectRepository.findAllById(1L);
        assertNotNull(projectEntity);
        newProjectTask.setProjectEntity(projectEntity);
        newProjectTask.setName("1. project - új feladat");
        assertDoesNotThrow(() -> spiedProjectTaskService.insertNewProjectTask(newProjectTask));
        verify(spyProjectTaskRepository, times(1)).saveAndFlush(newProjectTask);
        entityManager.clear();
        assertNotNull(newProjectTask.getId());
        actualProjectTask = spiedProjectTaskService.getProjectTaskById(newProjectTask.getId());
        assertEquals(newProjectTask, actualProjectTask);
        //Mindent ellenőriztünk
        assertDoesNotThrow(() -> ServiceException.isAllExceptionsThrown(ServiceException.ExceptionGroups.PROJECTTASKS_INSERT));
    }

    @Test
    @Transactional
    @Rollback
    void projectTaskServiceModifyTest() {
        ProjectEntity projectEntity = projectRepository.findAllById(1L);
        final ProjectTaskEntity projectTaskWithNoId;
        final ProjectTaskEntity projectTaskWithAnyCases;
        final ProjectTaskEntity projectTaskWithAnyNames;
        ProjectTaskEntity actualProjectTask;
        // ---------------- modifyProjectTask ------------------------------
        // - A task ID-je üres
        assertNotNull(projectEntity);
        projectTaskWithNoId = new ProjectTaskEntity(projectEntity, "Modified ProjectTask", "");
        assertEquals(
            ServiceException.Exceptions.PROJECTTASK_MODIFY_NOT_SAVED.getDescription(),
            assertThrows(ServiceException.class, () -> spiedProjectTaskService.modifyProjectTask(projectTaskWithNoId)
            ).getMessage()
        );
        // - A project nincs elmentve
        projectTaskWithAnyCases = spiedProjectTaskService.getProjectTaskById(1L);
        assertNotNull(projectTaskWithAnyCases);
        projectTaskWithAnyCases.setProjectEntity(new ProjectEntity("Not Saved project", ""));
        assertEquals(
            ServiceException.Exceptions.PROJECTTASK_MODIFY_PROJECT_NOT_SAVED.getDescription(),
            assertThrows(ServiceException.class, () -> spiedProjectTaskService.modifyProjectTask(projectTaskWithAnyCases)
            ).getMessage()
        );
        // - nem létező project-re mutat
        ProjectEntity deletedProjectEntity = new ProjectEntity("Törlendő projekt", "");
        projectRepository.saveAndFlush(deletedProjectEntity);
        projectRepository.delete(deletedProjectEntity);
        entityManager.flush();
        projectTaskWithAnyCases.setProjectEntity(deletedProjectEntity);
        assertEquals(
            ServiceException.Exceptions.PROJECTTASK_MODIFY_PROJECT_NOT_EXISTS.getDescription(),
            assertThrows(ServiceException.class, () -> spiedProjectTaskService.modifyProjectTask(projectTaskWithAnyCases)
            ).getMessage()
        );
        // - Üres név
        projectTaskWithAnyNames = spiedProjectTaskService.getProjectTaskById(2L); //"1. projekt 2. feladat"
        assertNotNull(projectTaskWithAnyNames);
        projectTaskWithAnyNames.setName("");
        assertEquals(
            ServiceException.Exceptions.PROJECTTASK_MODIFY_EMPTY_NAME.getDescription(),
            assertThrows(ServiceException.class, () -> spiedProjectTaskService.modifyProjectTask(projectTaskWithAnyNames)
            ).getMessage()
        );
        // - Ilyen nevű task már van a projektben
        projectTaskWithAnyNames.setName("1. projekt 1. feladat");
        assertEquals(
            ServiceException.Exceptions.PROJECTTASK_MODIFY_SAME_PROJECT_AND_NAME.getDescription(),
            assertThrows(ServiceException.class, () -> spiedProjectTaskService.modifyProjectTask(projectTaskWithAnyNames)
            ).getMessage()
        );
        // - Helyes adatokkal
        projectTaskWithAnyNames.setName("1. projekt második feladat");
        assertDoesNotThrow(() -> spiedProjectTaskService.modifyProjectTask(projectTaskWithAnyNames));
        verify(spyProjectTaskRepository, times(1)).saveAndFlush(projectTaskWithAnyNames);
        entityManager.clear();
        Long projectTaskId = projectTaskWithAnyNames.getId();
        assertNotNull(projectTaskId);
        actualProjectTask = spiedProjectTaskService.getProjectTaskById(projectTaskId);
        assertEquals(projectTaskWithAnyNames, actualProjectTask);
        //
        assertDoesNotThrow(() -> ServiceException.isAllExceptionsThrown(ServiceException.ExceptionGroups.PROJECTTASKS_MODIFY));
    }

    @Test
    @Transactional
    @Rollback
    void projectTaskServiceDeleteTest() {
        final ProjectTaskEntity projectTaskWithNoId;
        final ProjectTaskEntity projectTaskWithDeveloper;
        final ProjectTaskEntity projectTaskWithNoDeveloper;
        ProjectTaskEntity actualProjectTask;
        // ----------------- deleteProjectTask ----------------------------
        // - A task ID-je üres
        ProjectEntity projectEntity = projectRepository.findAllById(1L);
        assertNotNull(projectEntity);
        projectTaskWithNoId = new ProjectTaskEntity(projectEntity, "Törlendő feladat", "");
        assertEquals(
            ServiceException.Exceptions.PROJECTTASK_DELETE_NOT_SAVED.getDescription(),
            assertThrows(ServiceException.class, () -> spiedProjectTaskService.deleteProjectTask(projectTaskWithNoId)
            ).getMessage()
        );
        // - A task-hoz még van hozzárendelve fejlesztő
        projectTaskWithDeveloper = projectTaskRepository.findAllById(1L);
        assertNotNull(projectTaskWithDeveloper);
        assertEquals(
            ServiceException.Exceptions.PROJECTTASK_DELETE_DEVELOPERS_ASSIGNED.getDescription(),
            assertThrows(ServiceException.class, () -> spiedProjectTaskService.deleteProjectTask(projectTaskWithDeveloper)
            ).getMessage()

        );
        // - Minden ok.
        projectEntity = projectRepository.findAllById(4L);
        assertNotNull(projectEntity);
        projectTaskWithNoDeveloper = projectTaskRepository.findAllByProjectEntityAndName(
            projectEntity, "Projekt feladattal - feladat"
        );
        assertNotNull(projectTaskWithNoDeveloper);
        assertDoesNotThrow(() -> spiedProjectTaskService.deleteProjectTask(projectTaskWithNoDeveloper));
        Long projectTaskId = projectTaskWithNoDeveloper.getId();
        assertNotNull(projectTaskId);
        actualProjectTask = spiedProjectTaskService.getProjectTaskById(projectTaskId);
        assertNull(actualProjectTask);
        verify(spyProjectTaskRepository, times(1)).deleteById(projectTaskId);
        // - Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> ServiceException.isAllExceptionsThrown(ServiceException.ExceptionGroups.PROJECTTASKS_DELETE));
    }

    @Test
    @Transactional
    @Rollback
    void projectTaskServiceQueriesTest() {
        //getProjectTasks
        assertEquals(5, spiedProjectTaskService.getProjectTasks().size());
        //getProjectTaskById
        assertNotNull(spiedProjectTaskService.getProjectTaskById(1L));
        //getProjectTaskByProjectEntityAndName
        ProjectEntity projectEntity = projectRepository.findAllById(1L);
        assertNotNull(projectEntity);
        assertNotNull(spiedProjectTaskService.getProjectTaskByProjectEntityAndName(
            projectEntity,
            "1. projekt 1. feladat"
        ));
    }
}
