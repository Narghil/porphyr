
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
            DeveloperRepository.class,
            AdditionalAnswers.delegatesTo(developerRepository)
        );
        spiedDeveloperService.setDeveloperRepository(spyDeveloperRepository);
    }

    @BeforeEach
    void setupEach(){
        entityManager.clear();
    }

    private DeveloperEntity expectedDeveloper, actualDeveloper, myDeveloper;

    @Test
    @Transactional
    @Rollback
    void developerRepositoryTest() {
        //Developer felvétele
        myDeveloper = new DeveloperEntity("New Developer");
        spyDeveloperRepository.saveAndFlush(myDeveloper);
        expectedDeveloper = myDeveloper;
        assert myDeveloper.getId() != null;
        actualDeveloper = spyDeveloperRepository.findAllById(myDeveloper.getId());
        assertEquals(expectedDeveloper, actualDeveloper);
        //Developer módosítása
        myDeveloper.setName("Modified Developer");
        spyDeveloperRepository.saveAndFlush(myDeveloper);
        expectedDeveloper = myDeveloper;
        assert myDeveloper.getId() != null;
        actualDeveloper = spyDeveloperRepository.findAllById(myDeveloper.getId());
        assertEquals(expectedDeveloper, actualDeveloper);
        //Developer törlése
        assert myDeveloper.getId() != null;
        spyDeveloperRepository.deleteById(myDeveloper.getId());
        actualDeveloper = spyDeveloperRepository.findAllById(myDeveloper.getId());
        assertNull(actualDeveloper);
        //Developer törlése, aki még projekthez van rendelve - hiba
        spyDeveloperRepository.deleteById(1L) ;
        assertThrows(javax.persistence.PersistenceException.class, () -> entityManager.flush());
        entityManager.clear();
        actualDeveloper = spyDeveloperRepository.findAllById(1L);
        assertNotNull(actualDeveloper);
        //FindAll
        assertEquals( 4, spyDeveloperRepository.findAll().size());
        //FindAllByName
        assertEquals( 1, spyDeveloperRepository.findAllByName("1. fejlesztő").size());
        //FindAllByNameIdNot
        assertEquals( 0, spyDeveloperRepository.findAllByNameAndIdNot("1. fejlesztő", 1L).size());
        assertEquals( 1, spyDeveloperRepository.findAllByNameAndIdNot("1. fejlesztő", 2L).size());
    }

    @Test
    @Transactional
    @Rollback
    void developerServiceTest() {
        //entityManager.clear();
        //Minden developer lekérdezése
        assertEquals( 4, spiedDeveloperService.getDevelopers().size() );
        verify(spyDeveloperRepository, times(1)).findAll();
        //Felvétel
        myDeveloper = new DeveloperEntity();
        // - Kitöltetlen névvel
        assertThrows(
            ServiceException.class,
            () -> spiedDeveloperService.insertNewDeveloper(myDeveloper),
            ServiceException.Exceptions.DEVELOPER_WITH_EMPTY_NAME_CANT_INSERT.getDescription()
        );
        // - Már létező névvel
        myDeveloper.setName("1. fejlesztő");
        assertThrows(
            ServiceException.class,
            () -> spiedDeveloperService.insertNewDeveloper(myDeveloper),
            ServiceException.Exceptions.DEVELOPER_WITH_SAME_NAME_CANT_INSERT.getDescription()
        );
        // - Még nem létező névvel
        myDeveloper.setName("5. fejlesztő");
        assertDoesNotThrow( () -> spiedDeveloperService.insertNewDeveloper(myDeveloper));
        verify(spyDeveloperRepository, times(1)).saveAndFlush(myDeveloper);
        //Módosítás
        // - Nincs kitöltve az ID
        myDeveloper = new DeveloperEntity();
        assertThrows(
            ServiceException.class,
            () -> spiedDeveloperService.modifyDeveloper(myDeveloper),
            ServiceException.Exceptions.DEVELOPER_NOT_SAVED_CANT_MODIFY.getDescription()
        );
        // - Nincs kitöltve a név
        myDeveloper = spiedDeveloperService.getDeveloperById(5L);
        myDeveloper.setName("");
        assertThrows(
            ServiceException.class,
            () -> spiedDeveloperService.modifyDeveloper(myDeveloper),
            ServiceException.Exceptions.DEVELOPER_WITH_EMPTY_NAME_CANT_MODIFY.getDescription()
        );
        // - Ki van töltve a név, de van már ilyen.
        myDeveloper.setName("1. fejlesztő");
        //entityManager.detach(myDeveloper);
        assertThrows(
            ServiceException.class,
            () -> spiedDeveloperService.modifyDeveloper(myDeveloper),
            ServiceException.Exceptions.DEVELOPER_WITH_SAME_NAME_CANT_MODIFY.getDescription()
        );
        // - Ki van töltve a név, ugyanaz, ami volt: Nem hiba
        myDeveloper.setName("5. fejlesztő");
        assertDoesNotThrow(
            () -> spiedDeveloperService.modifyDeveloper(myDeveloper)
        );
        assertEquals("5. fejlesztő", spiedDeveloperService.getDeveloperById(5L).getName() );
        verify(spyDeveloperRepository, times(2)).saveAndFlush(any(DeveloperEntity.class));
        // - Ki van töltve a név, másra
        myDeveloper.setName("Ötödik fejlesztő");
        assertDoesNotThrow(
            () -> spiedDeveloperService.modifyDeveloper(myDeveloper)
        );
        assertEquals("Ötödik fejlesztő", spiedDeveloperService.getDeveloperById(5L).getName() );
        verify(spyDeveloperRepository, times(3)).saveAndFlush(any(DeveloperEntity.class));
        //Törlés
    }
}
