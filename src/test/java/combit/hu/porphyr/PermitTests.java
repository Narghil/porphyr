package combit.hu.porphyr;

import combit.hu.porphyr.config.RequestsConstants;
import combit.hu.porphyr.config.domain.PermitEntity;
import combit.hu.porphyr.config.repository.PermitRepository;
import combit.hu.porphyr.config.service.PermitService;
import combit.hu.porphyr.service.PorphyrServiceException;
import lombok.NonNull;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

//Permit-tel kapcsolatos adatbázis-módosító műveleteket a felhasználó nem indíthat,
// ezért a tesztelési esetek is egyszerűbbek.

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class PermitTests {
    private final @NonNull EntityManager entityManager;
    private final @NonNull PermitRepository spyPermitRepository;
    private final @NonNull PermitService spiedPermitService;

    @Autowired
    public PermitTests(
        final @NonNull EntityManager entityManager,
        final @NonNull PermitRepository permitRepository
    ) {
        this.entityManager = entityManager;
        this.spiedPermitService = new PermitService(permitRepository);
        this.spyPermitRepository = Mockito.mock(
            PermitRepository.class, AdditionalAnswers.delegatesTo(permitRepository)
        );
        this.spiedPermitService.setPermitRepository(this.spyPermitRepository);
        PorphyrServiceException.initExceptionsCounter();
    }

    @BeforeEach
    void setupEach() {
        entityManager.clear();
        Mockito.clearInvocations(spyPermitRepository);
    }

    //--------------------------- Repository műveletek tesztje -----------------------------
    @Test
    @Transactional
    @Rollback
    void permitRepositoryTest() {
        final @NonNull AtomicReference<List<PermitEntity>> permitsWrapper = new AtomicReference<>(new ArrayList<>());
        final @NonNull AtomicReference<PermitEntity> permitWrapper = new AtomicReference<>(new PermitEntity());
        //findAll()
        assertDoesNotThrow(() -> permitsWrapper.set(spyPermitRepository.findAll()));
        assertEquals(RequestsConstants.PERMITS.size(), permitsWrapper.get().size());
        //findAllById
        assertDoesNotThrow(() -> permitWrapper.set(spyPermitRepository.findAllById(1L)));
        assertNotNull(permitWrapper.get());
        //findAllByName
        assertDoesNotThrow(() -> permitWrapper.set(spyPermitRepository.findByName("root")));
        assertNotNull(permitWrapper.get());
        assertEquals("root", permitWrapper.get().getName());
        //saveAndFlush - existing permit
        permitWrapper.get().setUsable(false);
        assertDoesNotThrow(() -> spyPermitRepository.saveAndFlush(permitWrapper.get()));
        entityManager.clear();
        assertDoesNotThrow(() -> permitWrapper.set(spyPermitRepository.findByName("root")));
        assertEquals(false, permitWrapper.get().getUsable());
        entityManager.clear();
        //saveAndFlush - new permit
        permitWrapper.set(new PermitEntity("test", "Teszt"));
        assertDoesNotThrow(() -> spyPermitRepository.saveAndFlush(permitWrapper.get()));
        entityManager.clear();
        assertDoesNotThrow(() -> permitWrapper.set(spyPermitRepository.findByName("test")));
        assertEquals("Teszt", permitWrapper.get().getDescription());
    }

    //--------------------------- Service műveletek tesztje -----------------------------
    //--- Lekérdezések
    @Test
    @Transactional
    @Rollback
    void permitServiceQueriesTest() {
        final @NonNull AtomicReference<List<PermitEntity>> permitsWrapper = new AtomicReference<>(new ArrayList<>());
        final @NonNull AtomicReference<PermitEntity> permitWrapper = new AtomicReference<>(new PermitEntity());
        //getPermits
        assertDoesNotThrow(() -> permitsWrapper.set(spiedPermitService.getPermits()));
        assertEquals(RequestsConstants.PERMITS.size(), permitsWrapper.get().size());
        //getValidPermits
        assertDoesNotThrow(() -> permitsWrapper.set(spiedPermitService.getValidPermits()));
        assertEquals(RequestsConstants.PERMITS.size(), permitsWrapper.get().size());
        //getPermitById
        assertDoesNotThrow(() -> permitWrapper.set(spiedPermitService.getPermitById(1L)));
        assertNotNull(permitWrapper.get());
        //getPermitByName
        assertDoesNotThrow(() -> permitWrapper.set(spiedPermitService.getPermitByName("root")));
        assertNotNull(permitWrapper.get());
        assertEquals("root", permitWrapper.get().getName());
    }

    @Test
    @Transactional
    @Rollback
    void permitServiceTest() throws ExecutionException, InterruptedException {
        PermitEntity permitForInsert;
        PermitEntity actualPermit;
        //----------------------- Felvétel: insertNewPermit(); -----------------------------------------
        permitForInsert = new PermitEntity("insertTest", "Teszt");
        assertDoesNotThrow(() -> spiedPermitService.insertNewPermit(permitForInsert));
        //Visszaolvasás
        entityManager.clear();
        actualPermit = spiedPermitService.getPermitByName("insertTest");
        assertNotNull(actualPermit);
        assertEquals("insertTest", actualPermit.getName());
        //----------------------- Módosítás: modifyPermit(); -------------------------------------------
        entityManager.clear();
        PermitEntity permitForModify = spiedPermitService.getPermitByName("root");
        assertNotNull(permitForModify);
        permitForModify.setUsable(false);
        assertDoesNotThrow(() -> spiedPermitService.modifyPermit(permitForModify));
        //Visszaolvasás
        entityManager.clear();
        actualPermit = spiedPermitService.getPermitByName("root");
        assertNotNull(actualPermit);
        assertEquals(false, actualPermit.getUsable());
    }
}
