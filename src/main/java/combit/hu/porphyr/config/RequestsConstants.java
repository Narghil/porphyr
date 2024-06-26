package combit.hu.porphyr.config;

import lombok.NonNull;
import lombok.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Value
public class RequestsConstants {

    public static final @NonNull String PERMIT_ROOT = "root";
    public static final @NonNull String PERMIT_LOGOUT = "logout";
    public static final @NonNull String PERMIT_ERROR = "error";
    public static final @NonNull String PERMIT_ALL = "permit_all";
    public static final @NonNull String PERMIT_PROJECTS = "projects";
    public static final @NonNull String PERMIT_PROJECT_NEW = "newProject";
    public static final @NonNull String PERMIT_PROJECT_DELETE = "deleteProject";
    public static final @NonNull String PERMIT_PROJECT_MODIFY = "modifyProject";
    public static final @NonNull String PERMIT_PROJECT_DEVELOPERS = "projectDevelopers";
    public static final @NonNull String PERMIT_PROJECT_DEVELOPER_NEW = "newProjectDeveloper";
    public static final @NonNull String PERMIT_PROJECT_DEVELOPER_TASKS = "projectDeveloperTasks";
    public static final @NonNull String PERMIT_PROJECT_DEVELOPER_TASK_NEW = "newProjectDeveloperTask";
    public static final @NonNull String PERMIT_PROJECT_TASKS = "projectTasks";
    public static final @NonNull String PERMIT_PROJECT_TASK_NEW = "newProjectTask";
    public static final @NonNull String PERMIT_PROJECT_TASK_MODIFY = "modifyProjectTask";
    public static final @NonNull String PERMIT_PROJECT_TASK_DEVELOPER_NEW = "newProjectTaskDeveloper";
    public static final @NonNull String PERMIT_PROJECT_TASK_DEVELOPERS = "projectTaskDevelopers";
    public static final @NonNull String PERMIT_PROJECT_TASK_DEVELOPER_DELETE = "deleteProjectTaskDeveloper";
    public static final @NonNull String PERMIT_PROJECT_TASK_POSTS = "projectTaskPosts";
    public static final @NonNull String PERMIT_DEVELOPERS = "developers";
    public static final @NonNull String PERMIT_DEVELOPER_NEW = "newDeveloper";
    public static final @NonNull String PERMIT_DEVELOPER_MODIFY = "modifyDeveloper";
    public static final @NonNull String PERMIT_DEVELOPER_TASKS = "developerTasks";
    public static final @NonNull String PERMIT_DBCONSOLE = "dbConsole";
    public static final @NonNull String PERMIT_DEVELOPER_RAW_LIST = "rawDeveloperList";
    public static final @NonNull String PERMIT_RIGHTS = "rights";

    public static final @NonNull Map<String, String> PERMITS;

