package combit.hu.porphyr.controller;

import combit.hu.porphyr.controller.helpers.SessionData;
import combit.hu.porphyr.controller.helpers.TemplateData;
import combit.hu.porphyr.controller.helpers.WebErrorBean;
import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTaskDeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import combit.hu.porphyr.service.ProjectDeveloperService;
import combit.hu.porphyr.service.ProjectTaskDeveloperService;
import combit.hu.porphyr.service.ProjectTaskService;
import combit.hu.porphyr.service.PorphyrServiceException;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static combit.hu.porphyr.Constants.ON;
import static combit.hu.porphyr.controller.helpers.ControllerConstants.*;

@Controller
public class ProjectsDevelopersTasksController {

    private final @NonNull ProjectTaskService projectTaskService;
    private final @NonNull ProjectDeveloperService projectDeveloperService;
    private final @NonNull ProjectTaskDeveloperService projectTaskDeveloperService;

    @Autowired
    public ProjectsDevelopersTasksController(
        final @NonNull ProjectTaskService projectTaskService,
        final @NonNull ProjectDeveloperService projectDeveloperService,
        final @NonNull ProjectTaskDeveloperService projectTaskDeveloperService
    ) {
        this.projectTaskService = projectTaskService;
        this.projectDeveloperService = projectDeveloperService;
        this.projectTaskDeveloperService = projectTaskDeveloperService;
    }

    @Resource(name = "getWebErrorBean")
    WebErrorBean webErrorBean;
    @Resource(name = "getSessionData")
    SessionData sessionData;

    static final @NonNull String REDIRECT_TO_PROJECTDEVELOPERS_TASKS = "redirect:/project_developers_tasks";

