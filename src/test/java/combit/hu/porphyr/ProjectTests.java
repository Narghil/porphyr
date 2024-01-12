package combit.hu.porphyr;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import combit.hu.porphyr.repository.ProjectRepository;
import combit.hu.porphyr.service.ProjectService;
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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("service_test")
class ProjectTests {
    @Autowired
    private @NonNull EntityManager entityManager;

    @Autowired
    private @NonNull ProjectRepository projectRepository;

    private ProjectRepository spyProjectRepository;
    private ProjectService spiedProjectService;

    @BeforeAll
    void setupAll() {
        spiedProjectService = new ProjectService(entityManager, projectRepository);
        spyProjectRepository = Mockito.mock(
            ProjectRepository.class, AdditionalAnswers.delegatesTo(projectRepository)
        );
        spiedProjectService.setProjectRepository(spyProjectRepository);
        spiedProjectService.setEntityManager(entityManager);
        ServiceException.initExceptionsCounter();
    }

    @BeforeEach
    void setupEach() {
        entityManager.clear();
        Mockito.clearInvocations(spyProjectRepository);
    }

    @Test
    @Transactional
    @Rollback
    void projectEntityQueriesTest() {
        ProjectEntity actualProject;
        //getProjectDevelopers
        actualProject = spyProjectRepository.findAllById(1L);
        assert actualProject != null;
        assertArrayEquals(
            new String[]{"1. fejlesztő", "2. fejlesztő", "3. fejlesztő"},
            actualProject.getProjectDevelopers()
                .stream()
                .map(ProjectDeveloperEntity::getDeveloperEntity)
                .map(DeveloperEntity::getName)
                .toArray(String[]::new)
        );
        //getProjectTasks
        assertArrayEquals(
            new String[]{"1. projekt 1. feladat", "1. projekt 2. feladat"},
            actualProject.getProjectTasks()
                .stream()
                .map(ProjectTaskEntity::getName)
                .toArray(String[]::new)
        );
        //getProjectDevelopers
        actualProject = spyProjectRepository.findAllById(2L);
        assert actualProject != null;
        assertArrayEquals(
            new String[]{"2. fejlesztő", "3. fejlesztő", "4. fejlesztő"},
            actualProject.getProjectDevelopers()
                .stream()
                .map(ProjectDeveloperEntity::getDeveloperEntity)
                .map(DeveloperEntity::getName)
                .toArray(String[]::new)
        );
        //getProjectTasks
        assertArrayEquals(
            new String[]{"2. projekt 1. feladat", "2. projekt 2. feladat"},
            actualProject.getProjectTasks()
                .stream()
                .map(ProjectTaskEntity::getName)
                .toArray(String[]::new)
        );
    }

    @Test
    @Transactional
    @Rollback
    void projectRepositoryQueriesTest() {
        //---- findAll() -----
        final List<ProjectEntity> actualProjects = spyProjectRepository.findAll();
        final String[][] actualProjectsData = new String[][]{
            actualProjects.stream().map(ProjectEntity::getName).toArray(String[]::new),
            actualProjects.stream().map(ProjectEntity::getDescription).toArray(String[]::new)
        };
        final String[][] expectedProjectsData = new String[][]{
            new String[]{"1. projekt", "2. projekt", "Projekt fejlesztővel", "Projekt feladattal"},
            new String[]{"Első projekt", "Második projekt", "Projekt fejlesztővel", "Projekt feladattal"}
        };
        assertArrayEquals(expectedProjectsData, actualProjectsData);
        //----- findAllBy* -----------------
        assertEquals(1, Objects.requireNonNull(spyProjectRepository.findAllById(1L)).getId());
        assertEquals(1, spyProjectRepository.findAllByName("1. projekt").size());
        assertEquals(0, spyProjectRepository.findAllByNameAndIdNot("1. projekt", 1L).size());
        assertEquals(1, spyProjectRepository.findAllByNameAndIdNot("1. projekt", 2L).size());
    }

