package combit.hu.porphyr;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.repository.DeveloperRepository;
import combit.hu.porphyr.service.DeveloperService;
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
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("service_test")
class DeveloperTests {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DeveloperRepository developerRepository;

    private DeveloperRepository spyDeveloperRepository;
    final private DeveloperService spiedDeveloperService = new DeveloperService();

    @BeforeAll
    void setupAll() {
        spyDeveloperRepository = Mockito.mock(
            DeveloperRepository.class, AdditionalAnswers.delegatesTo(developerRepository)
        );
        spiedDeveloperService.setDeveloperRepository(spyDeveloperRepository);
        spiedDeveloperService.setEntityManager(entityManager);
        ServiceException.initExceptionsCounter();
    }

    @BeforeEach
    void setupEach() {
        entityManager.clear();
        Mockito.clearInvocations(spyDeveloperRepository);
    }

    @Test
    @Transactional
    @Rollback
    void developerRepositoryTest() {
        Long developerId;
        final DeveloperEntity expectedDeveloper = new DeveloperEntity("");
        DeveloperEntity actualDeveloper;
        //-------------------------------- Developer felvétele --
        // - Üres névvel (constraint ellenőrzése )
        assertThrows(Exception.class, () -> spiedDeveloperService.insertNewDeveloper(expectedDeveloper));
        // - Már létező névvel (constraint ellenőrzése)
        expectedDeveloper.setName("1. fejlesztő");
        assertThrows(Exception.class, () -> spiedDeveloperService.insertNewDeveloper(expectedDeveloper));
        // - Még nem létező névvel
        expectedDeveloper.setName("New Developer");
        assertDoesNotThrow(() -> spyDeveloperRepository.saveAndFlush(expectedDeveloper));
        developerId = expectedDeveloper.getId();
        assert developerId != null;
        // - Visszaolvasás
        entityManager.clear();
        actualDeveloper = spyDeveloperRepository.findAllById(developerId);
        assertEquals(expectedDeveloper, actualDeveloper);
        //--------------------------------- Developer módosítása ---------------------------------
        expectedDeveloper.setName("Modified Developer");
        assertDoesNotThrow(() -> spyDeveloperRepository.saveAndFlush(expectedDeveloper));
        developerId = expectedDeveloper.getId();
        assert developerId != null;
        // - Visszaolvasás
        entityManager.clear();
        actualDeveloper = spyDeveloperRepository.findAllById(developerId);
        assertEquals(expectedDeveloper, actualDeveloper);
        // ----------------------------------- Developer törlése --------------------------------
        assertDoesNotThrow(() -> spyDeveloperRepository.deleteById(expectedDeveloper.getId()));
        actualDeveloper = spyDeveloperRepository.findAllById(developerId);
        assertNull(actualDeveloper);
        entityManager.clear();
        // - Visszaolvasás
        actualDeveloper = spyDeveloperRepository.findAllById(developerId);
        assertNull(actualDeveloper);
        // - fejlesztő törlése, aki projekthez van rendelve (foreign key ellenőrzése)
        final DeveloperEntity deleteDeveloper = spyDeveloperRepository.findAllById(1L);
        assertNotNull( deleteDeveloper );
        assertThrows(Exception.class, () -> spiedDeveloperService.deleteDeveloper(deleteDeveloper));
        //------------------------------- Lekérdezések ---------------------------------------------
        // - FindAll
        assertEquals(4, spyDeveloperRepository.findAll().size());
        // - FindAllByName
        assertEquals(1, spyDeveloperRepository.findAllByName("1. fejlesztő").size());
        // - FindAllByNameIdNot
        assertEquals(0, spyDeveloperRepository.findAllByNameAndIdNot("1. fejlesztő", 1L).size());
        assertEquals(1, spyDeveloperRepository.findAllByNameAndIdNot("1. fejlesztő", 2L).size());
    }