    static {
        final Map<String, String> tmpMap = new HashMap<>();
        tmpMap.put(PERMIT_ROOT, "Főmenü");
        tmpMap.put(PERMIT_LOGOUT, "Kilépés");
        tmpMap.put(PERMIT_ERROR, "Hibák megjelenítése");
        tmpMap.put(PERMIT_ALL, "Minden");
        tmpMap.put(PERMIT_PROJECTS, "Projektek");
        tmpMap.put(PERMIT_PROJECT_NEW, "Új projekt felvitele");
        tmpMap.put(PERMIT_PROJECT_DELETE, "Projekt törlése");
        tmpMap.put(PERMIT_PROJECT_MODIFY, "Projekt módosítása");
        tmpMap.put(PERMIT_PROJECT_DEVELOPERS, "Projekt fejlesztői");
        tmpMap.put(PERMIT_PROJECT_DEVELOPER_NEW, "Fejlesztő hozzárendelése a projekthez");
        tmpMap.put(PERMIT_PROJECT_DEVELOPER_TASKS, "A projekt fejlesztőinek feladatai");
        tmpMap.put(PERMIT_PROJECT_DEVELOPER_TASK_NEW, "Feladat hozzárendelése a projekt fejlesztőihez");
        tmpMap.put(PERMIT_PROJECT_TASKS, "Projekt feladatai");
        tmpMap.put(PERMIT_PROJECT_TASK_NEW, "Új feladat felvitele a projekthez");
        tmpMap.put(PERMIT_PROJECT_TASK_MODIFY, "Projekt feladatainak módosítása");
        tmpMap.put(PERMIT_PROJECT_TASK_DEVELOPERS, "A projekt feladatain dolgozó fejlesztők");
        tmpMap.put(PERMIT_PROJECT_TASK_DEVELOPER_NEW, "Új fejlesztő hozzárendelése a projekt feladataihoz");
        tmpMap.put(PERMIT_PROJECT_TASK_DEVELOPER_DELETE, "Fejlesztő kivonása a projekt feladataiból");
        tmpMap.put(PERMIT_PROJECT_TASK_POSTS, "A projekt feladataihoz fűzött megjegyzések kezelése");
        tmpMap.put(PERMIT_DEVELOPERS, "Fejlesztők");
        tmpMap.put(PERMIT_DEVELOPER_NEW, "Új fejlesztő felvitele");
        tmpMap.put(PERMIT_DEVELOPER_MODIFY, "Fejlesztők módosítása");
        tmpMap.put(PERMIT_DEVELOPER_TASKS, "Fejlesztők feladatai");
        tmpMap.put(PERMIT_DBCONSOLE, "Adatbázis konzol");
        tmpMap.put(PERMIT_DEVELOPER_RAW_LIST, "Fejlesztők nyers listája, teszteléshez");
        tmpMap.put(PERMIT_RIGHTS, "Jogosultságok kezelése");
        PERMITS = Collections.unmodifiableMap(tmpMap);
    }