    @Test
    @Transactional
    @Rollback
    void projectServiceQueriesTest() throws ExecutionException, InterruptedException {
        //----------------------- Minden project lekérdezése: getProjects() --------------------------
        final List<ProjectEntity> actualProjects = spiedProjectService.getProjects();
        final String[][] actualProjectsData = new String[][]{
            actualProjects.stream().map(ProjectEntity::getName).toArray(String[]::new),
            actualProjects.stream().map(ProjectEntity::getDescription).toArray(String[]::new)
        };
        final String[][] expectedProjectsData = new String[][]{
            new String[]{"1. projekt", "2. projekt", "Projekt fejlesztővel", "Projekt feladattal"},
            new String[]{"Első projekt", "Második projekt", "Projekt fejlesztővel", "Projekt feladattal"}
        };
        assertArrayEquals(expectedProjectsData, actualProjectsData);
        //getProjectById
        assertEquals(1, Objects.requireNonNull(spiedProjectService.getProjectById(1L)).getId());
        assertEquals(2, Objects.requireNonNull(spiedProjectService.getProjectById(2L)).getId());
        assertEquals(3, Objects.requireNonNull(spiedProjectService.getProjectById(3L)).getId());
        assertEquals(4, Objects.requireNonNull(spiedProjectService.getProjectById(4L)).getId());
        //getProjectByName
        final ProjectEntity[] actualProjectsArray = new ProjectEntity[]{
            Objects.requireNonNull(spiedProjectService.getProjectByName("1. projekt")),
            Objects.requireNonNull(spiedProjectService.getProjectByName("2. projekt")),
            Objects.requireNonNull(spiedProjectService.getProjectByName("Projekt fejlesztővel")),
            Objects.requireNonNull(spiedProjectService.getProjectByName("Projekt feladattal"))
        };
        //
        assertEquals("1. projekt", actualProjectsArray[0].getName());
        assertEquals("2. projekt", actualProjectsArray[1].getName());
        assertEquals("Projekt fejlesztővel", actualProjectsArray[2].getName());
        assertEquals("Projekt feladattal", actualProjectsArray[3].getName());
        //
        verify(spyProjectRepository, times(1)).findAll();
        verify(spyProjectRepository, times(4)).findAllById(anyLong());
        verify(spyProjectRepository, times(4)).findAllByName(anyString());
    }

    @Test
    @Transactional
    @Rollback
    void projectRepositoryTest() throws ExecutionException, InterruptedException {
        Long projectId;
        final ProjectEntity projectWithSameName;
        final ProjectEntity projectWithNewName;
        final ProjectEntity projectWithDeveloper;
        final ProjectEntity projectWithTask;
        ProjectEntity actualProject;
        //-------------------------------- Project felvétele --
        // - Már létező névvel (constraint ellenőrzése)
        projectWithSameName = new ProjectEntity("1. projekt", "");
        assertThrows(Exception.class, () -> spiedProjectService.insertNewProject(projectWithSameName));
        // - Még nem létező névvel
        projectWithNewName = new ProjectEntity("New Project", "");
        assertDoesNotThrow(() -> spyProjectRepository.saveAndFlush(projectWithNewName));
        projectId = projectWithNewName.getId();
        assert projectId != null;
        // - Visszaolvasás
        entityManager.clear();
        actualProject = spyProjectRepository.findAllById(projectId);
        assertEquals(projectWithNewName, actualProject);
        //--------------------------------- Project módosítása ---------------------------------
        projectWithNewName.setName("Modified Project");
        assertDoesNotThrow(() -> spyProjectRepository.saveAndFlush(projectWithNewName));
        projectId = projectWithNewName.getId();
        assert projectId != null;
        // - Visszaolvasás
        entityManager.clear();
        actualProject = spyProjectRepository.findAllById(projectId);
        assertEquals(projectWithNewName, actualProject);
        // ----------------------------------- Project törlése --------------------------------
        assertDoesNotThrow(() -> spyProjectRepository.deleteById(projectWithNewName.getId()));
        actualProject = spyProjectRepository.findAllById(projectId);
        assertNull(actualProject);
        entityManager.clear();
        // - Visszaolvasás
        actualProject = spyProjectRepository.findAllById(projectId);
        assertNull(actualProject);
        // - projekt törlése, amihez fejlesztő van rendelve, de feladat nincs (foreign key ellenőrzése)
        projectWithDeveloper = Objects.requireNonNull(spiedProjectService.getProjectByName("Projekt fejlesztővel"));
        assertThrows(Exception.class, () -> spiedProjectService.deleteProject(projectWithDeveloper));
        // - projekt törlése, amihez feladat van rendelve, de projekt nincs (foreign key ellenőrzése)
        projectWithTask = Objects.requireNonNull(spiedProjectService.getProjectByName("Projekt feladattal"));
        assertThrows(Exception.class, () -> spiedProjectService.deleteProject(projectWithTask));
    }

