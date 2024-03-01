package combit.hu.porphyr;

import combit.hu.porphyr.domain.RoleEntity;
import combit.hu.porphyr.domain.UserEntity;
import combit.hu.porphyr.repository.RoleRepository;
import combit.hu.porphyr.repository.UserRepository;
import combit.hu.porphyr.service.RoleService;
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

import static combit.hu.porphyr.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class UserRoleTests {
    @Autowired
    private @NonNull EntityManager entityManager;

    @Autowired
    private @NonNull UserRepository userRepository;

    @Autowired
    private @NonNull RoleRepository roleRepository;

    private UserRepository spyUserRepository;
    private UserService spiedUserService;
    private RoleRepository spyRoleRepository;
    private RoleService spiedRoleService;

    @BeforeAll
    void setupAll() {
        spiedUserService = new UserService( entityManager, userRepository);
        spyUserRepository = Mockito.mock(
            UserRepository.class, AdditionalAnswers.delegatesTo(userRepository)
        );
        spiedUserService.setUserRepository(spyUserRepository);
        spiedUserService.setEntityManager(entityManager);

        spiedRoleService = new RoleService( entityManager, roleRepository);
        spyRoleRepository = Mockito.mock(
            RoleRepository.class, AdditionalAnswers.delegatesTo(roleRepository)
        );
        spiedRoleService.setRoleRepository(spyRoleRepository);
        spiedRoleService.setEntityManager(entityManager);

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
    void userRoleTests() throws ExecutionException, InterruptedException {
        final @NonNull String NEW_ROLE = "4-NEW_ROLE";
        //Létező role felvitele a user-hez
        final @NonNull UserEntity expectedUser =
            Objects.requireNonNull( spiedUserService.getUserByLoginName( loginNames[0] ))
        ;
        @NonNull RoleEntity newRole = Objects.requireNonNull( spiedRoleService.getRoleByRole( roleNames[1]));
        expectedUser.getRoles().add( newRole );
        spyUserRepository.saveAndFlush(expectedUser);
        entityManager.clear();
        @NonNull UserEntity actualUser =
            Objects.requireNonNull( spiedUserService.getUserByLoginName( expectedUser.getLoginName() ))
        ;
        assertEquals( expectedUser, actualUser);
        assertArrayEquals(
            expectedUser.getRoles().stream().map( RoleEntity::getRole ).sorted().toArray(),
            actualUser.getRoles().stream().map( RoleEntity::getRole ).sorted().toArray()
        );
        //új role felvitele a user-hez
        entityManager.clear();
        newRole = new RoleEntity(NEW_ROLE);
        expectedUser.getRoles().add( newRole );
        spyRoleRepository.saveAndFlush( newRole );
        spyUserRepository.saveAndFlush(expectedUser);
        entityManager.clear();
        actualUser = Objects.requireNonNull( spiedUserService.getUserByLoginName( expectedUser.getLoginName() ));
        assertEquals( expectedUser, actualUser);
        assertArrayEquals(
            expectedUser.getRoles().stream().map( RoleEntity::getRole ).sorted().toArray(),
            actualUser.getRoles().stream().map( RoleEntity::getRole ).sorted().toArray()
        );
        //Létező role felvitele a user-hez, ami már van nála: Nem jön létre duplikáció
        entityManager.clear();
        newRole = Objects.requireNonNull( spiedRoleService.getRoleByRole( roleNames[0]));
        expectedUser.getRoles().add( newRole );
        spyUserRepository.saveAndFlush(expectedUser);
        actualUser = Objects.requireNonNull( spiedUserService.getUserByLoginName( expectedUser.getLoginName() ));
        assertArrayEquals(
            actualUser.getRoles().stream().map( RoleEntity::getRole ).sorted().toArray(),
            new String[]{ roleNames[0], roleNames[1], NEW_ROLE }
        );
        //
        //role-ok elvétele a user-től.
        entityManager.clear();
        expectedUser.setRoles( new HashSet<>() );
        spyUserRepository.saveAndFlush(expectedUser);
        entityManager.clear();
        actualUser = Objects.requireNonNull( spiedUserService.getUserByLoginName( expectedUser.getLoginName() ));
        assertEquals( 0, actualUser.getRoles().size() );
        //De eze nem befolyásolja a meglévő role-ok számát.
        assertEquals( spiedRoleService.getRoles().size(), roleNames.length +1 );
    }
}
