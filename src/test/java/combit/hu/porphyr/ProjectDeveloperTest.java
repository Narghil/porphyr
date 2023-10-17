package combit.hu.porphyr;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.repository.DeveloperRepository;
import combit.hu.porphyr.repository.ProjectDeveloperRepository;
import combit.hu.porphyr.repository.ProjectRepository;
import combit.hu.porphyr.repository.ProjectTaskDeveloperRepository;
import combit.hu.porphyr.service.ProjectDeveloperService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("service_test")
class ProjectDeveloperTest {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProjectDeveloperRepository projectDeveloperRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DeveloperRepository developerRepository;

    @Autowired
    private ProjectTaskDeveloperRepository projectTaskDeveloperRepository;

    private ProjectDeveloperRepository spyProjectDeveloperRepository;
    final private ProjectDeveloperService spiedProjectDeveloperService = new ProjectDeveloperService();

    @BeforeAll
    void setupAll() {
        spyProjectDeveloperRepository = Mockito.mock(
            ProjectDeveloperRepository.class, AdditionalAnswers.delegatesTo(projectDeveloperRepository)
        );
        spiedProjectDeveloperService.setProjectDeveloperRepository(spyProjectDeveloperRepository);

        spiedProjectDeveloperService.setEntityManager(entityManager);
        spiedProjectDeveloperService.setProjectRepository(projectRepository);
        spiedProjectDeveloperService.setDeveloperRepository(developerRepository);
        spiedProjectDeveloperService.setProjectTaskDeveloperRepository(projectTaskDeveloperRepository);

        ServiceException.initExceptionsCounter();
    }

    @BeforeEach
    void setupEach() {
        entityManager.clear();
        Mockito.clearInvocations(spyProjectDeveloperRepository);
    }

    @Test
    @Transactional
    @Rollback
    void projectDeveloperRepositoryTest() {
        ProjectEntity projectEntity;
        DeveloperEntity developerEntity;
        ProjectDeveloperEntity projectDeveloperEntity = new ProjectDeveloperEntity();
        ProjectDeveloperEntity actualProjectDeveloperEntity;

        // - projectEntity.id = null (constraint ellenőrzés)
        projectEntity = new ProjectEntity("Project with null id", "");
        developerEntity = developerRepository.findAllById(1L);
        assertNotNull(projectEntity);
        assertNotNull(developerEntity);
        projectDeveloperEntity.setProjectAndDeveloper(projectEntity, developerEntity);
        assertThrows(
            DataIntegrityViolationException.class,
            () -> spyProjectDeveloperRepository.save(projectDeveloperEntity)
        );
        // - developerEntity.id = null (constraint ellenőrzés )
        entityManager.clear();
        projectEntity = projectRepository.findAllById(1L);
        developerEntity = new DeveloperEntity("Developer with null id");
        assertNotNull(projectEntity);
        assertNotNull(developerEntity);
        projectDeveloperEntity.setProjectAndDeveloper(projectEntity, developerEntity);
        assertThrows(
            DataIntegrityViolationException.class,
            () -> spyProjectDeveloperRepository.save(projectDeveloperEntity)
        );
        // - már létező tétel felvétele
        entityManager.clear();
        developerEntity = developerRepository.findAllById(1L);
        projectEntity = projectRepository.findAllById(1L);
        assertNotNull(projectEntity);
        assertNotNull(developerEntity);
        projectDeveloperEntity.setProjectAndDeveloper(projectEntity, developerEntity);
        assertThrows(
            DataIntegrityViolationException.class,
            () -> spyProjectDeveloperRepository.save(projectDeveloperEntity)
        );
        // - még nem létező tétel felvétele
        entityManager.clear();
        developerEntity = developerRepository.findAllById(1L);
        projectEntity = projectRepository.findAllById(4L);
        assertNotNull(projectEntity);
        assertNotNull(developerEntity);
        projectDeveloperEntity.setProjectAndDeveloper(projectEntity, developerEntity);
        assertDoesNotThrow(() -> spyProjectDeveloperRepository.saveAndFlush(projectDeveloperEntity));
        // Tétel törlése
        entityManager.clear();
        assertNotNull(projectDeveloperEntity.getId());
        spyProjectDeveloperRepository.deleteById(projectDeveloperEntity.getId());
        assertDoesNotThrow(() -> entityManager.flush());
        // Tétel törlése, ahol a fejlesztőhöz még tartozik feladat (FK ellenőrzés)
        entityManager.clear();
        spyProjectDeveloperRepository.deleteById(1L);
        assertThrows(javax.persistence.PersistenceException.class, () -> entityManager.flush());
        //Lekérdezések
        entityManager.clear();
        developerEntity = developerRepository.findAllById(1L);
        projectEntity = projectRepository.findAllById(1L);
        assertNotNull(projectEntity);
        assertNotNull(developerEntity);
        assert (projectDeveloperRepository.findAll().size() > 0);
        assert (projectDeveloperRepository.findAllByDeveloperEntity(developerEntity).size() > 0);
        assert (projectDeveloperRepository.findAllByProjectEntity(projectEntity).size() > 0);
        actualProjectDeveloperEntity = projectDeveloperRepository.findAllByProjectEntityAndDeveloperEntity(
            projectEntity,
            developerEntity
        );
        assertNotNull(actualProjectDeveloperEntity);
        assertNotNull(actualProjectDeveloperEntity.getId());
        assertNotNull(projectDeveloperRepository.findAllById(actualProjectDeveloperEntity.getId()));
    }

