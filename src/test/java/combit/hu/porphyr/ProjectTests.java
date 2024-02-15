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

import static combit.hu.porphyr.TestConstants.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
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
    // OneToMany kapcsolatok lekérdezésének ellenőrzése
    void projectEntityQueriesTest() {
        ProjectEntity actualProject;
        //getProjectDevelopers
        actualProject = spyProjectRepository.findAllById(1L);
        assert actualProject != null;
        assertArrayEquals(
            new String[]{developerNames[0], developerNames[1], developerNames[2]},
            actualProject.getProjectDevelopers()
                .stream()
                .map(ProjectDeveloperEntity::getDeveloperEntity)
                .map(DeveloperEntity::getName)
                .toArray(String[]::new)
        );
        //getProjectTasks
        assertArrayEquals(
            taskNames[0],
            actualProject.getProjectTasks()
                .stream()
                .map(ProjectTaskEntity::getName)
                .toArray(String[]::new)
        );
        //getProjectTasks olyan projektre, aminek nincsenek feladatai
        actualProject = spyProjectRepository.findAllById(3L);
        assert actualProject != null;
        assertArrayEquals(
            new String[]{},
            actualProject.getProjectTasks()
                .stream()
                .map(ProjectTaskEntity::getName)
                .toArray(String[]::new)
        );
    }

    @Test
    @Transactional
    @Rollback
    void projectServiceQueriesTest() throws ExecutionException, InterruptedException {
        //getProjects()
        final List<ProjectEntity> actualProjects = spiedProjectService.getProjects();
        assertArrayEquals(
            projectNames,
            actualProjects.stream().map(ProjectEntity::getName).toArray(String[]::new)
        );
        verify(spyProjectRepository, times(1)).findAll();
        //getProjectById
        assertArrayEquals(
            new Long[]{1L, 2L, 3L, 4L},
            new Long[]{
                Objects.requireNonNull(spiedProjectService.getProjectById(1L)).getId(),
                Objects.requireNonNull(spiedProjectService.getProjectById(2L)).getId(),
                Objects.requireNonNull(spiedProjectService.getProjectById(3L)).getId(),
                Objects.requireNonNull(spiedProjectService.getProjectById(4L)).getId()
            }
        );
        verify(spyProjectRepository, times(4)).findAllById(anyLong());
        //getProjectByName
        assertArrayEquals(
            projectNames,
            new String[]{
                Objects.requireNonNull(spiedProjectService.getProjectByName(projectNames[0])).getName(),
                Objects.requireNonNull(spiedProjectService.getProjectByName(projectNames[1])).getName(),
                Objects.requireNonNull(spiedProjectService.getProjectByName(projectNames[2])).getName(),
                Objects.requireNonNull(spiedProjectService.getProjectByName(projectNames[3])).getName()
            }
        );
        verify(spyProjectRepository, times(4)).findAllByName(anyString());
        //isProjectByNameAndIdNot
        assertArrayEquals(
            new Boolean[]{false, false, false, false},
            new Boolean[]{
                spiedProjectService.isProjectWithNameAndNotId(projectNames[0], 1L),
                spiedProjectService.isProjectWithNameAndNotId(projectNames[1], 2L),
                spiedProjectService.isProjectWithNameAndNotId(projectNames[2], 3L),
                spiedProjectService.isProjectWithNameAndNotId(projectNames[3], 4L)
            }
        );
        assertArrayEquals(
            new Boolean[]{true,true,true,true},
            new Boolean[]{
                spiedProjectService.isProjectWithNameAndNotId(projectNames[0], 4L),
                spiedProjectService.isProjectWithNameAndNotId(projectNames[1], 3L),
                spiedProjectService.isProjectWithNameAndNotId(projectNames[2], 2L),
                spiedProjectService.isProjectWithNameAndNotId(projectNames[3], 1L)
            }
        );
        verify(spyProjectRepository, times(8)).findAllByNameAndIdNot(anyString(), anyLong());
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
        projectWithSameName = new ProjectEntity(projectNames[0], "");
        assertThrows(Exception.class, () -> spyProjectRepository.saveAndFlush(projectWithSameName));
        entityManager.clear();
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
        projectWithDeveloper = Objects.requireNonNull(spiedProjectService.getProjectByName(projectNames[2]));
        assertThrows(Exception.class, () -> spiedProjectService.deleteProject(projectWithDeveloper));
        // - projekt törlése, amihez feladat van rendelve, de projekt nincs (foreign key ellenőrzése)
        projectWithTask = Objects.requireNonNull(spiedProjectService.getProjectByName(projectNames[3]));
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
        projectForInsert.setName(projectNames[0]);
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
        projectWithAnyNames.setName(projectNames[0]);
        assertThrows(
            ServiceException.class,
            () -> spiedProjectService.modifyProject(projectWithAnyNames),
            ServiceException.Exceptions.PROJECT_MODIFY_SAME_NAME.getDescription()
        );
        // - Ki van töltve a név, ugyanaz, ami volt: Nem hiba
        projectWithAnyNames.setName(projectNames[1]);
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
        projectWithDeveloper = spiedProjectService.getProjectByName(projectNames[2]);
        assertNotNull(projectWithDeveloper);
        assertThrows(
            ServiceException.class,
            () -> spiedProjectService.deleteProject(projectWithDeveloper),
            ServiceException.Exceptions.PROJECT_DELETE_DEVELOPERS_ASSIGNED.getDescription()
        );
        // - Már létező project, de még van hozzá feladat
        entityManager.clear();
        projectWithTask = spiedProjectService.getProjectByName(projectNames[3]);
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
