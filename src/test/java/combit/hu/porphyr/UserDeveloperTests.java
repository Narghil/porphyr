package combit.hu.porphyr;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.config.domain.UserEntity;
import combit.hu.porphyr.repository.DeveloperRepository;
import combit.hu.porphyr.config.repository.UserRepository;
import combit.hu.porphyr.service.DeveloperService;
import combit.hu.porphyr.service.PorphyrServiceException;
import combit.hu.porphyr.config.service.UserService;
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
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static combit.hu.porphyr.TestConstants.LOGIN_NAMES;
import static combit.hu.porphyr.TestConstants.DEVELOPER_NAMES;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class UserDeveloperTests {
    private final @NonNull EntityManager entityManager;

    private final @NonNull UserRepository spyUserRepository;
    private final @NonNull UserService spiedUserService;
    private final @NonNull DeveloperRepository spyDeveloperRepository;
    private final @NonNull DeveloperService spiedDeveloperService;

    @Autowired
    public UserDeveloperTests(
        final @NonNull EntityManager entityManager,
        final @NonNull UserRepository userRepository,
        final @NonNull DeveloperRepository developerRepository
    ) {
        this.entityManager = entityManager;
        this.spiedUserService = new UserService(this.entityManager, userRepository);
        this.spyUserRepository = Mockito.mock(
            UserRepository.class, AdditionalAnswers.delegatesTo(userRepository)
        );
        this.spiedUserService.setUserRepository(this.spyUserRepository);
        this.spiedUserService.setEntityManager(this.entityManager);

        this.spiedDeveloperService = new DeveloperService(this.entityManager, developerRepository);
        this.spyDeveloperRepository = Mockito.mock(
            DeveloperRepository.class, AdditionalAnswers.delegatesTo(developerRepository)
        );
        this.spiedDeveloperService.setDeveloperRepository(this.spyDeveloperRepository);
        this.spiedDeveloperService.setEntityManager(this.entityManager);

        PorphyrServiceException.initExceptionsCounter();
    }

    @BeforeEach
    void setupEach() {
        entityManager.clear();
        Mockito.clearInvocations(spyUserRepository);
    }

    @Test
    @Transactional
    @Rollback
    void userDeveloperTests() throws ExecutionException, InterruptedException {
        final @NonNull String NEW_DEVELOPER = "5. fejlesztő";
        //Létező developer felvitele a user-hez
        final @NonNull UserEntity expectedUser =
            Objects.requireNonNull(spiedUserService.getUserByLoginName(LOGIN_NAMES[0]));
        @NonNull
        DeveloperEntity newDeveloper = Objects.requireNonNull(spiedDeveloperService.getDeveloperByName(DEVELOPER_NAMES[1]));
        expectedUser.getDevelopers().add(newDeveloper);
        spyUserRepository.saveAndFlush(expectedUser);
        entityManager.clear();
        @NonNull
        UserEntity actualUser =
            Objects.requireNonNull(spiedUserService.getUserByLoginName(expectedUser.getLoginName()));
        assertEquals(expectedUser, actualUser);
        assertArrayEquals(
            expectedUser.getDevelopers().stream().map(DeveloperEntity::getName).sorted().toArray(),
            actualUser.getDevelopers().stream().map(DeveloperEntity::getName).sorted().toArray()
        );
        //új developer felvitele a user-hez
        entityManager.clear();
        newDeveloper = new DeveloperEntity(NEW_DEVELOPER);
        expectedUser.getDevelopers().add(newDeveloper);
        spyDeveloperRepository.saveAndFlush(newDeveloper);
        spyUserRepository.saveAndFlush(expectedUser);
        entityManager.clear();
        actualUser = Objects.requireNonNull(spiedUserService.getUserByLoginName(expectedUser.getLoginName()));
        assertEquals(expectedUser, actualUser);
        assertArrayEquals(
            expectedUser.getDevelopers().stream().map(DeveloperEntity::getName).sorted().toArray(),
            actualUser.getDevelopers().stream().map(DeveloperEntity::getName).sorted().toArray()
        );
        //Létező developer felvitele a user-hez, ami már van nála: Nem jön létre duplikáció
        entityManager.clear();
        newDeveloper = Objects.requireNonNull(spiedDeveloperService.getDeveloperByName(DEVELOPER_NAMES[0]));
        expectedUser.getDevelopers().add(newDeveloper);
        spyUserRepository.saveAndFlush(expectedUser);
        actualUser = Objects.requireNonNull(spiedUserService.getUserByLoginName(expectedUser.getLoginName()));
        assertArrayEquals(
            actualUser.getDevelopers().stream().map(DeveloperEntity::getName).sorted().toArray(),
            new String[]{DEVELOPER_NAMES[0], DEVELOPER_NAMES[1], NEW_DEVELOPER}
        );
        //
        //developer-ok elvétele a user-től.
        entityManager.clear();
        expectedUser.setDevelopers(new HashSet<>());
        spyUserRepository.saveAndFlush(expectedUser);
        entityManager.clear();
        actualUser = Objects.requireNonNull(spiedUserService.getUserByLoginName(expectedUser.getLoginName()));
        assertEquals(0, actualUser.getDevelopers().size());
        //De eze nem befolyásolja a meglévő developer-ok számát.
        assertEquals(spiedDeveloperService.getDevelopers().size(), DEVELOPER_NAMES.length + 1);
    }
}
