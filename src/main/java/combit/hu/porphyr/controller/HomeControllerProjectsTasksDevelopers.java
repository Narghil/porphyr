package combit.hu.porphyr.controller;

import combit.hu.porphyr.controller.helpers.SelectedOperationDataBean;
import combit.hu.porphyr.controller.helpers.WebErrorBean;
import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTaskDeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import combit.hu.porphyr.service.ProjectDeveloperService;
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

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static combit.hu.porphyr.controller.helpers.HomeControllerConstants.*;

@Controller
public class HomeControllerProjectsTasksDevelopers {

    private ProjectService projectService;
    private ProjectTaskService projectTaskService;
    private ProjectDeveloperService projectDeveloperService;
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
    public void setProjectDeveloperService(ProjectDeveloperService projectDeveloperService) {
        this.projectDeveloperService = projectDeveloperService;
    }

    @Autowired
    public void setProjectTaskDeveloperService(ProjectTaskDeveloperService projectTaskDeveloperService) {
        this.projectTaskDeveloperService = projectTaskDeveloperService;
    }

    @Resource(name = "getWebErrorBean")
    WebErrorBean webErrorBean;
    @Resource(name = "getSelectedOperationDataBean")
    SelectedOperationDataBean selectedOperationDataBean;

    static final @NonNull String REDIRECT_TO_PROJECTTASKS_DEVELOPERS = "redirect:/project_tasks_developers";

    //-------------- Műveletválasztó -------------------
    @RequestMapping("/selectProjectTasksDeveloperOperation")
    public @NonNull String selectOperation(
        @ModelAttribute
        @NonNull
        SelectedOperationDataBean selectedOperation
    ) throws ExecutionException, InterruptedException {
        @NonNull String result;
        selectedOperationDataBean.setProjectTaskDeveloperId(selectedOperation.getProjectTaskDeveloperId());
        selectedOperationDataBean.setLongData(selectedOperation.getLongData());
        switch (selectedOperation.getOperation()) {
            case MODIFY: {
                modifyProjectTaskDeveloper();
                result = REDIRECT_TO_PROJECTTASKS_DEVELOPERS;
                break;
            }
            case DELETE: {
                deleteProjectTaskDeveloper();
                result = REDIRECT_TO_PROJECTTASKS_DEVELOPERS;
                break;
            }
            default:
                throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        return result;
    }

    //-------------- Fejlesztő törlése ------------------------
    public void deleteProjectTaskDeveloper(
    ) throws InterruptedException, ExecutionException {
        final @Nullable ProjectTaskDeveloperEntity projectTaskDeveloper =
            projectTaskDeveloperService.getProjectTaskDeveloperById(selectedOperationDataBean.getProjectTaskDeveloperId());
        if (projectTaskDeveloper != null) {
            try {
                projectTaskDeveloperService.deleteProjectTaskDeveloper(projectTaskDeveloper);
            } catch (ServiceException serviceException) {
                webErrorBean.setError("ON", ERROR_TITLE, serviceException.getMessage());
            }
        } else {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
    }

    //---------------- Fejlesztő munkaidejének módosítása
    public void modifyProjectTaskDeveloper(
    ) throws InterruptedException, ExecutionException {
        final @Nullable ProjectTaskDeveloperEntity projectTaskDeveloper =
            projectTaskDeveloperService.getProjectTaskDeveloperById(selectedOperationDataBean.getProjectTaskDeveloperId());
        if (projectTaskDeveloper != null) {
            try {
                projectTaskDeveloper.setSpendTime(Objects.requireNonNull(selectedOperationDataBean.getLongData()));
                projectTaskDeveloperService.modifyProjectTaskDeveloper(projectTaskDeveloper);
            } catch (ServiceException serviceException) {
                webErrorBean.setError("ON", ERROR_TITLE, serviceException.getMessage());
            }
        } else {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
    }

    //---------------------- Új fejlesztő hozzárendelése a feladathoz
    @RequestMapping("/project_tasks_developers_new")
    public String newProjectTaskDeveloper(Model model) throws InterruptedException, ExecutionException {
        final @Nullable ProjectEntity project = projectService.getProjectById(selectedOperationDataBean.getProjectId());
        final @Nullable ProjectTaskEntity projectTask = projectTaskService.getProjectTaskById(selectedOperationDataBean.getProjectTaskId());
        if (project != null && projectTask != null) {
            final @NonNull List<ProjectDeveloperEntity> projectDevelopers = project.getProjectDevelopers();
            final @NonNull List<ProjectTaskDeveloperEntity> projectTaskDevelopers = projectTask.getProjectTaskDevelopers();

            for (ProjectTaskDeveloperEntity projectTaskDeveloper : projectTaskDevelopers) {
                projectDevelopers.removeIf(aDeveloper -> (
                    aDeveloper.getDeveloperEntity().getId() != null &&
                        aDeveloper.getDeveloperEntity()
                            .getId()
                            .equals(projectTaskDeveloper.getProjectDeveloperEntity().getDeveloperEntity().getId()))
                );
            }

            model.addAttribute("projectName", project.getName());
            model.addAttribute("taskName", projectTask.getName());
            model.addAttribute("selectableProjectDevelopers", projectDevelopers);
            model.addAttribute("formData", selectedOperationDataBean);
        } else {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }

        return "project_tasks_developers_new";
    }

    @RequestMapping("/newProjectTaskDeveloper")
    public @NonNull String newProjectTaskDeveloper(
        @ModelAttribute
        @NonNull
        SelectedOperationDataBean formData
    ) throws InterruptedException, ExecutionException {
        final @Nullable ProjectDeveloperEntity projectDeveloper = projectDeveloperService.getProjectDeveloperById(
            formData.getProjectDeveloperId());
        final @Nullable ProjectTaskEntity projectTask = projectTaskService.getProjectTaskById(selectedOperationDataBean.getProjectTaskId());
        if (projectDeveloper != null && projectTask != null) {
            final @NonNull ProjectTaskDeveloperEntity newProjectTaskDeveloper =
                new ProjectTaskDeveloperEntity();
            newProjectTaskDeveloper.setProjectTaskAndProjectDeveloper(projectTask, projectDeveloper);

            try {
                projectTaskDeveloperService.insertNewProjectTaskDeveloper(newProjectTaskDeveloper);
            } catch (ServiceException serviceException) {
                webErrorBean.setError("ON", ERROR_TITLE, serviceException.getMessage());
            }
        } else {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }

        return REDIRECT_TO_PROJECTTASKS_DEVELOPERS;
    }
}
