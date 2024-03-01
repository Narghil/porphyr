package combit.hu.porphyr;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.UserEntity;
import combit.hu.porphyr.repository.DeveloperRepository;
import combit.hu.porphyr.repository.UserRepository;
import combit.hu.porphyr.service.DeveloperService;
import combit.hu.porphyr.service.ServiceException;
import combit.hu.porphyr.service.UserService;
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
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static combit.hu.porphyr.TestConstants.loginNames;
import static combit.hu.porphyr.TestConstants.developerNames;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class UserDeveloperTests {
    @Autowired
    private @NonNull EntityManager entityManager;

    @Autowired
    private @NonNull UserRepository userRepository;

    @Autowired
    private @NonNull DeveloperRepository developerRepository;

    private UserRepository spyUserRepository;
    private UserService spiedUserService;
    private DeveloperRepository spyDeveloperRepository;
    private DeveloperService spiedDeveloperService;

    @BeforeAll
    void setupAll() {
        spiedUserService = new UserService( entityManager, userRepository);
        spyUserRepository = Mockito.mock(
            UserRepository.class, AdditionalAnswers.delegatesTo(userRepository)
        );
        spiedUserService.setUserRepository(spyUserRepository);
        spiedUserService.setEntityManager(entityManager);

        spiedDeveloperService = new DeveloperService( entityManager, developerRepository);
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
        Mockito.clearInvocations(spyUserRepository);
    }

    @Test
    @Transactional
    @Rollback
    void userDeveloperTests() throws ExecutionException, InterruptedException {
        final @NonNull String NEW_DEVELOPER = "5. fejlesztő";
        //Létező developer felvitele a user-hez
        final @NonNull UserEntity expectedUser =
            Objects.requireNonNull( spiedUserService.getUserByLoginName( loginNames[0] ))
        ;
        @NonNull DeveloperEntity newDeveloper = Objects.requireNonNull( spiedDeveloperService.getDeveloperByName( developerNames[1]));
        expectedUser.getDevelopers().add( newDeveloper );
        spyUserRepository.saveAndFlush(expectedUser);
        entityManager.clear();
        @NonNull UserEntity actualUser =
            Objects.requireNonNull( spiedUserService.getUserByLoginName( expectedUser.getLoginName() ))
        ;
        assertEquals( expectedUser, actualUser);
        assertArrayEquals(
            expectedUser.getDevelopers().stream().map( DeveloperEntity::getName ).sorted().toArray(),
            actualUser.getDevelopers().stream().map( DeveloperEntity::getName ).sorted().toArray()
        );
        //új developer felvitele a user-hez
        entityManager.clear();
        newDeveloper = new DeveloperEntity(NEW_DEVELOPER);
        expectedUser.getDevelopers().add( newDeveloper );
        spyDeveloperRepository.saveAndFlush( newDeveloper );
        spyUserRepository.saveAndFlush(expectedUser);
        entityManager.clear();
        actualUser = Objects.requireNonNull( spiedUserService.getUserByLoginName( expectedUser.getLoginName() ));
        assertEquals( expectedUser, actualUser);
        assertArrayEquals(
            expectedUser.getDevelopers().stream().map( DeveloperEntity::getName ).sorted().toArray(),
            actualUser.getDevelopers().stream().map( DeveloperEntity::getName ).sorted().toArray()
        );
        //Létező developer felvitele a user-hez, ami már van nála: Nem jön létre duplikáció
        entityManager.clear();
        newDeveloper = Objects.requireNonNull( spiedDeveloperService.getDeveloperByName( developerNames[0]));
        expectedUser.getDevelopers().add( newDeveloper );
        spyUserRepository.saveAndFlush(expectedUser);
        actualUser = Objects.requireNonNull( spiedUserService.getUserByLoginName( expectedUser.getLoginName() ));
        assertArrayEquals(
            actualUser.getDevelopers().stream().map( DeveloperEntity::getName ).sorted().toArray(),
            new String[]{ developerNames[0], developerNames[1], NEW_DEVELOPER }
        );
        //
        //developer-ok elvétele a user-től.
        entityManager.clear();
        expectedUser.setDevelopers( new HashSet<>() );
        spyUserRepository.saveAndFlush(expectedUser);
        entityManager.clear();
        actualUser = Objects.requireNonNull( spiedUserService.getUserByLoginName( expectedUser.getLoginName() ));
        assertEquals( 0, actualUser.getDevelopers().size() );
        //De eze nem befolyásolja a meglévő developer-ok számát.
        assertEquals( spiedDeveloperService.getDevelopers().size(), developerNames.length +1 );
    }
}