    @Test
    @Transactional
    @Rollback
    void projectServiceInsertTest() throws ExecutionException, InterruptedException {
        Long projectId;
        final ProjectEntity projectForInsert;
        ProjectEntity actualProject;
        //----------------------- Felvétel: insertNewProject(); -----------------------------------------
        // - Kitöltetlen névvel
        projectForInsert = new ProjectEntity("", "");
        assertThrows(
            ServiceException.class,
            () -> spiedProjectService.insertNewProject(projectForInsert),
            ServiceException.Exceptions.PROJECT_INSERT_EMPTY_NAME.getDescription()
        );
        // - Már létező névvel
        projectForInsert.setName("1. projekt");
        assertThrows(
            ServiceException.class,
            () -> spiedProjectService.insertNewProject(projectForInsert),
            ServiceException.Exceptions.PROJECT_INSERT_SAME_NAME.getDescription()
        );
        // - Még nem létező névvel
        projectForInsert.setName("5. projekt");
        assertDoesNotThrow(() -> spiedProjectService.insertNewProject(projectForInsert));
        verify(spyProjectRepository, times(1)).saveAndFlush(projectForInsert);
        // - Visszaolvasás
        projectId = projectForInsert.getId();
        entityManager.clear();
        assertNotNull(projectId);
        actualProject = Objects.requireNonNull(spiedProjectService.getProjectById(projectId));
        assertEquals(projectForInsert, actualProject);
        assertDoesNotThrow(() -> ServiceException.isAllExceptionsThrown(ServiceException.ExceptionGroups.PROJECT_INSERT));
    }