    @Test
    @Transactional
    @Rollback
    void projectDeveloperServiceInsertTest() {
        ProjectEntity projectEntity;
        DeveloperEntity developerEntity;
        ProjectDeveloperEntity newProjectDeveloperEntity = new ProjectDeveloperEntity();
        // - projectEntity.id = null
        projectEntity = new ProjectEntity();
        developerEntity = developerRepository.findAllById(1L);
        assertNotNull(developerEntity);
        newProjectDeveloperEntity.setProjectAndDeveloper(projectEntity, developerEntity);
        assertEquals(
            ServiceException.Exceptions.PROJECTDEVELOPER_INSERT_PROJECT_NOT_SAVED.getDescription(),
            assertThrows(
                ServiceException.class,
                () -> spiedProjectDeveloperService.insertNewProjectDeveloper(newProjectDeveloperEntity)
            ).getMessage()
        );
        // - projectEntity nincs az adatbázisban
        projectEntity.setName("Deleted Project");
        projectRepository.saveAndFlush(projectEntity);
        projectRepository.delete(projectEntity);
        entityManager.flush();
        assertEquals(
            ServiceException.Exceptions.PROJECTDEVELOPER_INSERT_PROJECT_NOT_EXISTS.getDescription(),
            assertThrows(
                ServiceException.class,
                () -> spiedProjectDeveloperService.insertNewProjectDeveloper(newProjectDeveloperEntity)
            ).getMessage()
        );
        // - developerEntity.id = nul
        projectEntity = projectRepository.findAllById(1L);
        assertNotNull(projectEntity);
        developerEntity = new DeveloperEntity();
        newProjectDeveloperEntity.setProjectAndDeveloper(projectEntity, developerEntity);
        assertEquals(
            ServiceException.Exceptions.PROJECTDEVELOPER_INSERT_DEVELOPER_NOT_SAVED.getDescription(),
            assertThrows(
                ServiceException.class,
                () -> spiedProjectDeveloperService.insertNewProjectDeveloper(newProjectDeveloperEntity)
            ).getMessage()
        );
        // - developerEntity nincs az adatbázisban
        developerEntity.setName("Deleted Developer");
        developerRepository.saveAndFlush(developerEntity);
        developerRepository.delete(developerEntity);
        entityManager.flush();
        assertEquals(
            ServiceException.Exceptions.PROJECTDEVELOPER_INSERT_DEVELOPER_NOT_EXISTS.getDescription(),
            assertThrows(
                ServiceException.class,
                () -> spiedProjectDeveloperService.insertNewProjectDeveloper(newProjectDeveloperEntity)
            ).getMessage()
        );
        // - már van ilyen tétel
        projectEntity = projectRepository.findAllById(1L);
        developerEntity = developerRepository.findAllById(1L);
        assertNotNull(projectEntity);
        assertNotNull(developerEntity);
        newProjectDeveloperEntity.setProjectAndDeveloper(projectEntity, developerEntity);
        assertEquals(
            ServiceException.Exceptions.PROJECTDEVELOPER_INSERT_EXISTING_DATA.getDescription(),
            assertThrows(
                ServiceException.class,
                () -> spiedProjectDeveloperService.insertNewProjectDeveloper(newProjectDeveloperEntity)
            ).getMessage()
        );
        // - mentés jó adatokkal
        developerEntity = developerRepository.findAllById(1L);
        projectEntity = projectRepository.findAllById(4L);
        assertNotNull(projectEntity);
        assertNotNull(developerEntity);
        newProjectDeveloperEntity.setProjectAndDeveloper(projectEntity, developerEntity);
        assertDoesNotThrow( () -> spiedProjectDeveloperService.insertNewProjectDeveloper(newProjectDeveloperEntity));
        verify(spyProjectDeveloperRepository, times(1)).saveAndFlush(newProjectDeveloperEntity);
        //Visszaolvasás
        entityManager.clear();
        assertNotNull(newProjectDeveloperEntity.getId());
        assertEquals( newProjectDeveloperEntity , spiedProjectDeveloperService.getProjectDeveloperById( newProjectDeveloperEntity.getId()));
        // - Minden hibát ellenőriztünk
        assertDoesNotThrow(() -> ServiceException.isAllExceptionsThrown(ServiceException.ExceptionGroups.PROJECTDEVELOPERS_INSERT));
    }

