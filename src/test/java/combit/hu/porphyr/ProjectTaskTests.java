package combit.hu.porphyr;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTaskDeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import combit.hu.porphyr.repository.ProjectRepository;
import combit.hu.porphyr.repository.ProjectTaskRepository;
import combit.hu.porphyr.service.ProjectTaskService;
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
import javax.persistence.PersistenceException;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class ProjectTaskTests {
    @Autowired
    private @NonNull EntityManager entityManager;

    @Autowired
    private @NonNull ProjectTaskRepository projectTaskRepository;

    @Autowired
    private @NonNull ProjectRepository projectRepository;

    private ProjectTaskRepository spyProjectTaskRepository;
    private ProjectTaskService spiedProjectTaskService;

    @BeforeAll
    void setupAll() {
        spiedProjectTaskService = new ProjectTaskService(
            entityManager, projectTaskRepository, projectRepository
        );
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
    void projectTaskEntityQueriesTest() {
        //GetProjectEntity
        assertEquals(
            "1. projekt",
            Objects.requireNonNull(projectTaskRepository.findAllById(1L)).getProjectEntity().getName()
        );
        assertEquals(
            "1. projekt",
            Objects.requireNonNull(projectTaskRepository.findAllById(2L)).getProjectEntity().getName()
        );
        assertEquals(
            "2. projekt",
            Objects.requireNonNull(projectTaskRepository.findAllById(3L)).getProjectEntity().getName()
        );
        assertEquals(
            "2. projekt",
            Objects.requireNonNull(projectTaskRepository.findAllById(4L)).getProjectEntity().getName()
        );
        assertEquals("Projekt feladattal", Objects.requireNonNull(projectTaskRepository.findAllById(5L))
            .getProjectEntity().getName());
        //GetProjectTaskDevelopers
        assertArrayEquals(
            new String[]{"1. fejlesztő", "2. fejlesztő"},
            Objects.requireNonNull(projectTaskRepository.findAllById(1L)).getProjectTaskDevelopers().stream()
                .map(ProjectTaskDeveloperEntity::getProjectDeveloperEntity)
                .map(ProjectDeveloperEntity::getDeveloperEntity)
                .map(DeveloperEntity::getName).toArray(String[]::new)
        );
        assertArrayEquals(
            new String[]{"2. fejlesztő", "3. fejlesztő"},
            Objects.requireNonNull(projectTaskRepository.findAllById(2L)).getProjectTaskDevelopers().stream()
                .map(ProjectTaskDeveloperEntity::getProjectDeveloperEntity)
                .map(ProjectDeveloperEntity::getDeveloperEntity)
                .map(DeveloperEntity::getName).toArray(String[]::new)
        );
        assertArrayEquals(
            new String[]{"2. fejlesztő", "3. fejlesztő"},
            Objects.requireNonNull(projectTaskRepository.findAllById(3L)).getProjectTaskDevelopers().stream()
                .map(ProjectTaskDeveloperEntity::getProjectDeveloperEntity)
                .map(ProjectDeveloperEntity::getDeveloperEntity)
                .map(DeveloperEntity::getName).toArray(String[]::new)
        );
        assertArrayEquals(
            new String[]{"3. fejlesztő", "4. fejlesztő"},
            Objects.requireNonNull(projectTaskRepository.findAllById(4L)).getProjectTaskDevelopers().stream()
                .map(ProjectTaskDeveloperEntity::getProjectDeveloperEntity)
                .map(ProjectDeveloperEntity::getDeveloperEntity)
                .map(DeveloperEntity::getName).toArray(String[]::new)
        );
        assertArrayEquals(
            new String[]{},
            Objects.requireNonNull(projectTaskRepository.findAllById(5L)).getProjectTaskDevelopers().stream()
                .map(ProjectTaskDeveloperEntity::getProjectDeveloperEntity)
                .map(ProjectDeveloperEntity::getDeveloperEntity)
                .map(DeveloperEntity::getName).toArray(String[]::new)
        );
    }

    @Test
    @Transactional
    @Rollback
    void projectTaskRepositoryQueriesTest() {
        //findAll
        assertArrayEquals(
            new String[]{
                "1. projekt 1. feladat", "1. projekt 2. feladat",
                "2. projekt 1. feladat", "2. projekt 2. feladat",
                "Projekt feladattal - feladat"
            },
            projectTaskRepository.findAll().stream().map(ProjectTaskEntity::getName).toArray(String[]::new)
        );
        //findAllByProjectEntity
        @NonNull ProjectEntity projectEntity = Objects.requireNonNull(projectRepository.findAllById(1L));
        assertArrayEquals(
            new String[]{"1. projekt 1. feladat", "1. projekt 2. feladat"},
            projectTaskRepository.findAllByProjectEntity(projectEntity)
                .stream()
                .map(ProjectTaskEntity::getName)
                .toArray(String[]::new)
        );
        projectEntity = Objects.requireNonNull(projectRepository.findAllById(2L));
        assertArrayEquals(
            new String[]{"2. projekt 1. feladat", "2. projekt 2. feladat"},
            projectTaskRepository.findAllByProjectEntity(projectEntity)
                .stream()
                .map(ProjectTaskEntity::getName)
                .toArray(String[]::new)
        );
        projectEntity = Objects.requireNonNull(projectRepository.findAllById(3L));
        assertArrayEquals(
            new String[]{},
            projectTaskRepository.findAllByProjectEntity(projectEntity)
                .stream()
                .map(ProjectTaskEntity::getName)
                .toArray(String[]::new)
        );
        projectEntity = Objects.requireNonNull(projectRepository.findAllById(4L));
        assertArrayEquals(
            new String[]{"Projekt feladattal - feladat"},
            projectTaskRepository.findAllByProjectEntity(projectEntity)
                .stream()
                .map(ProjectTaskEntity::getName)
                .toArray(String[]::new)
        );
        //findAllById
        // --assertEquals("1. projekt 1. feladat", Objects.requireNonNull(projectTaskRepository.findAllById(1L)).getName());
        // --assertEquals("1. projekt 2. feladat", Objects.requireNonNull(projectTaskRepository.findAllById(2L)).getName());
        assertEquals("2. projekt 1. feladat", Objects.requireNonNull(projectTaskRepository.findAllById(3L)).getName());
        // --assertEquals("2. projekt 2. feladat", Objects.requireNonNull(projectTaskRepository.findAllById(4L)).getName());
        assertEquals(
            "Projekt feladattal - feladat",
            Objects.requireNonNull(projectTaskRepository.findAllById(5L)).getName()
        );
        //findAllByProjectEntityAndName
        projectEntity = Objects.requireNonNull(projectRepository.findAllByName("1. projekt")).get(0);
        assertEquals(
            "1. projekt 1. feladat",
            Objects.requireNonNull(projectTaskRepository.findAllByProjectEntityAndName(projectEntity,
                "1. projekt 1. feladat")).getName()
        );
        assertEquals(
            "1. projekt 2. feladat",
            Objects.requireNonNull(projectTaskRepository.findAllByProjectEntityAndName(projectEntity,
                "1. projekt 2. feladat")).getName()
        );
        projectEntity = Objects.requireNonNull(projectRepository.findAllByName("2. projekt")).get(0);
        assertEquals(
            "2. projekt 1. feladat",
            Objects.requireNonNull(projectTaskRepository.findAllByProjectEntityAndName(projectEntity,
                "2. projekt 1. feladat")).getName()
        );
        assertEquals(
            "2. projekt 2. feladat",
            Objects.requireNonNull(projectTaskRepository.findAllByProjectEntityAndName(projectEntity,
                "2. projekt 2. feladat")).getName()
        );
        projectEntity = Objects.requireNonNull(projectRepository.findAllByName("Projekt feladattal")).get(0);
        assertEquals(
            "Projekt feladattal - feladat",
            Objects.requireNonNull(projectTaskRepository.findAllByProjectEntityAndName(projectEntity,
                "Projekt feladattal - feladat")).getName()
        );
        assertNull(projectTaskRepository.findAllByProjectEntityAndName(projectEntity, "1. projekt 1. feladat"));
        //findAllByProjectEntityAndNameAndIdNot
        projectEntity = Objects.requireNonNull(projectRepository.findAllByName("1. projekt")).get(0);
        assertNull( projectTaskRepository.findAllByProjectEntityAndNameAndIdNot(projectEntity,"1. projekt 1. feladat", 1L));
        assertNull( projectTaskRepository.findAllByProjectEntityAndNameAndIdNot(projectEntity,"1. projekt 2. feladat", 2L));
        assertEquals( 1L, Objects.requireNonNull(projectTaskRepository.findAllByProjectEntityAndNameAndIdNot(
            projectEntity,
            "1. projekt 1. feladat",
            0L
        )).getId());
        assertEquals( 2L, Objects.requireNonNull(projectTaskRepository.findAllByProjectEntityAndNameAndIdNot(
            projectEntity,
            "1. projekt 2. feladat",
            0L
        )).getId());
        projectEntity = Objects.requireNonNull(projectRepository.findAllByName("2. projekt")).get(0);
        assertNull( projectTaskRepository.findAllByProjectEntityAndNameAndIdNot(projectEntity,"1. projekt 1. feladat", 3L));
        assertNull( projectTaskRepository.findAllByProjectEntityAndNameAndIdNot(projectEntity,"2. projekt 2. feladat", 4L));
        assertEquals( 3L, Objects.requireNonNull(projectTaskRepository.findAllByProjectEntityAndNameAndIdNot(
            projectEntity,
            "2. projekt 1. feladat",
            0L
        )).getId());
        assertEquals( 4L, Objects.requireNonNull(projectTaskRepository.findAllByProjectEntityAndNameAndIdNot(
            projectEntity,
            "2. projekt 2. feladat",
            0L
        )).getId());
        projectEntity = Objects.requireNonNull(projectRepository.findAllByName("Projekt feladattal")).get(0);
        assertNull( projectTaskRepository.findAllByProjectEntityAndNameAndIdNot(projectEntity,"Projekt feladattal - feladat", 5L));
        assertEquals( 5L, Objects.requireNonNull(projectTaskRepository.findAllByProjectEntityAndNameAndIdNot(
            projectEntity,
            "Projekt feladattal - feladat",
            0L
        )).getId());
    }

    @Test
    @Transactional
    @Rollback
    void projectTaskServiceQueriesTest() throws ExecutionException, InterruptedException {
        //getProjectTasks
        assertArrayEquals(
            new String[]{
                "1. projekt 1. feladat", "1. projekt 2. feladat",
                "2. projekt 1. feladat", "2. projekt 2. feladat",
                "Projekt feladattal - feladat"
            },
            spiedProjectTaskService.getProjectTasks().stream().map( ProjectTaskEntity::getName).toArray(String[]::new)
        );
        //getProjectTaskById
        assertNotNull(spiedProjectTaskService.getProjectTaskById(1L));
        //getProjectTaskByProjectEntityAndName
        ProjectEntity projectEntity = projectRepository.findAllById(1L);
        assertNotNull(projectEntity);
        assertNotNull(spiedProjectTaskService.getProjectTaskByProjectEntityAndName(
            projectEntity,
            "1. projekt 1. feladat"
        ));
        //getProjectTasksByProjectId
        assertEquals(2, spiedProjectTaskService.getProjectTasksByProjectEntity(projectEntity).size());
        projectEntity = projectRepository.findAllById(2L);
        assertNotNull(projectEntity);
        assertEquals(2, spiedProjectTaskService.getProjectTasksByProjectEntity(projectEntity).size());
        projectEntity = projectRepository.findAllById(3L);
        assertNotNull(projectEntity);
        assertEquals(0, spiedProjectTaskService.getProjectTasksByProjectEntity(projectEntity).size());
        projectEntity = projectRepository.findAllById(4L);
        assertNotNull(projectEntity);
        assertEquals(1, spiedProjectTaskService.getProjectTasksByProjectEntity(projectEntity).size());
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
        assertEquals(testProjectTask, actualProjectTask);
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
        assertEquals(testProjectTask, actualProjectTask);
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
    void projectTaskServiceInsertTest() throws ExecutionException, InterruptedException {
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
    void projectTaskServiceModifyTest() throws ExecutionException, InterruptedException {
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
            assertThrows(
                ServiceException.class,
                () -> spiedProjectTaskService.modifyProjectTask(projectTaskWithAnyCases)
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
            assertThrows(
                ServiceException.class,
                () -> spiedProjectTaskService.modifyProjectTask(projectTaskWithAnyCases)
            ).getMessage()
        );
        // - Üres név
        projectTaskWithAnyNames = spiedProjectTaskService.getProjectTaskById(2L); //"1. projekt 2. feladat"
        assertNotNull(projectTaskWithAnyNames);
        projectTaskWithAnyNames.setName("");
        assertEquals(
            ServiceException.Exceptions.PROJECTTASK_MODIFY_EMPTY_NAME.getDescription(),
            assertThrows(
                ServiceException.class,
                () -> spiedProjectTaskService.modifyProjectTask(projectTaskWithAnyNames)
            ).getMessage()
        );
        // - Ilyen nevű task már van a projektben
        projectTaskWithAnyNames.setName("1. projekt 1. feladat");
        assertEquals(
            ServiceException.Exceptions.PROJECTTASK_MODIFY_SAME_PROJECT_AND_NAME.getDescription(),
            assertThrows(
                ServiceException.class,
                () -> spiedProjectTaskService.modifyProjectTask(projectTaskWithAnyNames)
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
    void projectTaskServiceDeleteTest() throws ExecutionException, InterruptedException {
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
            assertThrows(
                ServiceException.class,
                () -> spiedProjectTaskService.deleteProjectTask(projectTaskWithDeveloper)
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
}
