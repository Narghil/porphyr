package combit.hu.porphyr;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.RoleEntity;
import combit.hu.porphyr.domain.UserEntity;
import combit.hu.porphyr.repository.DeveloperRepository;
import combit.hu.porphyr.repository.UserRepository;
import combit.hu.porphyr.service.DeveloperService;
import combit.hu.porphyr.service.UserService;
import combit.hu.porphyr.service.PorphyrServiceException;
import static combit.hu.porphyr.TestConstants.*;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class UserTests {
    private final @NonNull EntityManager entityManager;

    private final @NonNull UserRepository spyUserRepository;
    private final @NonNull UserService spiedUserService;
    private final @NonNull DeveloperService developerService;

    @Autowired
    public UserTests(
        final @NonNull EntityManager entityManager,
        final @NonNull UserRepository userRepository,
        final @NonNull DeveloperRepository developerRepository
    ) {
        this.entityManager = entityManager;
        this.spiedUserService = new UserService( this.entityManager, userRepository);
        this.spyUserRepository = Mockito.mock(
            UserRepository.class, AdditionalAnswers.delegatesTo( userRepository)
        );
        this.spiedUserService.setUserRepository(this.spyUserRepository);
        this.spiedUserService.setEntityManager(this.entityManager);

        this.developerService = new DeveloperService( this.entityManager, developerRepository );

        PorphyrServiceException.initExceptionsCounter();
    }

    @BeforeEach
    void setupEach() {
        entityManager.clear();
        Mockito.clearInvocations(spyUserRepository);
    }

    //------------------------------- Lekérdezések ---------------------------------------------
    @Test
    @Transactional
    @Rollback
    //OneToMany kapcsolatok lekérdezésének ellenőrzése
    void userEntityQueriesTest() {
        //Roles
        assertArrayEquals(
            new String[]{ROLE_NAMES[1]},
            Objects.requireNonNull( spyUserRepository.findByLoginName(LOGIN_NAMES[1]))
                .getRoles()
                .stream().map(RoleEntity::getRole).toArray(String[]::new)
        );
        //Developers
        assertArrayEquals(
            DEVELOPER_NAMES,
            Objects.requireNonNull(spyUserRepository.findByLoginName(LOGIN_NAMES[1]))
                .getDevelopers().stream().map(DeveloperEntity::getName).sorted()
                .toArray(String[]::new)
        );
    }

    @Test
    @Transactional
    @Rollback
    void userServiceQueriesTest() throws ExecutionException, InterruptedException {
        @NonNull List<String> actualUserPermitNames = new ArrayList<>();
        @NonNull List<String> actualUserPermittedRequestCalls = new ArrayList<>();
        @NonNull List<DeveloperEntity> actualUserDevelopers = new ArrayList<>();

        @NonNull List<DeveloperEntity> requestedUserDevelopers =
            Collections.singletonList(developerService.getDeveloperById(1L)) ;
        @NonNull List<DeveloperEntity> requestedAdminDevelopers =
            Arrays.asList(
                developerService.getDeveloperById(1L), developerService.getDeveloperById(2L),
                developerService.getDeveloperById(3L), developerService.getDeveloperById(4L)
            );
        //getUsers() --------------------------
        assertArrayEquals(
            LOGIN_NAMES,
            spiedUserService.getUsers().stream().map(UserEntity::getLoginName).toArray(String[]::new)
        );
        verify(spyUserRepository, times(1)).findAll();
        //getUserByLoginNames
        assertArrayEquals(
            USER_FULL_NAMES,
            new String[]{
                Objects.requireNonNull(spiedUserService.getUserByLoginName(LOGIN_NAMES[0])).getFullName(),
                Objects.requireNonNull(spiedUserService.getUserByLoginName(LOGIN_NAMES[1])).getFullName()
            }
        );
        verify( spyUserRepository, times(2)).findByLoginName(anyString());
        //getUserPermits() : "user" user
        UserEntity user = spiedUserService.getUserByLoginName( LOGIN_NAMES[0] );
        assertNotNull( user );
        assertTrue( spiedUserService.getUserPermits( user, actualUserPermitNames, actualUserPermittedRequestCalls, actualUserDevelopers ) );
        assertEquals( 23, actualUserPermitNames.size());
        assertArrayEquals(
            requestedUserDevelopers.stream().map(DeveloperEntity::getName).sorted().toArray(),
            actualUserDevelopers.stream().map(DeveloperEntity::getName).sorted().toArray()
        );
        //getUserPermits() : "admin" user
        user = spiedUserService.getUserByLoginName( LOGIN_NAMES[1] );
        assertNotNull( user );
        assertTrue( spiedUserService.getUserPermits( user, actualUserPermitNames, actualUserPermittedRequestCalls, actualUserDevelopers ) );
        assertEquals( 24, actualUserPermitNames.size() );
        assertArrayEquals(
            requestedAdminDevelopers.stream().map(DeveloperEntity::getName).sorted().toArray(),
            actualUserDevelopers.stream().map(DeveloperEntity::getName).sorted().toArray()
        );
    }

    //--------------------------- Repository műveletek tesztje -----------------------------
    @Test
    @Transactional
    @Rollback
    void userRepositoryTest() {
        final @NonNull String JOHN_SMITH = "john_smith";
        final @NonNull String MODDED_USER = "modded_user";
        final UserEntity expectedUser = new UserEntity();
        expectedUser.setFullName("John Smith");
        expectedUser.setPassword("{noop}password");
        expectedUser.setEmail("none@email.org");
        UserEntity actualUser;
        //-------------------------------- User felvétele --
        // - Már létező névvel (constraint ellenőrzése)
        expectedUser.setLoginName("user");
        assertThrows(Exception.class, () -> spiedUserService.insertNewUser(expectedUser));
        // - Még nem létező névvel
        expectedUser.setLoginName(JOHN_SMITH);
        assertDoesNotThrow(() -> spyUserRepository.saveAndFlush(expectedUser));
        assertNotNull( expectedUser.getId() );
        // - Visszaolvasás
        entityManager.clear();
        actualUser = spyUserRepository.findByLoginName(JOHN_SMITH);
        assertEquals(expectedUser, actualUser);
        //--------------------------------- User módosítása ---------------------------------
        entityManager.clear();
        final UserEntity userWithExistingLoginName = spyUserRepository.findByLoginName(JOHN_SMITH);
        assertNotNull(userWithExistingLoginName);
        userWithExistingLoginName.setLoginName("user");
        assertThrows(Exception.class, () -> spiedUserService.modifyUser(userWithExistingLoginName));
        //
        entityManager.clear();
        final UserEntity userWithNewLoginName =  spyUserRepository.findByLoginName(JOHN_SMITH);
        assertNotNull(userWithNewLoginName);
        userWithNewLoginName.setLoginName(MODDED_USER);
        assertDoesNotThrow(() -> spyUserRepository.saveAndFlush(userWithNewLoginName));
        // - Visszaolvasás
        entityManager.clear();
        actualUser = spyUserRepository.findByLoginName(MODDED_USER);
        assertNotNull(actualUser);
        assertEquals(userWithNewLoginName, actualUser);
        // ----------------------------------- User törlése --------------------------------
        entityManager.clear();
        actualUser = spyUserRepository.findByLoginName("admin");
        assertNotNull(actualUser);
        Long userId = Objects.requireNonNull(actualUser.getId());
        assertDoesNotThrow(() -> spyUserRepository.deleteById(userId));
        entityManager.flush();
        entityManager.clear();
        // - Visszaolvasás
        actualUser = spyUserRepository.findAllById(userId);
        assertNull(actualUser);
    }

    //--------------------------- Service műveletek tesztje -----------------------------
    @Test
    @Transactional
    @Rollback
    void userServiceInsertTest() throws ExecutionException, InterruptedException {
        Long userId;
        UserEntity userForInsert;
        UserEntity actualUser;
        final @NonNull String NEW_USER = "new_user";
        //----------------------- Felvétel: insertNewUser(); -----------------------------------------
        userForInsert = new UserEntity();
        // - Már létező login névvel
        userForInsert.setLoginName(LOGIN_NAMES[0]);
        assertEquals(
            PorphyrServiceException.Exceptions.USER_INSERT_SAME_LOGIN_NAME.getDescription(),
            assertThrows(PorphyrServiceException.class, () -> spiedUserService.insertNewUser(userForInsert)
            ).getMessage()
        );
        // - Még nem létező névvel
        userForInsert.setLoginName(NEW_USER);
        assertDoesNotThrow(() -> spiedUserService.insertNewUser(userForInsert));
        verify(spyUserRepository, times(1)).saveAndFlush(userForInsert);
        // - Visszaolvasás
        userId = userForInsert.getId();
        entityManager.clear();
        assertNotNull(userId);
        actualUser = Objects.requireNonNull(spiedUserService.getUserByLoginName(NEW_USER));
        assertNotNull(actualUser);
        // - Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> PorphyrServiceException.isAllExceptionsThrown(PorphyrServiceException.ExceptionGroups.USERS_INSERT));
    }

    @Test
    @Transactional
    @Rollback
    void userServiceModifyTest() throws ExecutionException, InterruptedException {
        final @NonNull String NEW_LOGIN_NAME = "new_login_name";
        Long userId;
        final UserEntity userWithEmptyId = new UserEntity();
        final UserEntity userWithAnyNames;
        UserEntity actualUser;
        //------------------------------ Módosítás: modifyUser(); ----------------------------------------
        // - Nincs kitöltve az ID
        assertEquals(
            PorphyrServiceException.Exceptions.USER_MODIFY_NOT_SAVED.getDescription(),
            assertThrows(PorphyrServiceException.class, () -> spiedUserService.modifyUser(userWithEmptyId)
            ).getMessage()
        );
        // - Ki van töltve a login név, de van már ilyen.
        userWithAnyNames = Objects.requireNonNull(spiedUserService.getUserByLoginName(LOGIN_NAMES[0]));
        userWithAnyNames.setLoginName(LOGIN_NAMES[1]);
        assertEquals(
            PorphyrServiceException.Exceptions.USER_MODIFY_SAME_LOGIN_NAME.getDescription(),
            assertThrows(PorphyrServiceException.class, () -> spiedUserService.modifyUser(userWithAnyNames)
            ).getMessage()
        );
        // - Ki van töltve a név, ugyanaz, ami volt: Nem hiba
        userWithAnyNames.setLoginName(LOGIN_NAMES[0]);
        assertDoesNotThrow(
            () -> spiedUserService.modifyUser(userWithAnyNames)
        );
        entityManager.clear();
        // - Ki van töltve a név, másra
        userWithAnyNames.setLoginName(NEW_LOGIN_NAME);
        assertDoesNotThrow(
            () -> spiedUserService.modifyUser(userWithAnyNames)
        );
        userId = userWithAnyNames.getId();
        assertNotNull(userId);
        actualUser = spiedUserService.getUserByLoginName(NEW_LOGIN_NAME);
        assertNotNull(actualUser);
        verify(spyUserRepository, times(2)).saveAndFlush(any(UserEntity.class));
        // - Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> PorphyrServiceException.isAllExceptionsThrown(PorphyrServiceException.ExceptionGroups.USERS_MODIFY));
    }

    @Test
    @Transactional
    @Rollback
    void userServiceDeleteTest() throws ExecutionException, InterruptedException {
        final UserEntity userWithEmptyId;
        UserEntity actualUser;
        // ---------------------------------- Törlés: deleteUser() ----------------------------------------
        // - Nincs kitöltve az ID
        userWithEmptyId = new UserEntity();
        assertEquals(
            PorphyrServiceException.Exceptions.USER_DELETE_NOT_SAVED.getDescription(),
            assertThrows(PorphyrServiceException.class, () -> spiedUserService.deleteUser(userWithEmptyId)
            ).getMessage()
        );
        // - Ki van töltve az ID
        actualUser = spiedUserService.getUserByLoginName(LOGIN_NAMES[0]);
        assertNotNull(actualUser);
        assertDoesNotThrow(
            () -> spiedUserService.deleteUser(actualUser)
        );
        // - Visszaolvasás
        entityManager.clear();
        assertNull(spiedUserService.getUserByLoginName(LOGIN_NAMES[0]));
        // - Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> PorphyrServiceException.isAllExceptionsThrown(PorphyrServiceException.ExceptionGroups.USERS_DELETE));
    }
}
