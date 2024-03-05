package combit.hu.porphyr.controller;

import combit.hu.porphyr.controller.helpers.TemplateData;
import combit.hu.porphyr.controller.helpers.SessionData;
import combit.hu.porphyr.controller.helpers.WebErrorBean;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import combit.hu.porphyr.service.ProjectTaskDeveloperService;
import combit.hu.porphyr.service.ProjectTaskService;
import combit.hu.porphyr.service.PorphyrServiceException;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.ExecutionException;
import javax.annotation.Resource;

import static combit.hu.porphyr.Constants.ON;
import static combit.hu.porphyr.controller.helpers.HomeControllerConstants.*;

@Controller
public class HomeControllerProjectsTasks {

    private final @NonNull ProjectTaskService projectTaskService;
    private final @NonNull ProjectTaskDeveloperService projectTaskDeveloperService;

    @Autowired
    public HomeControllerProjectsTasks(
        final @NonNull ProjectTaskService projectTaskService,
        final @NonNull ProjectTaskDeveloperService projectTaskDeveloperService
    ) {
        this.projectTaskService = projectTaskService;
        this.projectTaskDeveloperService = projectTaskDeveloperService;
    }

    @Resource(name = "getWebErrorBean")
    WebErrorBean webErrorBean;
    @Resource(name = "getSessionData")
    SessionData sessionData;

    static final @NonNull String REDIRECT_TO_PROJECTTASKS = "redirect:/project_tasks";
    static final @NonNull String REDIRECT_TO_PROJECTTASKS_DEVELOPERS = "redirect:/project_tasks_developers";
    static final @NonNull String REDIRECT_TO_PROJECTTASKS_MODIFY = "redirect:/project_tasks_modify";
    static final @NonNull String REDIRECT_TO_PROJECTTASKS_NEW = "redirect:/project_tasks_new";
    static final @NonNull String ERROR = "error";

    //-------------- Projekt feladatainak listája
    @RequestMapping("/project_tasks")
    public @NonNull String loadDataBeforeProjectTasks(
        final @NonNull Model model
    ) throws ExecutionException, InterruptedException {
        final ProjectEntity project = sessionData.getSelectedProject();
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        model.addAttribute("project", project);
        model.addAttribute("dataFromTemplate", sessionData.getDataFromTemplate());
        return "project_tasks";
    }

