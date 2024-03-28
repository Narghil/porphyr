package combit.hu.porphyr;

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

    public static final @NonNull String CALL_RAW_DEVELOPER_LIST = "/devs";
    public static final @NonNull String CALL_SELECT_DEVELOPER_OPERATION = "/selectDeveloperOperation";
    public static final @NonNull String CALL_START_NEW_DEVELOPER = "/developers_new_start";
    public static final @NonNull String CALL_NEW_DEVELOPER = "/developer_new";
    public static final @NonNull String CALL_INSERT_NEW_DEVELOPER = "/insertNewDeveloper";
    public static final @NonNull String CALL_START_MODIFY_DEVELOPER = "/developer_modify";
    public static final @NonNull String CALL_MODIFY_DEVELOPER = "/modifyDeveloper";
    public static final @NonNull String CALL_DEVELOPER_TASKS = "/developer_tasks";
    public static final @NonNull String CALL_MODIFY_DEVELOPER_TASK = "/modifyDeveloperTask";
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
    public static final @NonNull String CALL_LOGOUT_CONFIRM = "/confirm_logout";

    //A PERMITS táblába a kulcsok kerülnek bele. Így egy funkcióhoz (engedélyhez) több request is tartozhat.
    public static final @NonNull Map<String, List<String>> PROTECTED_REQUEST_CALLS;
    static {
        final Map<String, List<String>> tmpMap = new HashMap<>();
        tmpMap.put("rawDeveloperList", new ArrayList<>(Collections.singletonList(CALL_RAW_DEVELOPER_LIST)) );
        tmpMap.put("newDeveloper", new ArrayList<>(Arrays.asList(  CALL_START_NEW_DEVELOPER, CALL_NEW_DEVELOPER, CALL_INSERT_NEW_DEVELOPER)) );
        tmpMap.put("modifyDeveloper", new ArrayList<>(Arrays.asList(  CALL_START_MODIFY_DEVELOPER, CALL_MODIFY_DEVELOPER)) );
        tmpMap.put("developerTasks", new ArrayList<>(Collections.singletonList(CALL_DEVELOPER_TASKS)) );
        tmpMap.put("modifyDeveloperTask", new ArrayList<>(Collections.singletonList(CALL_MODIFY_DEVELOPER_TASK)) );
        tmpMap.put("error", new ArrayList<>(Collections.singletonList(CALL_ERROR)) );
        tmpMap.put("root", new ArrayList<>(Collections.singletonList(CALL_ROOT)) );
        tmpMap.put("dbConsole", new ArrayList<>(Arrays.asList(CALL_DBCONSOLE_ALIAS, CALL_DBCONSOLE_ALIAS, CALL_DBCONSOLE_ROOT)) );
        tmpMap.put("projects", new ArrayList<>(Arrays.asList(CALL_PROJECTS, CALL_SELECT_PROJECT_OPERATION)) );
        tmpMap.put("developers", new ArrayList<>(Arrays.asList(CALL_DEVELOPERS, CALL_SELECT_DEVELOPER_OPERATION)) );
        tmpMap.put("newProject", new ArrayList<>(Arrays.asList(  CALL_START_NEW_PROJECT, CALL_NEW_PROJECT, CALL_INSERT_NEW_PROJECT )) );
        tmpMap.put("deleteProject", new ArrayList<>(Collections.singletonList(CALL_DELETE_PROJECT)) );
        tmpMap.put("modifyProject", new ArrayList<>(Arrays.asList(  CALL_START_MODIFY_PROJECT, CALL_MODIFY_PROJECT )) );
        tmpMap.put("projectDevelopers", new ArrayList<>(Arrays.asList( CALL_PROJECT_DEVELOPERS, CALL_SELECT_PROJECT_DEVELOPER_OPERATION)) );
        tmpMap.put("newProjectDeveloper", new ArrayList<>(Arrays.asList(  CALL_START_NEW_PROJECT_DEVELOPER, CALL_INSERT_NEW_PROJECT_DEVELOPER )) );
        tmpMap.put("projectDeveloperTasks", new ArrayList<>(Arrays.asList( CALL_PROJECT_DEVELOPER_TASKS, CALL_SELECT_PROJECT_DEVELOPER_TASK_OPERATION)) );
        tmpMap.put("newProjectDeveloperTask", new ArrayList<>(Arrays.asList( CALL_START_NEW_PROJECT_DEVELOPER_TASK, CALL_INSERT_NEW_PROJECT_TASK_DEVELOPER )) );
        tmpMap.put("projectTasks", new ArrayList<>(Arrays.asList(CALL_PROJECT_TASKS, CALL_SELECT_PROJECT_TASK_OPERATION)) );
        tmpMap.put("projectTaskDevelopers", new ArrayList<>(Arrays.asList(CALL_PROJECT_TASK_DEVELOPERS, CALL_SELECT_PROJECT_TASKS_DEVELOPER_OPERATION)) );
        tmpMap.put("deleteProjectTaskDeveloper", new ArrayList<>(Collections.singletonList(CALL_DELETE_PROJECT_TASK_DEVELOPER)) );
        tmpMap.put("newProjectTask", new ArrayList<>(Arrays.asList(  CALL_START_NEW_PROJECT_TASK, CALL_NEW_PROJECT_TASK, CALL_INSERT_NEW_PROJECT_TASK )) );
        tmpMap.put("modifyProjectTask", new ArrayList<>(Arrays.asList(  CALL_START_MODIFY_PROJECT_TASK, CALL_MODIFY_PROJECT_TASK )) );
        tmpMap.put("newProjectTaskDeveloper", new ArrayList<>(Arrays.asList(  CALL_START_NEW_PROJECT_TASK_DEVELOPER, CALL_NEW_PROJECT_TASK_DEVELOPER )) );
        tmpMap.put("logout", new ArrayList<>(Collections.singletonList( CALL_LOGOUT_CONFIRM )) );
        PROTECTED_REQUEST_CALLS = Collections.unmodifiableMap(tmpMap);
    }

    private RequestsConstants() {
    }
}