    public static final @NonNull String CALL_RAW_DEVELOPER_LIST = "/devs";
    public static final @NonNull String CALL_SELECT_DEVELOPER_OPERATION = "/selectDeveloperOperation";
    public static final @NonNull String CALL_START_NEW_DEVELOPER = "/developers_new_start";
    public static final @NonNull String CALL_NEW_DEVELOPER = "/developer_new";
    public static final @NonNull String CALL_INSERT_NEW_DEVELOPER = "/insertNewDeveloper";
    public static final @NonNull String CALL_START_MODIFY_DEVELOPER = "/developer_modify";
    public static final @NonNull String CALL_MODIFY_DEVELOPER = "/modifyDeveloper";
    public static final @NonNull String CALL_DEVELOPER_TASKS = "/developer_tasks";
    public static final @NonNull String CALL_SELECT_DEVELOPER_TASK_OPERATION = "/selectDeveloperTaskOperation";
    public static final @NonNull String CALL_ERROR = "/error";
    public static final @NonNull String CALL_ROOT = "/";
    public static final @NonNull String CALL_DBCONSOLE = "/dbConsole";
    public static final @NonNull String CALL_DBCONSOLE_ALIAS = "/porphyr";
    public static final @NonNull String CALL_DBCONSOLE_ROOT = "/porphyr/";
    public static final @NonNull String CALL_PROJECTS = "/projects";
    public static final @NonNull String CALL_DEVELOPERS = "/developers";
    public static final @NonNull String CALL_SELECT_PROJECT_OPERATION = "/selectProjectOperation";
    public static final @NonNull String CALL_START_NEW_PROJECT = "/project_new_start";
    public static final @NonNull String CALL_NEW_PROJECT = "/project_new";
    public static final @NonNull String CALL_INSERT_NEW_PROJECT = "/insertNewProject";
    public static final @NonNull String CALL_DELETE_PROJECT = "/project_delete";
    public static final @NonNull String CALL_START_MODIFY_PROJECT = "/project_modify";
    public static final @NonNull String CALL_MODIFY_PROJECT = "/modifyProject";
    public static final @NonNull String CALL_SELECT_PROJECT_DEVELOPER_OPERATION = "/selectProjectDeveloperOperation";
    public static final @NonNull String CALL_PROJECT_DEVELOPERS = "/project_developers";
    public static final @NonNull String CALL_START_NEW_PROJECT_DEVELOPER = "/project_developers_new";
    public static final @NonNull String CALL_INSERT_NEW_PROJECT_DEVELOPER = "/insertNewProjectDeveloper";
    public static final @NonNull String CALL_SELECT_PROJECT_DEVELOPER_TASK_OPERATION = "/selectProjectDeveloperTaskOperation";
    public static final @NonNull String CALL_PROJECT_DEVELOPER_TASKS = "/project_developers_tasks";
    public static final @NonNull String CALL_START_NEW_PROJECT_DEVELOPER_TASK = "/project_developers_tasks_new";
    public static final @NonNull String CALL_INSERT_NEW_PROJECT_TASK_DEVELOPER = "/insertNewProjectTaskDeveloper";
    public static final @NonNull String CALL_PROJECT_TASKS = "/project_tasks";
    public static final @NonNull String CALL_SELECT_PROJECT_TASK_OPERATION = "/selectProjectTaskOperation";
    public static final @NonNull String CALL_PROJECT_TASK_DEVELOPERS = "/project_tasks_developers";
    public static final @NonNull String CALL_DELETE_PROJECT_TASK_DEVELOPER = "/deleteProjectTaskDeveloper";
    public static final @NonNull String CALL_START_NEW_PROJECT_TASK = "/projecttask_new_start";
    public static final @NonNull String CALL_NEW_PROJECT_TASK = "/project_tasks_new";
    public static final @NonNull String CALL_INSERT_NEW_PROJECT_TASK = "/insertNewProjectTask";
    public static final @NonNull String CALL_START_MODIFY_PROJECT_TASK = "/project_tasks_modify";
    public static final @NonNull String CALL_MODIFY_PROJECT_TASK = "/modifyProjectTask";
    public static final @NonNull String CALL_SELECT_PROJECT_TASKS_DEVELOPER_OPERATION = "/selectProjectTasksDeveloperOperation";
    public static final @NonNull String CALL_START_NEW_PROJECT_TASK_DEVELOPER = "/project_tasks_developers_new";
    public static final @NonNull String CALL_NEW_PROJECT_TASK_DEVELOPER = "/newProjectTaskDeveloper";
    public static final @NonNull String CALL_PROJECT_TASK_POSTS = "/project_tasks_posts";
    public static final @NonNull String CALL_SELECT_PROJECT_TASK_POSTS_OPERATION = "/selectProjectTaskPostOperation";
    public static final @NonNull String CALL_LOGOUT_CONFIRM = "/confirm_logout";
    public static final @NonNull String CALL_RIGHTS = "/rights";
    public static final @NonNull String CALL_RIGHTS_USERS = "/rights/users";
    public static final @NonNull String CALL_START_NEW_USER = "/startNewUser";
    public static final @NonNull String CALL_START_MODIFY_USER = "/startModifyUser/\\d*";
    public static final @NonNull String CALL_START_DELETE_USER = "/startDeleteUser/\\d*";
    public static final @NonNull String CALL_USER_INPUT_DATA_PROCESSING = "/userInputDataProcessing";
    public static final @NonNull String CALL_RIGHTS_ROLES = "/rights/roles";
    public static final @NonNull String CALL_START_NEW_ROLE = "/startNewRole";
    public static final @NonNull String CALL_START_MODIFY_ROLE = "/startModifyRole/\\d*";
    public static final @NonNull String CALL_START_DELETE_ROLE = "/startDeleteRole/\\d*";
    public static final @NonNull String CALL_ROLE_INPUT_DATA_PROCESSING = "/roleInputDataProcessing";
    public static final @NonNull String CALL_START_USER_ROLES = "/startUserRoles/\\d*";
    public static final @NonNull String CALL_RIGHTS_USER_ROLES = "/rights/user_roles";
    public static final @NonNull String CALL_START_DELETE_USER_ROLE = "/startDeleteUserRole/\\d*/\\d*";
    public static final @NonNull String CALL_START_NEW_USER_ROLE = "/startNewUserRole/\\d*";
    public static final @NonNull String CALL_USER_ROLE_NEW = "/rights/user_role_new";
    public static final @NonNull String CALL_USER_ROLE_NEW_DATA_PROCESSING = "/newUserRoleDataProcessing";
    public static final @NonNull String CALL_START_USER_DEVELOPERS = "/startUserDevelopers/\\d*";
    public static final @NonNull String CALL_RIGHTS_USER_DEVELOPERS = "/rights/user_developers";
    public static final @NonNull String CALL_START_DELETE_USER_DEVELOPER = "/startDeleteUserDeveloper/\\d*/\\d*";
    public static final @NonNull String CALL_START_NEW_USER_DEVELOPER = "/startNewUserDeveloper/\\d*";
    public static final @NonNull String CALL_USER_DEVELOPER_NEW = "/rights/user_developer_new";
    public static final @NonNull String CALL_USER_DEVELOPER_NEW_DATA_PROCESSING = "/newUserDeveloperDataProcessing";
    public static final @NonNull String CALL_RIGHTS_PERMITS = "/rights/permits";
    public static final @NonNull String CALL_START_ROLE_PERMITS = "/startRolePermits/\\d*";
    public static final @NonNull String CALL_ROLE_PERMITS = "/rights/role_permits";
    public static final @NonNull String CALL_START_NEW_ROLE_PERMIT = "/startNewRolePermit/\\d*";
    public static final @NonNull String CALL_INSERT_NEW_ROLE_PERMIT = "/insertNewRolePermit";
    public static final @NonNull String CALL_START_DELETE_ROLE_PERMIT = "/startDeleteRolePermit/\\d*/\\d*";

