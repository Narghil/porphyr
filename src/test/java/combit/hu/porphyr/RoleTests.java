package combit.hu.porphyr;

import combit.hu.porphyr.domain.RoleEntity;
import combit.hu.porphyr.domain.UserEntity;
import combit.hu.porphyr.repository.RoleRepository;
import combit.hu.porphyr.service.PorphyrServiceException;
import combit.hu.porphyr.service.RoleService;
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

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static combit.hu.porphyr.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class RoleTests {
    private final @NonNull EntityManager entityManager;
    private final @NonNull RoleRepository spyRoleRepository;
    private final @NonNull RoleService spiedRoleService;

    @Autowired
    public RoleTests(
        final @NonNull EntityManager entityManager,
        final @NonNull RoleRepository roleRepository
    ) {
        this.entityManager = entityManager;
        this.spiedRoleService = new RoleService(this.entityManager, roleRepository);
        this.spyRoleRepository = Mockito.mock(
            RoleRepository.class, AdditionalAnswers.delegatesTo(roleRepository)
        );
        this.spiedRoleService.setRoleRepository(this.spyRoleRepository);
        this.spiedRoleService.setEntityManager(this.entityManager);
        PorphyrServiceException.initExceptionsCounter();
    }

    @BeforeEach
    void setupEach() {
        entityManager.clear();
        Mockito.clearInvocations(spyRoleRepository);
    }

    //------------------------------- Lekérdezések ---------------------------------------------
    @Test
    @Transactional
    @Rollback
    void roleEntityQueriesTest() {
        //Lekérdezés ID szerint ("USER", 1 felhasználóhoz rendelve)
        RoleEntity actualRoleEntity = spyRoleRepository.findAllById(1L);
        assertNotNull(actualRoleEntity);
        assertEquals(ROLE_NAMES[0], actualRoleEntity.getRole());
        assertArrayEquals(
            new String[]{LOGIN_NAMES[0]},
            actualRoleEntity.getUsers().stream().map(UserEntity::getLoginName).toArray(String[]::new)
        );
        //Lekérdezés név szerint ("ADMIN", 1 felhasználóhoz rendelve)
        entityManager.clear();
        actualRoleEntity = spyRoleRepository.findByRole(ROLE_NAMES[1]);
        assertNotNull(actualRoleEntity);
        assertEquals(ROLE_NAMES[1], actualRoleEntity.getRole());
        assertArrayEquals(
            new String[]{LOGIN_NAMES[1]},
            actualRoleEntity.getUsers().stream().map(UserEntity::getLoginName).toArray(String[]::new)
        );
    }

    @Test
    @Transactional
    @Rollback
    void roleServiceQueriesTest() throws ExecutionException, InterruptedException {
        //getRoles() --------------------------
        assertArrayEquals(
            ROLE_NAMES,
            spiedRoleService.getRoles().stream().map(RoleEntity::getRole)
                .sorted().toArray(String[]::new)
        );
        verify(spyRoleRepository, times(1)).findAll();
        //get Roles By Names
        assertArrayEquals(
            ROLE_NAMES,
            new String[]{
                Objects.requireNonNull(spiedRoleService.getRoleByRole(ROLE_NAMES[0])).getRole(),
                Objects.requireNonNull(spiedRoleService.getRoleByRole(ROLE_NAMES[1])).getRole(),
                Objects.requireNonNull(spiedRoleService.getRoleByRole(ROLE_NAMES[2])).getRole()
            }
        );
        verify(spyRoleRepository, times(3)).findByRole(anyString());
        //findAllByRoleAndIdNot
        assertEquals(0, spyRoleRepository.findAllByRoleAndIdNot(ROLE_NAMES[0], 1L).size());
        assertEquals(1, spyRoleRepository.findAllByRoleAndIdNot(ROLE_NAMES[0], 2L).size());
    }

    //--------------------------- Repository műveletek tesztje -----------------------------
    @Test
    @Transactional
    @Rollback
    void roleRepositoryTest() {
        final RoleEntity expectedRole = new RoleEntity();
        RoleEntity actualRole;
        //-------------------------------- Role felvétele --
        // - Már létező névvel (constraint ellenőrzése)
        expectedRole.setRole(ROLE_NAMES[0]);
        assertThrows(Exception.class, () -> spiedRoleService.insertNewRole(expectedRole));
        // - Még nem létező névvel
        expectedRole.setRole("NEW_ROLE");
        assertDoesNotThrow(() -> spyRoleRepository.saveAndFlush(expectedRole));
        assertNotNull(expectedRole.getId());
        // - Visszaolvasás
        entityManager.clear();
        actualRole = spyRoleRepository.findByRole("NEW_ROLE");
        assertEquals(expectedRole, actualRole);
        //--------------------------------- Role módosítása ---------------------------------
        entityManager.clear();
        final RoleEntity roleWithExistingName = spyRoleRepository.findByRole(ROLE_NAMES[0]);
        assertNotNull(roleWithExistingName);
        roleWithExistingName.setRole(ROLE_NAMES[1]);
        assertThrows(Exception.class, () -> spiedRoleService.modifyRole(roleWithExistingName));
        //
        entityManager.clear();
        final RoleEntity roleWithNewName = spyRoleRepository.findByRole("NEW_ROLE");
        assertNotNull(roleWithNewName);
        roleWithNewName.setRole("MODDED_ROLE");
        assertDoesNotThrow(() -> spyRoleRepository.saveAndFlush(roleWithNewName));
        // - Visszaolvasás
        entityManager.clear();
        actualRole = spyRoleRepository.findByRole("MODDED_ROLE");
        assertNotNull(actualRole);
        assertEquals(roleWithNewName, actualRole);
        // ----------------------------------- Role törlése --------------------------------
        entityManager.clear();
        actualRole = spyRoleRepository.findByRole("MODDED_ROLE");
        assertNotNull(actualRole);
        final Long finalRoleId = actualRole.getId();
        assertDoesNotThrow(() -> spyRoleRepository.deleteById(finalRoleId));
        entityManager.flush();
        // - Visszaolvasás
        entityManager.clear();
        actualRole = spyRoleRepository.findByRole("MODDED_ROLE");
        assertNull(actualRole);
    }

    //--------------------------- Service műveletek tesztje -----------------------------
    @Test
    @Transactional
    @Rollback
    void roleServiceInsertTest() throws ExecutionException, InterruptedException {
        Long roleId;
        RoleEntity roleForInsert;
        RoleEntity actualRole;
        //----------------------- Felvétel: insertNewRole(); -----------------------------------------
        roleForInsert = new RoleEntity();
        // - Már létező login névvel
        roleForInsert.setRole(ROLE_NAMES[0]);
        assertEquals(
            PorphyrServiceException.Exceptions.ROLES_INSERT_SAME_NAME.getDescription(),
            assertThrows(PorphyrServiceException.class, () -> spiedRoleService.insertNewRole(roleForInsert)
            ).getMessage()
        );
        // - Még nem létező névvel
        roleForInsert.setRole("NEW_ROLE");
        assertDoesNotThrow(() -> spiedRoleService.insertNewRole(roleForInsert));
        verify(spyRoleRepository, times(1)).saveAndFlush(roleForInsert);
        // - Visszaolvasás
        roleId = roleForInsert.getId();
        entityManager.clear();
        assertNotNull(roleId);
        actualRole = Objects.requireNonNull(spiedRoleService.getRoleByRole("NEW_ROLE"));
        assertNotNull(actualRole);
        // - Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> PorphyrServiceException.isAllExceptionsThrown(PorphyrServiceException.ExceptionGroups.ROLES_INSERT));
    }

    @Test
    @Transactional
    @Rollback
    void roleServiceModifyTest() throws ExecutionException, InterruptedException {
        Long roleId;
        final RoleEntity roleWithEmptyId = new RoleEntity();
        final RoleEntity roleWithAnyNames;
        RoleEntity actualRole;
        //------------------------------ Módosítás: modifyRole(); ----------------------------------------
        // - Nincs kitöltve az ID
        assertEquals(
            PorphyrServiceException.Exceptions.ROLES_MODIFY_NOT_SAVED.getDescription(),
            assertThrows(PorphyrServiceException.class, () -> spiedRoleService.modifyRole(roleWithEmptyId)
            ).getMessage()
        );
        // - Ki van töltve a login név, de van már ilyen.
        entityManager.clear();
        roleWithAnyNames = Objects.requireNonNull(spiedRoleService.getRoleByRole(ROLE_NAMES[0]));
        roleWithAnyNames.setRole(ROLE_NAMES[1]);
        assertEquals(
            PorphyrServiceException.Exceptions.ROLES_MODIFY_SAME_NAME.getDescription(),
            assertThrows(PorphyrServiceException.class, () -> spiedRoleService.modifyRole(roleWithAnyNames)
            ).getMessage()
        );
        // - Ki van töltve a név, ugyanaz, ami volt: Nem hiba
        roleWithAnyNames.setRole(ROLE_NAMES[0]);
        assertDoesNotThrow(
            () -> spiedRoleService.modifyRole(roleWithAnyNames)
        );
        entityManager.clear();
        // - Ki van töltve a név, másra
        roleWithAnyNames.setRole("NEW_ROLE");
        assertDoesNotThrow(
            () -> spiedRoleService.modifyRole(roleWithAnyNames)
        );
        roleId = roleWithAnyNames.getId();
        assertNotNull(roleId);
        actualRole = spiedRoleService.getRoleByRole("NEW_ROLE");
        assertNotNull(actualRole);
        verify(spyRoleRepository, times(2)).saveAndFlush(any(RoleEntity.class));
        // - Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> PorphyrServiceException.isAllExceptionsThrown(PorphyrServiceException.ExceptionGroups.ROLES_MODIFY));
    }

    @Test
    @Transactional
    @Rollback
    void roleServiceDeleteTest() throws ExecutionException, InterruptedException {
        // ---------------------------------- Törlés: deleteRole() ----------------------------------------
        // - Nincs kitöltve az ID
        final RoleEntity roleWithEmptyId = new RoleEntity();
        assertEquals(
            PorphyrServiceException.Exceptions.ROLES_DELETE_NOT_SAVED.getDescription(),
            assertThrows(PorphyrServiceException.class, () -> spiedRoleService.deleteRole(roleWithEmptyId)
            ).getMessage()
        );
        // - Ki van töltve az ID - de van hozzá user
        final RoleEntity roleWithUser = spiedRoleService.getRoleByRole(ROLE_NAMES[0]);
        assertNotNull(roleWithUser);
        assertEquals(
            PorphyrServiceException.Exceptions.ROLES_DELETE_ATTACHED_USERS.getDescription(),
            assertThrows(PorphyrServiceException.class, () -> spiedRoleService.deleteRole(roleWithUser)
            ).getMessage()
        );
        // - GUEST törlése - nincs hozzá user
        final RoleEntity roleWithoutUser = spiedRoleService.getRoleByRole(ROLE_NAMES[2]);
        assertNotNull(roleWithoutUser);
        assertDoesNotThrow(() -> spiedRoleService.deleteRole(roleWithoutUser));
        // - Visszaolvasás
        entityManager.clear();
        assertNull(spiedRoleService.getRoleByRole(ROLE_NAMES[2]));
        // - Minden hibalehetőség tesztelve volt:
        assertDoesNotThrow(() -> PorphyrServiceException.isAllExceptionsThrown(PorphyrServiceException.ExceptionGroups.ROLES_DELETE));
    }
}