    //-------------- Műveletválasztó -------------------
    @RequestMapping("/selectProjectTaskOperation")
    public @NonNull String selectOperation(
        @ModelAttribute
        @NonNull
        final TemplateData dataFromTemplate
    ) throws ExecutionException, InterruptedException {
        @NonNull
        final String result;
        final @Nullable Long projectTaskId = dataFromTemplate.getId();
        if (projectTaskId == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        final @Nullable ProjectTaskEntity projectTask = projectTaskService.getProjectTaskById(projectTaskId);
        if (projectTask == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        sessionData.setSelectedProjectTaskId(projectTaskId);
        switch (dataFromTemplate.getOperation()) {
            case MENU_ITEM_DEVELOPERS: {
                result = REDIRECT_TO_PROJECTTASKS_DEVELOPERS;
                break;
            }
            case MENU_ITEM_MODIFY: {
                result = REDIRECT_TO_PROJECTTASKS_MODIFY;
                break;
            }
            case MENU_ITEM_DELETE: {
                deleteProjectTask();
                result = REDIRECT_TO_PROJECTTASKS;
                break;
            }
            default:
                throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        return result;
    }

    //------------------ Fejlesztők listája
    @RequestMapping("/project_tasks_developers")
    public @NonNull String projectTaskDevelopers(
        final @NonNull Model model
    ) throws ExecutionException, InterruptedException {
        final @Nullable ProjectEntity project = sessionData.getSelectedProject();
        final @Nullable ProjectTaskEntity projectTask = sessionData.getSelectedProjectTask();

        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        model.addAttribute(
            "possibleNewDevelopers",  //change to: assignableDevelopers
            project.getProjectDevelopers().size() - projectTask.getProjectTaskDevelopers().size()
        );
        model.addAttribute("projectTask", projectTask);
        model.addAttribute("dataFromTemplate", sessionData.getDataFromTemplate());
        return "project_tasks_developers";
    }

    //------------------ Feladat törlése -----------------------------
    public void deleteProjectTask(
    ) throws InterruptedException, ExecutionException {
        try {
            projectTaskService.deleteProjectTask(sessionData.getSelectedProjectTask());
        } catch (PorphyrServiceException porphyrServiceException) {
            webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
        }
    }

    //-------------- Fejlesztő törlése ------------------------
    @RequestMapping("/deleteProjectTaskDeveloper")
    public @NonNull String deleteProjectTaskDeveloper(
        @ModelAttribute
        @NonNull
        final TemplateData dataFromTemplate
    ) throws InterruptedException, ExecutionException {
        try {
            projectTaskDeveloperService.deleteProjectTaskDeveloper(
                sessionData.getSelectedProjectTaskDeveloper());
        } catch (PorphyrServiceException porphyrServiceException) {
            webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
        }
        return REDIRECT_TO_PROJECTTASKS_DEVELOPERS;
    }

    //------------------ Új feladat felvitele a projekthez ---------------------------------
    @RequestMapping("/projecttask_new_start")
    public @NonNull String startNewProjectTask(final @NonNull Model model) {
        sessionData.setSelectedProjectTaskId(0L);
        return REDIRECT_TO_PROJECTTASKS_NEW;
    }

    @RequestMapping("/project_tasks_new")
    public @NonNull String newProjectTask(final @NonNull Model model)
        throws ExecutionException, InterruptedException {
        model.addAttribute(
            "projectName",
            sessionData.getSelectedProject().getName()
        );
        model.addAttribute("newProjectTask", sessionData.getDataToTemplate());
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        return "project_tasks_new";
    }

    @PostMapping("/insertNewProjectTask")
    public @NonNull String insertNewProjectTask(
        @ModelAttribute
        @NonNull
        final TemplateData dataFromTemplate
    ) throws InterruptedException, ExecutionException {
        @NonNull String result = REDIRECT_TO_PROJECTTASKS;
        @Nullable
        final String name = dataFromTemplate.getName();
        if (name == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        sessionData.setDataToTemplate(dataFromTemplate);
        final @NonNull ProjectTaskEntity newProjectTask = new ProjectTaskEntity();
        newProjectTask.setProjectEntity(
            sessionData.getSelectedProject()
        );
        newProjectTask.setName(name);
        newProjectTask.setDescription(dataFromTemplate.getDescription());
        try {
            projectTaskService.insertNewProjectTask(newProjectTask);
        } catch (PorphyrServiceException porphyrServiceException) {
            webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
            result = REDIRECT_TO_PROJECTTASKS_NEW;
        }
        return result;
    }

    //------------------ Projektfeladat módosítása ---------------------------------
    @RequestMapping("/project_tasks_modify")
    public @NonNull String modifyProjectTask(final @NonNull Model model)
        throws ExecutionException, InterruptedException {
        model.addAttribute(
            "projectName", sessionData.getSelectedProject().getName()
        );
        model.addAttribute(
            "projectTaskName", sessionData.getSelectedProjectTask().getName()
        );
        model.addAttribute("projectTask", sessionData.getSelectedProjectTask());
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        return "project_tasks_modify";
    }

    @PostMapping("/modifyProjectTask")
    public @NonNull String endModifyProjectTask(
        @ModelAttribute
        @NonNull
        final TemplateData dataFromTemplate
    ) throws InterruptedException, ExecutionException {
        @NonNull String result = REDIRECT_TO_PROJECTTASKS;
        @Nullable
        final String name = dataFromTemplate.getName();
        if (name == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        final @Nullable ProjectTaskEntity editedProjectTask = sessionData.getSelectedProjectTask();
        editedProjectTask.setName(name);
        editedProjectTask.setDescription(dataFromTemplate.getDescription());
        try {
            projectTaskService.modifyProjectTask(editedProjectTask);
        } catch (PorphyrServiceException porphyrServiceException) {
            webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
            result = REDIRECT_TO_PROJECTTASKS_MODIFY;
        }
        return result;
    }
}