    @Test
    @Transactional
    @Rollback
    void developerServiceInsertTest() throws ExecutionException, InterruptedException {
        Long developerId;
        DeveloperEntity developerForInsert;
        DeveloperEntity actualDeveloper;
        //----------------------- Felvétel: insertNewDeveloper(); -----------------------------------------
        // - Kitöltetlen névvel
        developerForInsert = new DeveloperEntity();
        assertEquals(
            ServiceException.Exceptions.DEVELOPER_INSERT_EMPTY_NAME.getDescription(),
            assertThrows(ServiceException.class, () -> spiedDeveloperService.insertNewDeveloper(developerForInsert)
            ).getMessage()
        );
        // - Már létező névvel
        developerForInsert.setName("1. fejlesztő");
        assertEquals(
            ServiceException.Exceptions.DEVELOPER_INSERT_SAME_NAME.getDescription(),
            assertThrows(ServiceException.class, () -> spiedDeveloperService.insertNewDeveloper(developerForInsert)
            ).getMessage()
        );
        // - Még nem létező névvel
        developerForInsert.setName("5. fejlesztő");
        assertDoesNotThrow(() -> spiedDeveloperService.insertNewDeveloper(developerForInsert));
        verify(spyDeveloperRepository, times(1)).saveAndFlush(developerForInsert);
        // - Visszaolvasás
        developerId = developerForInsert.getId();
        entityManager.clear();
        assertNotNull(developerId);
        actualDeveloper = Objects.requireNonNull(spiedDeveloperService.getDeveloperById(developerId));
        assertEquals("5. fejlesztő", actualDeveloper.getName());
        // - Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> ServiceException.isAllExceptionsThrown(ServiceException.ExceptionGroups.DEVELOPER_INSERT));
    }

    @Test
    @Transactional
    @Rollback
    void developerServiceModifyTest() throws ExecutionException, InterruptedException {
        Long developerId;
        final DeveloperEntity developerWithEmptyId = new DeveloperEntity();
        final DeveloperEntity developerWithAnyNames;
        DeveloperEntity actualDeveloper;
        //------------------------------ Módosítás: modifyDeveloper(); ----------------------------------------
        // - Nincs kitöltve az ID
        assertEquals(
            ServiceException.Exceptions.DEVELOPER_MODIFY_NOT_SAVED.getDescription(),
            assertThrows(ServiceException.class, () -> spiedDeveloperService.modifyDeveloper(developerWithEmptyId)
            ).getMessage()
        );
        // - Nincs kitöltve a név
        developerWithAnyNames = Objects.requireNonNull(spiedDeveloperService.getDeveloperById(4L));
        developerWithAnyNames.setName("");
        assertEquals(
            ServiceException.Exceptions.DEVELOPER_MODIFY_EMPTY_NAME.getDescription(),
            assertThrows(ServiceException.class, () -> spiedDeveloperService.modifyDeveloper(developerWithAnyNames)
            ).getMessage()
        );
        // - Ki van töltve a név, de van már ilyen.
        developerWithAnyNames.setName("1. fejlesztő");
        assertEquals(
            ServiceException.Exceptions.DEVELOPER_MODIFY_SAME_NAME.getDescription(),
            assertThrows(ServiceException.class, () -> spiedDeveloperService.modifyDeveloper(developerWithAnyNames)
            ).getMessage()
        );
        // - Ki van töltve a név, ugyanaz, ami volt: Nem hiba
        developerWithAnyNames.setName("4. fejlesztő");
        assertDoesNotThrow(
            () -> spiedDeveloperService.modifyDeveloper(developerWithAnyNames)
        );
        entityManager.clear();
        actualDeveloper = spiedDeveloperService.getDeveloperById(4L);
        assertNotNull(actualDeveloper);
        assertEquals("4. fejlesztő", actualDeveloper.getName());
        verify(spyDeveloperRepository, times(1)).saveAndFlush(any(DeveloperEntity.class));
        // - Ki van töltve a név, másra
        developerWithAnyNames.setName("Negyedik fejlesztő");
        assertDoesNotThrow(
            () -> spiedDeveloperService.modifyDeveloper(developerWithAnyNames)
        );
        verify(spyDeveloperRepository, times(2)).saveAndFlush(any(DeveloperEntity.class));
        // - Visszaolvasás
        entityManager.clear();
        developerId = developerWithAnyNames.getId();
        assertNotNull(developerId);
        actualDeveloper = spiedDeveloperService.getDeveloperById(developerId);
        assertEquals("Negyedik fejlesztő", Objects.requireNonNull(actualDeveloper).getName());
        // - Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> ServiceException.isAllExceptionsThrown(ServiceException.ExceptionGroups.DEVELOPER_MODIFY));
    }