    public static final @NonNull Map<String, List<String>> PROTECTED_REQUEST_CALLS;

    static {
        final Map<String, List<String>> tmpMap = new HashMap<>();
        tmpMap.put(PERMIT_ALL, new ArrayList<>(Collections.singletonList("It will changed to all calls")));
        tmpMap.put(PERMIT_ROOT, new ArrayList<>(Collections.singletonList(CALL_ROOT)));
        tmpMap.put(PERMIT_PROJECTS, new ArrayList<>(Arrays.asList(CALL_PROJECTS, CALL_SELECT_PROJECT_OPERATION)));
        tmpMap.put(
            PERMIT_PROJECT_NEW,
            new ArrayList<>(Arrays.asList(CALL_START_NEW_PROJECT, CALL_NEW_PROJECT, CALL_INSERT_NEW_PROJECT))
        );
        tmpMap.put(PERMIT_PROJECT_DELETE, new ArrayList<>(Collections.singletonList(CALL_DELETE_PROJECT)));
        tmpMap.put(
            PERMIT_PROJECT_MODIFY,
            new ArrayList<>(Arrays.asList(CALL_START_MODIFY_PROJECT, CALL_MODIFY_PROJECT))
        );
        tmpMap.put(
            PERMIT_PROJECT_DEVELOPERS,
            new ArrayList<>(Arrays.asList(CALL_PROJECT_DEVELOPERS, CALL_SELECT_PROJECT_DEVELOPER_OPERATION))
        );
        tmpMap.put(
            PERMIT_PROJECT_DEVELOPER_NEW,
            new ArrayList<>(Arrays.asList(CALL_START_NEW_PROJECT_DEVELOPER, CALL_INSERT_NEW_PROJECT_DEVELOPER))
        );
        tmpMap.put(
            PERMIT_PROJECT_DEVELOPER_TASKS,
            new ArrayList<>(Arrays.asList(CALL_PROJECT_DEVELOPER_TASKS, CALL_SELECT_PROJECT_DEVELOPER_TASK_OPERATION))
        );
        tmpMap.put(
            PERMIT_PROJECT_DEVELOPER_TASK_NEW,
            new ArrayList<>(Arrays.asList(
                CALL_START_NEW_PROJECT_DEVELOPER_TASK,
                CALL_INSERT_NEW_PROJECT_TASK_DEVELOPER
            ))
        );
        tmpMap.put(
            PERMIT_PROJECT_TASKS,
            new ArrayList<>(Arrays.asList(CALL_PROJECT_TASKS, CALL_SELECT_PROJECT_TASK_OPERATION))
        );
        tmpMap.put(
            PERMIT_PROJECT_TASK_NEW,
            new ArrayList<>(Arrays.asList(
                CALL_START_NEW_PROJECT_TASK,
                CALL_NEW_PROJECT_TASK,
                CALL_INSERT_NEW_PROJECT_TASK
            ))
        );
        tmpMap.put(
            PERMIT_PROJECT_TASK_MODIFY,
            new ArrayList<>(Arrays.asList(CALL_START_MODIFY_PROJECT_TASK, CALL_MODIFY_PROJECT_TASK))
        );
        tmpMap.put(
            PERMIT_PROJECT_TASK_DEVELOPERS,
            new ArrayList<>(Arrays.asList(CALL_PROJECT_TASK_DEVELOPERS, CALL_SELECT_PROJECT_TASKS_DEVELOPER_OPERATION))
        );
        tmpMap.put(
            PERMIT_PROJECT_TASK_DEVELOPER_NEW,
            new ArrayList<>(Arrays.asList(CALL_START_NEW_PROJECT_TASK_DEVELOPER, CALL_NEW_PROJECT_TASK_DEVELOPER))
        );
        tmpMap.put(
            PERMIT_PROJECT_TASK_DEVELOPER_DELETE,
            new ArrayList<>(Collections.singletonList(CALL_DELETE_PROJECT_TASK_DEVELOPER))
        );
        tmpMap.put(
            PERMIT_PROJECT_TASK_POSTS,
            new ArrayList<>(Arrays.asList(CALL_PROJECT_TASK_POSTS, CALL_SELECT_PROJECT_TASK_POSTS_OPERATION))
        );
        tmpMap.put(PERMIT_DEVELOPERS, new ArrayList<>(Arrays.asList(CALL_DEVELOPERS, CALL_SELECT_DEVELOPER_OPERATION)));
        tmpMap.put(
            PERMIT_DEVELOPER_NEW,
            new ArrayList<>(Arrays.asList(CALL_START_NEW_DEVELOPER, CALL_NEW_DEVELOPER, CALL_INSERT_NEW_DEVELOPER))
        );
        tmpMap.put(
            PERMIT_DEVELOPER_MODIFY,
            new ArrayList<>(Arrays.asList(
                CALL_START_MODIFY_DEVELOPER,
                CALL_MODIFY_DEVELOPER
            ))
        );
        tmpMap.put(PERMIT_DEVELOPER_TASKS, new ArrayList<>(Arrays.asList(
            CALL_DEVELOPER_TASKS, CALL_SELECT_DEVELOPER_TASK_OPERATION ))
        );
        tmpMap.put(PERMIT_DEVELOPER_RAW_LIST, new ArrayList<>(Collections.singletonList(CALL_RAW_DEVELOPER_LIST)));
        tmpMap.put(PERMIT_ERROR, new ArrayList<>(Collections.singletonList(CALL_ERROR)));
        tmpMap.put(
            PERMIT_DBCONSOLE,
            new ArrayList<>(Arrays.asList(CALL_DBCONSOLE_ALIAS, CALL_DBCONSOLE_ALIAS, CALL_DBCONSOLE_ROOT))
        );
        tmpMap.put(PERMIT_LOGOUT, new ArrayList<>(Collections.singletonList(CALL_LOGOUT_CONFIRM)));
        tmpMap.put(
            PERMIT_RIGHTS,
            new ArrayList<>(Arrays.asList(CALL_RIGHTS,
                CALL_RIGHTS_USERS, CALL_START_NEW_USER, CALL_START_MODIFY_USER, CALL_START_DELETE_USER,
                CALL_USER_INPUT_DATA_PROCESSING,
                CALL_RIGHTS_ROLES, CALL_START_NEW_ROLE, CALL_START_MODIFY_ROLE, CALL_START_DELETE_ROLE,
                CALL_ROLE_INPUT_DATA_PROCESSING,
                CALL_START_USER_ROLES, CALL_RIGHTS_USER_ROLES, CALL_START_DELETE_USER_ROLE,
                CALL_START_NEW_USER_ROLE, CALL_USER_ROLE_NEW, CALL_USER_ROLE_NEW_DATA_PROCESSING,
                CALL_START_USER_DEVELOPERS, CALL_RIGHTS_USER_DEVELOPERS, CALL_START_DELETE_USER_DEVELOPER,
                CALL_START_NEW_USER_DEVELOPER, CALL_USER_DEVELOPER_NEW, CALL_USER_DEVELOPER_NEW_DATA_PROCESSING,
                CALL_RIGHTS_PERMITS,
                CALL_START_ROLE_PERMITS, CALL_ROLE_PERMITS,
                CALL_START_NEW_ROLE_PERMIT, CALL_INSERT_NEW_ROLE_PERMIT,
                CALL_START_DELETE_ROLE_PERMIT
            ))
        );
        PROTECTED_REQUEST_CALLS = Collections.unmodifiableMap(tmpMap);
    }

    private RequestsConstants() {
    }
}