    @Test
    @Transactional
    @Rollback
    void projectDeveloperServiceDeleteTest() {
        // - Az entity még nincs elmentve
        final @NonNull ProjectDeveloperEntity newProjectDeveloperEntity = new ProjectDeveloperEntity();
        assertEquals(
            ServiceException.Exceptions.PROJECTDEVELOPER_DELETE_NOT_SAVED.getDescription(),
            assertThrows(
                ServiceException.class,
                () -> spiedProjectDeveloperService.deleteProjectDeveloper(newProjectDeveloperEntity)
            ).getMessage()
        );
        // - nem törölhető az az összerendelés, amihez tartozik ProjectTaskDeveloper bejegyzés
        entityManager.clear();
        final ProjectDeveloperEntity projectDeveloperEntityWithTask = spyProjectDeveloperRepository.findAllById(1L);
        assertNotNull(projectDeveloperEntityWithTask);
        assertEquals(
            ServiceException.Exceptions.PROJECTDEVELOPER_DELETE_ASSIGNED_TO_TASK.getDescription(),
            assertThrows(
                ServiceException.class,
                () -> spiedProjectDeveloperService.deleteProjectDeveloper(projectDeveloperEntityWithTask)
            ).getMessage()
        );
        entityManager.clear();
        // - törlés jó adatokkal
        final ProjectDeveloperEntity projectDeveloperEntityToDelete = spyProjectDeveloperRepository.findAllById(7L);
        assertNotNull(projectDeveloperEntityToDelete);
        assertDoesNotThrow( ()-> spiedProjectDeveloperService.deleteProjectDeveloper(projectDeveloperEntityToDelete));
        verify( spyProjectDeveloperRepository, times(1)).deleteById(7L);
        // - visszaolvasás
        ProjectDeveloperEntity actualProjectDeveloperEntity = spyProjectDeveloperRepository.findAllById(7L);
        assertNull( actualProjectDeveloperEntity);
        // - Minden hibát ellenőriztünk
        assertDoesNotThrow(() -> ServiceException.isAllExceptionsThrown(ServiceException.ExceptionGroups.PROJECTDEVELOPERS_DELETE));
    }

    @Test
    @Transactional
    @Rollback
    void projectDeveloperServiceQueriesTest() {
        DeveloperEntity developerEntity;
        ProjectEntity projectEntity;
        // - getProjectDevelopers
        assertEquals( 7 , spiedProjectDeveloperService.getProjectDevelopers().size() );
        // - getProjectDevelopersByDeveloper
        developerEntity = developerRepository.findAllById(2L);
        assertNotNull(developerEntity);
        assertEquals( 2, spiedProjectDeveloperService.getProjectDevelopersByDeveloper(developerEntity).size());
        // - getProjectDevelopersByProject
        projectEntity = projectRepository.findAllById(1L);
        assertNotNull(projectEntity);
        assertEquals( 3, spiedProjectDeveloperService.getProjectDevelopersByProject(projectEntity).size());
        // - getProjectDeveloperById
        assertNotNull( spiedProjectDeveloperService.getProjectDeveloperById(1L));
        // - getProjectDeveloperByProjectAndDeveloper
        assertNotNull( spiedProjectDeveloperService.getProjectDeveloperByProjectAndDeveloper(projectEntity,developerEntity));
    }
}