    @Test
    @Transactional
    @Rollback
    void developerServiceDeleteTest() throws ExecutionException, InterruptedException {
        Long developerId;
        final DeveloperEntity developerWithEmptyId;
        final DeveloperEntity developerWithProject;
        DeveloperEntity developerWithNoProject;
        DeveloperEntity actualDeveloper;
        // ---------------------------------- Törlés: deleteDeveloper() ----------------------------------------
        // - Nincs kitöltve az ID
        developerWithEmptyId = new DeveloperEntity();
        assertEquals(
            ServiceException.Exceptions.DEVELOPER_DELETE_NOT_SAVED.getDescription(),
            assertThrows(ServiceException.class, () -> spiedDeveloperService.deleteDeveloper(developerWithEmptyId)
            ).getMessage()
        );
        // - Már létező developer, de még be van osztva projekthez.
        entityManager.clear();
        developerWithProject = spiedDeveloperService.getDeveloperById(1L);
        assertNotNull(developerWithProject);
        assertEquals(
            ServiceException.Exceptions.DEVELOPER_DELETE_ASSIGNED_TO_PROJECTS.getDescription(),
            assertThrows(ServiceException.class, () -> spiedDeveloperService.deleteDeveloper(developerWithProject)
            ).getMessage()
        );
        // - Létező developer, nincs projekthez rendelve
        entityManager.clear();
        developerWithNoProject = new DeveloperEntity("Ötödik fejlesztő");
        spiedDeveloperService.insertNewDeveloper(developerWithNoProject);
        entityManager.clear();
        actualDeveloper = spiedDeveloperService.getDeveloperByName("Ötödik fejlesztő");
        assertNotNull(actualDeveloper);
        developerId = actualDeveloper.getId();
        assertNotNull( developerId );
        assertDoesNotThrow(() -> spiedDeveloperService.deleteDeveloper(actualDeveloper));
        verify(spyDeveloperRepository, times(1)).deleteById(developerId);
        // - Visszaolvasás
        entityManager.clear();
        assertNull(spiedDeveloperService.getDeveloperById(developerId));
        assertNull(spiedDeveloperService.getDeveloperByName("Ötödik fejlesztő"));
        // - Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> ServiceException.isAllExceptionsThrown(ServiceException.ExceptionGroups.DEVELOPER_DELETE));
    }

    @Test
    @Transactional
    @Rollback
    void developerServiceQueriesTest() throws ExecutionException, InterruptedException {
        DeveloperEntity actualDeveloper;
        //----------------------- Minden developer lekérdezése: getDevelopers() --------------------------
        assertEquals(4, spiedDeveloperService.getDevelopers().size());
        verify(spyDeveloperRepository, times(1)).findAll();
        // --------------------------------- Egyéb lekérdezések ------------------------------------------
        // Egy developer projektjei: getDeveloperProjects()
        entityManager.clear();
        actualDeveloper = Objects.requireNonNull(spiedDeveloperService.getDeveloperByName("1. fejlesztő"));
        assertEquals(1, actualDeveloper.getDeveloperProjects().size());
        actualDeveloper = Objects.requireNonNull(spiedDeveloperService.getDeveloperByName("2. fejlesztő"));
        assertEquals(2, actualDeveloper.getDeveloperProjects().size());
        actualDeveloper = Objects.requireNonNull(spiedDeveloperService.getDeveloperByName("3. fejlesztő"));
        assertEquals(2, actualDeveloper.getDeveloperProjects().size());
        actualDeveloper = Objects.requireNonNull(spiedDeveloperService.getDeveloperByName("4. fejlesztő"));
        assertEquals(2, actualDeveloper.getDeveloperProjects().size());
    }
}
