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

import static combit.hu.porphyr.Constants.ON;
import static combit.hu.porphyr.controller.helpers.HomeControllerConstants.*;

@Controller
public class HomeControllerProjectsDevelopersTasks {

    private ProjectTaskService projectTaskService;
    private ProjectDeveloperService projectDeveloperService;
    private ProjectTaskDeveloperService projectTaskDeveloperService;

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
    @Resource(name = "getSessionData")
    SessionData sessionData;

    static final @NonNull String REDIRECT_TO_PROJECTDEVELOPERS_TASKS = "redirect:/project_developers_tasks";

    //------------------ Műveletválasztó ----------------------------------------
    @RequestMapping("/selectProjectDeveloperTaskOperation")
    public @NonNull String selectOperation(
        @ModelAttribute
        @NonNull
        TemplateData dataFromTemplate
    ) throws InterruptedException, ExecutionException {
        @NonNull String result;
        @Nullable Long projectTaskDeveloperId = dataFromTemplate.getId();
        if (projectTaskDeveloperId == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        sessionData.setSelectedProjectTaskDeveloperId(projectTaskDeveloperId);
        @Nullable ProjectTaskDeveloperEntity projectTaskDeveloper =
            projectTaskDeveloperService.getProjectTaskDeveloperById(projectTaskDeveloperId);
        if (projectTaskDeveloper == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        switch (dataFromTemplate.getOperation()) {
            case MODIFY: {
                sessionData.setDataFromTemplate(dataFromTemplate);
                projectDevelopersTasksSpendTime();
                result = REDIRECT_TO_PROJECTDEVELOPERS_TASKS;
                break;
            }
            case DELETE: {
                removeProjectTaskFromProjectDeveloper();
                result = REDIRECT_TO_PROJECTDEVELOPERS_TASKS;
                break;
            }
            default:
                throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        return result;
    }

    //------------------------ Fejlesztő feladatai a projektben
    @RequestMapping("/project_developers_tasks")
    public @NonNull String tasksOfProjectDeveloper(
        Model model
    ) throws InterruptedException, ExecutionException {
        final @NonNull ProjectEntity project = sessionData.getSelectedProject();
        final @NonNull DeveloperEntity developer = sessionData.getSelectedDeveloper();
        final @Nullable Long projectId = project.getId();
        final @Nullable Long developerId = developer.getId();

        if (projectId == null || developerId == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
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
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloper = sessionData.getSelectedProjectTaskDeveloper();
        projectTaskDeveloper.setSpendTime(time);
        try {
            projectTaskDeveloperService.modifyProjectTaskDeveloper(projectTaskDeveloper);
        } catch (ServiceException serviceException) {
            webErrorBean.setError(ON, ERROR_TITLE, serviceException.getMessage());
        }
    }

    //---------------------------- Projektfeladat eltávolítása a fejlesztőtől
    public void removeProjectTaskFromProjectDeveloper(
    ) throws InterruptedException, ExecutionException {
        final ProjectTaskDeveloperEntity projectTaskDeveloper =
            sessionData.getSelectedProjectTaskDeveloper();
        try {
            projectTaskDeveloperService.deleteProjectTaskDeveloper(projectTaskDeveloper);
        } catch (ServiceException serviceException) {
            webErrorBean.setError(ON, ERROR_TITLE, serviceException.getMessage());
        }
    }

    //---------------------------- Új projektfeladat hozzárendelése a fejlesztőhöz.
    @RequestMapping("/project_developers_tasks_new")
    public @NonNull String insertNewProjectDeveloperTask(
        Model model
    ) throws InterruptedException, ExecutionException {
        final @NonNull ProjectEntity project = sessionData.getSelectedProject();
        final @Nullable Long projectId = project.getId();
        if (projectId == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        final @NonNull DeveloperEntity developer = sessionData.getSelectedDeveloper();
        final @Nullable Long developerId = developer.getId();
        if (developerId == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        final @Nullable ProjectDeveloperEntity projectDeveloper =
            projectDeveloperService.getProjectDeveloperByProjectAndDeveloper(project, developer);
        if (projectDeveloper == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        final @Nullable Long projectDeveloperId = projectDeveloper.getId();
        if (projectDeveloperId == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
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
        TemplateData dataFromTemplate
    ) throws InterruptedException, ExecutionException {
        final @Nullable Long projectTaskId = dataFromTemplate.getId();
        if (projectTaskId == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        ProjectTaskEntity projectTaskEntity = projectTaskService.getProjectTaskById(projectTaskId);
        if (projectTaskEntity == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        try {
            ProjectTaskDeveloperEntity projectTaskDeveloperEntity = new ProjectTaskDeveloperEntity();
            projectTaskDeveloperEntity.setProjectTaskEntity(projectTaskEntity);
            projectTaskDeveloperEntity.setProjectDeveloperEntity(
                sessionData.getSelectedProjectDeveloper()
            );
            projectTaskDeveloperService.insertNewProjectTaskDeveloper(projectTaskDeveloperEntity);
        } catch (ServiceException serviceException) {
            webErrorBean.setError(ON, ERROR_TITLE, serviceException.getMessage());
        }
        return REDIRECT_TO_PROJECTDEVELOPERS_TASKS;
    }
}
