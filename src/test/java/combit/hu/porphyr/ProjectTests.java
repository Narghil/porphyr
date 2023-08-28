package combit.hu.porphyr;

import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.repository.ProjectRepository;
import combit.hu.porphyr.service.ProjectService;
import combit.hu.porphyr.service.ServiceException;
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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("service_test")
class ProjectTests {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProjectRepository projectRepository;

    private ProjectRepository spyProjectRepository;
    final private ProjectService spiedProjectService = new ProjectService();

    @BeforeAll
    void setupAll() {
        spyProjectRepository = Mockito.mock(
            ProjectRepository.class, AdditionalAnswers.delegatesTo(projectRepository)
        );
        spiedProjectService.setProjectRepository(spyProjectRepository);
        spiedProjectService.setEntityManager(entityManager);
        ServiceException.initExceptionsCounter();
    }

    @BeforeEach
    void setupEach(){
        entityManager.clear();
        Mockito.clearInvocations(spyProjectRepository);
    }

    @Test
    @Transactional
    @Rollback
    void projectRepositoryTest() {
        Long projectId;
        final ProjectEntity projectWithSameName;
        final ProjectEntity projectWithNewName;
        final ProjectEntity projectWithDeveloper;
        final ProjectEntity projectWithTask;
        ProjectEntity actualProject;
        //-------------------------------- Project felvétele --
        // - Már létező névvel (constraint ellenőrzése)
        projectWithSameName = new ProjectEntity("1. projekt","");
        assertThrows(Exception.class, () -> spiedProjectService.insertNewProject(projectWithSameName));
        // - Még nem létező névvel
        projectWithNewName = new ProjectEntity("New Project","");
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
        assertThrows( Exception.class, () -> spiedProjectService.deleteProject(projectWithDeveloper));
        // - projekt törlése, amihez feladat van rendelve, de projekt nincs (foreign key ellenőrzése)
        projectWithTask = Objects.requireNonNull(spiedProjectService.getProjectByName("Projekt feladattal"));
        assertThrows( Exception.class, () -> spiedProjectService.deleteProject(projectWithTask));
        //------------------------------- Lekérdezések ---------------------------------------------
        // - FindAll
        assertEquals(4, spyProjectRepository.findAll().size());
        // - FindAllByName
        assertEquals(1, spyProjectRepository.findAllByName("1. projekt").size());
        // - FindAllByNameIdNot
        assertEquals(0, spyProjectRepository.findAllByNameAndIdNot("1. projekt", 1L).size());
        assertEquals(1, spyProjectRepository.findAllByNameAndIdNot("1. projekt", 2L).size());
    }

    @Test
    @Transactional
    @Rollback
    void projectServiceInsertTest() {
        Long projectId;
        final ProjectEntity projectForInsert;
        ProjectEntity actualProject;
        //----------------------- Felvétel: insertNewProject(); -----------------------------------------
        // - Kitöltetlen névvel
        projectForInsert = new ProjectEntity("","");
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
        assertDoesNotThrow(() -> ServiceException.isAllExceptionsThrown( ServiceException.ExceptionGroups.PROJECT_INSERT ) );
    }

    @Test
    @Transactional
    @Rollback
    void projectServiceModifyTest() {
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
        assertNotNull( actualProject );
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
        assertEquals(projectWithAnyNames, actualProject );
        // - Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> ServiceException.isAllExceptionsThrown( ServiceException.ExceptionGroups.PROJECT_MODIFY ) );
    }

    @Test
    @Transactional
    @Rollback
    void projectServiceDeleteTest() {
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
        assertDoesNotThrow(() -> ServiceException.isAllExceptionsThrown( ServiceException.ExceptionGroups.PROJECT_DELETE ) );
    }

    @Test
    @Transactional
    @Rollback
    void projectServiceQueriesTest() {
        ProjectEntity actualProject;
        //----------------------- Minden project lekérdezése: getProjects() --------------------------
        assertEquals(4, spiedProjectService.getProjects().size());
        verify(spyProjectRepository, times(1)).findAll();
        // --------------------------------- Egyéb lekérdezések ------------------------------------------
        // Projektek fejlesztői: getProjectDevelopers()
        actualProject = Objects.requireNonNull(spiedProjectService.getProjectByName("1. projekt"));
        assertEquals(3, actualProject.getProjectDevelopers().size());
        actualProject = Objects.requireNonNull(spiedProjectService.getProjectByName("2. projekt"));
        assertEquals(3, actualProject.getProjectDevelopers().size());
        actualProject = Objects.requireNonNull(spiedProjectService.getProjectByName("Projekt fejlesztővel"));
        assertEquals(1, actualProject.getProjectDevelopers().size());
        actualProject = Objects.requireNonNull(spiedProjectService.getProjectByName("Projekt feladattal"));
        assertEquals(0, actualProject.getProjectDevelopers().size());
        // Projektek feladatai: getProjectTasks()
        actualProject = Objects.requireNonNull(spiedProjectService.getProjectByName("1. projekt"));
        assertEquals(2, actualProject.getProjectTasks().size());
        actualProject = Objects.requireNonNull(spiedProjectService.getProjectByName("2. projekt"));
        assertEquals(2, actualProject.getProjectTasks().size());
        actualProject = Objects.requireNonNull(spiedProjectService.getProjectByName("Projekt fejlesztővel"));
        assertEquals(0, actualProject.getProjectTasks().size());
        actualProject = Objects.requireNonNull(spiedProjectService.getProjectByName("Projekt feladattal"));
        assertEquals(1, actualProject.getProjectTasks().size());
    }

}
