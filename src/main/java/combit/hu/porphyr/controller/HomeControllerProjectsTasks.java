package combit.hu.porphyr.controller;

import combit.hu.porphyr.controller.helpers.SelectedOperationDataBean;
import combit.hu.porphyr.controller.helpers.WebErrorBean;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTaskDeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import combit.hu.porphyr.service.ProjectService;
import combit.hu.porphyr.service.ProjectTaskDeveloperService;
import combit.hu.porphyr.service.ProjectTaskService;
import combit.hu.porphyr.service.ServiceException;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.ExecutionException;
import javax.annotation.Resource;

import static combit.hu.porphyr.controller.helpers.HomeControllerConstants.*;

@Controller
public class HomeControllerProjectsTasks {

    private ProjectService projectService;
    private ProjectTaskService projectTaskService;
    private ProjectTaskDeveloperService projectTaskDeveloperService;

    @Autowired
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Autowired
    public void setProjectTaskService(ProjectTaskService projectTaskService) {
        this.projectTaskService = projectTaskService;
    }

    @Autowired
    public void setProjectTaskDeveloperService(ProjectTaskDeveloperService projectTaskDeveloperService) {
        this.projectTaskDeveloperService = projectTaskDeveloperService;
    }

    @Resource(name = "getWebErrorBean")
    WebErrorBean webErrorBean;
    @Resource(name = "getSelectedOperationDataBean")
    SelectedOperationDataBean selectedOperationDataBean;

    static final @NonNull String REDIRECT_TO_PROJECTTASKS = "redirect:/project_tasks";
    static final @NonNull String REDIRECT_TO_PROJECTTASKS_DEVELOPERS = "redirect:/project_tasks_developers";
    static final @NonNull String REDIRECT_TO_PROJECTTASKS_MODIFY = "redirect:/project_tasks_modify";

    //-------------- Projekt feladatainak listája
    @RequestMapping("/project_tasks")
    public @NonNull String loadDataBeforeProjectTasks(
        Model model
    ) throws InterruptedException, ExecutionException {
        final ProjectEntity project = projectService.getProjectById(selectedOperationDataBean.getProjectId());
        if (project == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        model.addAttribute("error", webErrorBean.getWebErrorData());
        model.addAttribute("project", project);
        model.addAttribute("selectedOperation", selectedOperationDataBean);
        return "project_tasks";
    }

    //-------------- Műveletválasztó -------------------
    @RequestMapping("/selectProjectTaskOperation")
    public @NonNull String selectOperation(
        @ModelAttribute
        @NonNull
        SelectedOperationDataBean selectedOperation
    ) throws ExecutionException, InterruptedException {
        @NonNull String result;
        selectedOperationDataBean.setProjectTaskId(selectedOperation.getProjectTaskId());
        switch (selectedOperation.getOperation()) {
            case DEVELOPERS: {
                result = REDIRECT_TO_PROJECTTASKS_DEVELOPERS;
                break;
            }
            case MODIFY: {
                result = REDIRECT_TO_PROJECTTASKS_MODIFY;
                break;
            }
            case DELETE: {
                deleteProjectTask();
                result = REDIRECT_TO_PROJECTTASKS;
                break;
            }
            default:
                throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        return result;
    }

    //------------------ Fejlesztők listája
    @RequestMapping("/project_tasks_developers")
    public @NonNull String projectTaskDevelopers(
        Model model
    ) throws ExecutionException, InterruptedException {
        final @Nullable ProjectEntity project = projectService.getProjectById(selectedOperationDataBean.getProjectId());
        final @Nullable ProjectTaskEntity projectTask = projectTaskService.getProjectTaskById(selectedOperationDataBean.getProjectTaskId());

        if (project != null && projectTask != null) {
            model.addAttribute("error", webErrorBean.getWebErrorData());
            model.addAttribute(
                "possibleNewDevelopers",
                project.getProjectDevelopers().size() - projectTask.getProjectTaskDevelopers().size()
            );
            model.addAttribute("projectTask", projectTask);
            model.addAttribute("selectedOperationData", selectedOperationDataBean);
        } else {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        return "project_tasks_developers";
    }

    //------------------ Feladat törlése -----------------------------
    public void deleteProjectTask(
    ) throws InterruptedException, ExecutionException {
        final @Nullable ProjectTaskEntity projectTask =
            projectTaskService.getProjectTaskById(selectedOperationDataBean.getProjectTaskId());
        if (projectTask != null) {
            try {
                projectTaskService.deleteProjectTask(projectTask);
            } catch (ServiceException serviceException) {
                webErrorBean.setError("ON", ERROR_TITLE, serviceException.getMessage());
            }
        } else {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
    }

    //-------------- Fejlesztő törlése ------------------------
    @RequestMapping("/deleteProjectTaskDeveloper")
    public String deleteProjectTaskDeveloper(
        @ModelAttribute
        @NonNull
        SelectedOperationDataBean selectedOperation
    ) throws InterruptedException, ExecutionException {
        final @Nullable ProjectTaskDeveloperEntity projectTaskDeveloper =
            projectTaskDeveloperService.getProjectTaskDeveloperById(selectedOperation.getProjectTaskDeveloperId());
        if( projectTaskDeveloper != null) {
            try {
                projectTaskDeveloperService.deleteProjectTaskDeveloper(projectTaskDeveloper);
            } catch (ServiceException serviceException) {
                webErrorBean.setError("ON", ERROR_TITLE, serviceException.getMessage());
            }
        } else {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        return REDIRECT_TO_PROJECTTASKS_DEVELOPERS;
    }
}