    //------------------ Műveletválasztó ----------------------------------------
    @RequestMapping("/selectProjectDeveloperTaskOperation")
    public @NonNull String selectOperation(
        @ModelAttribute
        @NonNull
        final TemplateData dataFromTemplate
    ) throws InterruptedException, ExecutionException {
        @NonNull
        final String result;
        @Nullable
        final Long projectTaskDeveloperId = dataFromTemplate.getId();
        if (projectTaskDeveloperId == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        sessionData.setSelectedProjectTaskDeveloperId(projectTaskDeveloperId);
        @Nullable
        ProjectTaskDeveloperEntity projectTaskDeveloper =
            projectTaskDeveloperService.getProjectTaskDeveloperById(projectTaskDeveloperId);
        if (projectTaskDeveloper == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        switch (dataFromTemplate.getOperation()) {
            case MENU_ITEM_MODIFY: {
                sessionData.setDataFromTemplate(dataFromTemplate);
                projectDevelopersTasksSpendTime();
                result = REDIRECT_TO_PROJECTDEVELOPERS_TASKS;
                break;
            }
            case MENU_ITEM_DELETE: {
                removeProjectTaskFromProjectDeveloper();
                result = REDIRECT_TO_PROJECTDEVELOPERS_TASKS;
                break;
            }
            default:
                throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        return result;
    }

    //------------------------ Fejlesztő feladatai a projektben
    @RequestMapping("/project_developers_tasks")
    public @NonNull String tasksOfProjectDeveloper(
        final @NonNull Model model
    ) throws InterruptedException, ExecutionException {
        final @NonNull ProjectEntity project = sessionData.getSelectedProject();
        final @NonNull DeveloperEntity developer = sessionData.getSelectedDeveloper();
        final @Nullable Long projectId = project.getId();
        final @Nullable Long developerId = developer.getId();

        if (projectId == null || developerId == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        List<ProjectTaskEntity> allProjectTasks = projectTaskService.getProjectTasksByProjectEntity(
            Objects.requireNonNull(project)
        );
        List<ProjectTaskDeveloperEntity> allProjectTaskDevelopers =
            projectTaskDeveloperService.getProjectTaskDeveloperByProjectIdAndDeveloperId(projectId, developerId);

        model.addAttribute("error", webErrorBean.getWebErrorData());
        model.addAttribute("projectTaskDevelopers", allProjectTaskDevelopers);
        model.addAttribute("project", project);
        model.addAttribute("developer", developer);
        model.addAttribute("dataFromTemplate", sessionData.getDataFromTemplate());
        model.addAttribute(
            "assignAbleProjectTasks",
            allProjectTasks.size() - allProjectTaskDevelopers.size()
        );
        return "project_developers_tasks";
    }

    //-------------------------- Idő módosítása a feladatban
    public void projectDevelopersTasksSpendTime(
    ) throws InterruptedException, ExecutionException {
        final @Nullable Long time = sessionData.getDataFromTemplate().getLongData();
        if (time == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloper = sessionData.getSelectedProjectTaskDeveloper();
        projectTaskDeveloper.setSpendTime(time);
        try {
            projectTaskDeveloperService.modifyProjectTaskDeveloper(projectTaskDeveloper);
        } catch (PorphyrServiceException porphyrServiceException) {
            webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
        }
    }

    //---------------------------- Projektfeladat eltávolítása a fejlesztőtől
    public void removeProjectTaskFromProjectDeveloper(
    ) throws InterruptedException, ExecutionException {
        final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloper =
            sessionData.getSelectedProjectTaskDeveloper();
        try {
            projectTaskDeveloperService.deleteProjectTaskDeveloper(projectTaskDeveloper);
        } catch (PorphyrServiceException porphyrServiceException) {
            webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
        }
    }

    //---------------------------- Új projektfeladat hozzárendelése a fejlesztőhöz.
    @RequestMapping("/project_developers_tasks_new")
    public @NonNull String insertNewProjectDeveloperTask(
        final @NonNull Model model
    ) throws InterruptedException, ExecutionException {
        final @NonNull ProjectEntity project = sessionData.getSelectedProject();
        final @Nullable Long projectId = project.getId();
        if (projectId == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        final @NonNull DeveloperEntity developer = sessionData.getSelectedDeveloper();
        final @Nullable Long developerId = developer.getId();
        if (developerId == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        final @Nullable ProjectDeveloperEntity projectDeveloper =
            projectDeveloperService.getProjectDeveloperByProjectAndDeveloper(project, developer);
        if (projectDeveloper == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        final @Nullable Long projectDeveloperId = projectDeveloper.getId();
        if (projectDeveloperId == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        sessionData.setSelectedProjectDeveloperId(projectDeveloperId);
        //A projekten belül, a fejlesztőhöz nem tartozó feladatok listájának összeállítsa
        //-- Az összes projektfeladat
        final @NonNull List<ProjectTaskEntity> projectTasks = projectTaskService.getProjectTasksByProjectEntity(
            project);
        //-- A fejlesztőnél lévő projektfeladatok
        final @NonNull List<ProjectTaskDeveloperEntity> projectTaskDevelopers =
            projectTaskDeveloperService.getProjectTaskDeveloperByProjectIdAndDeveloperId(projectId, developerId);
        for (ProjectTaskDeveloperEntity projectTaskDeveloper : projectTaskDevelopers) {
            projectTasks.removeIf(aProjectTask -> (
                    aProjectTask == projectTaskDeveloper.getProjectTaskEntity()
                )
            );
        }
        model.addAttribute("freeProjectTasks", projectTasks);
        model.addAttribute("projectName", project.getName());
        model.addAttribute("developerName", developer.getName());
        model.addAttribute("dataFromTemplate", sessionData.getDataFromTemplate());

        return "project_developers_tasks_new";
    }

    @RequestMapping("/insertNewProjectTaskDeveloper")
    public @NonNull String insertNewProjectTaskDeveloper(
        @ModelAttribute
        @NonNull
        final TemplateData dataFromTemplate
    ) throws InterruptedException, ExecutionException {
        final @Nullable Long projectTaskId = dataFromTemplate.getId();
        if (projectTaskId == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        ProjectTaskEntity projectTaskEntity = projectTaskService.getProjectTaskById(projectTaskId);
        if (projectTaskEntity == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        try {
            ProjectTaskDeveloperEntity projectTaskDeveloperEntity = new ProjectTaskDeveloperEntity();
            projectTaskDeveloperEntity.setProjectTaskEntity(projectTaskEntity);
            projectTaskDeveloperEntity.setProjectDeveloperEntity(
                sessionData.getSelectedProjectDeveloper()
            );
            projectTaskDeveloperService.insertNewProjectTaskDeveloper(projectTaskDeveloperEntity);
        } catch (PorphyrServiceException porphyrServiceException) {
            webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
        }
        return REDIRECT_TO_PROJECTDEVELOPERS_TASKS;
    }
}