    @Test
    @Transactional
    @Rollback
    void projectServiceModifyTest() throws ExecutionException, InterruptedException {
        Long projectId;
        final ProjectEntity projectWithNoId;
        final ProjectEntity projectWithAnyNames;
        ProjectEntity actualProject;
        //------------------------------ Módosítás: modifyProject(); ----------------------------------------
        // - Nincs kitöltve az ID
        projectWithNoId = new ProjectEntity();
        assertThrows(
            ServiceException.class,
            () -> spiedProjectService.modifyProject(projectWithNoId),
            ServiceException.Exceptions.PROJECT_MODIFY_NOT_SAVED.getDescription()
        );
        // - Nincs kitöltve a név
        projectWithAnyNames = Objects.requireNonNull(spiedProjectService.getProjectById(2L));
        projectWithAnyNames.setName("");
        assertThrows(
            ServiceException.class,
            () -> spiedProjectService.modifyProject(projectWithAnyNames),
            ServiceException.Exceptions.PROJECT_MODIFY_EMPTY_NAME.getDescription()
        );
        // - Ki van töltve a név, de van már ilyen.
        projectWithAnyNames.setName("1. projekt");
        assertThrows(
            ServiceException.class,
            () -> spiedProjectService.modifyProject(projectWithAnyNames),
            ServiceException.Exceptions.PROJECT_MODIFY_SAME_NAME.getDescription()
        );
        // - Ki van töltve a név, ugyanaz, ami volt: Nem hiba
        projectWithAnyNames.setName("2. projekt");
        assertDoesNotThrow(
            () -> spiedProjectService.modifyProject(projectWithAnyNames)
        );
        entityManager.clear();
        actualProject = spiedProjectService.getProjectById(2L);
        assertNotNull(actualProject);
        assertEquals(projectWithAnyNames, actualProject);
        verify(spyProjectRepository, times(1)).saveAndFlush(any(ProjectEntity.class));
        // - Ki van töltve a név, másra
        projectWithAnyNames.setName("Második projekt");
        assertDoesNotThrow(
            () -> spiedProjectService.modifyProject(projectWithAnyNames)
        );
        verify(spyProjectRepository, times(2)).saveAndFlush(any(ProjectEntity.class));
        // - Visszaolvasás
        entityManager.clear();
        projectId = projectWithAnyNames.getId();
        assertNotNull(projectId);
        actualProject = spiedProjectService.getProjectById(projectId);
        assertNotNull(actualProject);
        assertEquals(projectWithAnyNames, actualProject);
        // - Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> ServiceException.isAllExceptionsThrown(ServiceException.ExceptionGroups.PROJECT_MODIFY));
    }

    @Test
    @Transactional
    @Rollback
    void projectServiceDeleteTest() throws ExecutionException, InterruptedException {
        Long projectId;
        final ProjectEntity projectWithNoId;
        final ProjectEntity projectWithDeveloper;
        final ProjectEntity projectWithTask;
        final ProjectEntity projectWithNoOthers;
        ProjectEntity actualProject;
        // ---------------------------------- Törlés: deleteProject() ----------------------------------------
        // - Nincs kitöltve az ID
        projectWithNoId = new ProjectEntity();
        assertThrows(
            ServiceException.class,
            () -> spiedProjectService.deleteProject(projectWithNoId),
            ServiceException.Exceptions.PROJECT_DELETE_NOT_SAVED.getDescription()
        );
        // - Már létező project, de még be van osztva hozzá fejlesztő
        entityManager.clear();
        projectWithDeveloper = spiedProjectService.getProjectByName("Projekt fejlesztővel");
        assertNotNull(projectWithDeveloper);
        assertThrows(
            ServiceException.class,
            () -> spiedProjectService.deleteProject(projectWithDeveloper),
            ServiceException.Exceptions.PROJECT_DELETE_DEVELOPERS_ASSIGNED.getDescription()
        );
        // - Már létező project, de még van hozzá feladat
        entityManager.clear();
        projectWithTask = spiedProjectService.getProjectByName("Projekt feladattal");
        assertNotNull(projectWithTask);
        assertThrows(
            ServiceException.class,
            () -> spiedProjectService.deleteProject(projectWithTask),
            ServiceException.Exceptions.PROJECT_DELETE_TASKS_ASSIGNED.getDescription()
        );
        // - Létező project, nincs semmihez rendelve
        entityManager.clear();
        projectWithNoOthers = new ProjectEntity("Harmadik projekt", "");
        spiedProjectService.insertNewProject(projectWithNoOthers);
        entityManager.clear();
        actualProject = spiedProjectService.getProjectByName("Harmadik projekt");
        assertNotNull(actualProject);
        projectId = actualProject.getId();
        assertNotNull(projectId);
        assertDoesNotThrow(() -> spiedProjectService.deleteProject(projectWithNoOthers));
        verify(spyProjectRepository, times(1)).deleteById(projectId);
        // - Visszaolvasás
        entityManager.clear();
        assertNull(spiedProjectService.getProjectById(projectId));
        assertNull(spiedProjectService.getProjectByName("Harmadik projekt"));
        // - Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> ServiceException.isAllExceptionsThrown(ServiceException.ExceptionGroups.PROJECT_DELETE));
    }
}
