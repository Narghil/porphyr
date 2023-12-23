package combit.hu.porphyr.controller;

import combit.hu.porphyr.controller.helpers.SessionData;
import combit.hu.porphyr.controller.helpers.TemplateData;
import combit.hu.porphyr.controller.helpers.WebErrorBean;
import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTaskDeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import combit.hu.porphyr.service.ProjectTaskDeveloperService;
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
import java.util.concurrent.ExecutionException;

import static combit.hu.porphyr.Constants.ON;
import static combit.hu.porphyr.controller.helpers.HomeControllerConstants.*;

@Controller
public class HomeControllerProjectsTasksDevelopers {

    private ProjectTaskDeveloperService projectTaskDeveloperService;

    @Autowired
    public void setProjectTaskDeveloperService(ProjectTaskDeveloperService projectTaskDeveloperService) {
        this.projectTaskDeveloperService = projectTaskDeveloperService;
    }

    @Resource(name = "getWebErrorBean")
    WebErrorBean webErrorBean;
    @Resource(name = "getSessionData")
    SessionData sessionData;

    static final @NonNull String REDIRECT_TO_PROJECTTASKS_DEVELOPERS = "redirect:/project_tasks_developers";

    //-------------- Műveletválasztó -------------------
    @RequestMapping("/selectProjectTasksDeveloperOperation")
    public @NonNull String selectOperation(
        @ModelAttribute
        @NonNull
        TemplateData dataFromTemplate
    ) throws ExecutionException, InterruptedException {
        @NonNull String result;
        sessionData.setDataFromTemplate(dataFromTemplate);
        final @Nullable Long projectTaskDeveloperId = dataFromTemplate.getId();
        if (projectTaskDeveloperId == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        final @Nullable ProjectTaskDeveloperEntity projectTaskDeveloper =
            projectTaskDeveloperService.getProjectTaskDeveloperById(projectTaskDeveloperId);
        if (projectTaskDeveloper == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        sessionData.setSelectedProjectTaskDeveloperId(projectTaskDeveloperId);
        sessionData.moveDataFromToTemplate();
        switch (dataFromTemplate.getOperation()) {
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
        try {
            projectTaskDeveloperService.deleteProjectTaskDeveloper(
                sessionData.getSelectedProjectTaskDeveloper());
        } catch (ServiceException serviceException) {
            webErrorBean.setError(ON, ERROR_TITLE, serviceException.getMessage());
        }
    }

    //---------------- Fejlesztő munkaidejének módosítása
    public void modifyProjectTaskDeveloper(
    ) throws InterruptedException, ExecutionException {
        final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloper = sessionData.getSelectedProjectTaskDeveloper();
        final @Nullable Long time = sessionData.getDataFromTemplate().getLongData();
        if (time == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        try {
            projectTaskDeveloper.setSpendTime(time);
            projectTaskDeveloperService.modifyProjectTaskDeveloper(projectTaskDeveloper);
        } catch (ServiceException serviceException) {
            webErrorBean.setError(ON, ERROR_TITLE, serviceException.getMessage());
        }
    }

    //---------------------- Új fejlesztő hozzárendelése a feladathoz
    @RequestMapping("/project_tasks_developers_new")
    public String newProjectTaskDeveloper(Model model)
    throws InterruptedException, ExecutionException {
        final @NonNull ProjectEntity project = sessionData.getSelectedProject();
        final @NonNull ProjectTaskEntity projectTask = sessionData.getSelectedProjectTask();
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
        model.addAttribute("dataFromTemplate", sessionData.getDataFromTemplate());

        return "project_tasks_developers_new";
    }

    @RequestMapping("/newProjectTaskDeveloper")
    public @NonNull String newProjectTaskDeveloper(
        @ModelAttribute
        @NonNull
        TemplateData dataFromTemplate
    ) throws InterruptedException, ExecutionException {
        final @Nullable Long projectDeveloperId = dataFromTemplate.getId();
        if( projectDeveloperId == null){
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        sessionData.setSelectedProjectDeveloperId( projectDeveloperId );
        final @NonNull ProjectDeveloperEntity projectDeveloper = sessionData.getSelectedProjectDeveloper();
        final @NonNull ProjectTaskEntity projectTask = sessionData.getSelectedProjectTask();
        final @NonNull ProjectTaskDeveloperEntity newProjectTaskDeveloper = new ProjectTaskDeveloperEntity();
        newProjectTaskDeveloper.setProjectTaskAndProjectDeveloper(projectTask, projectDeveloper);
        try {
            projectTaskDeveloperService.insertNewProjectTaskDeveloper(newProjectTaskDeveloper);
        } catch (ServiceException serviceException) {
            webErrorBean.setError(ON, ERROR_TITLE, serviceException.getMessage());
        }

        return REDIRECT_TO_PROJECTTASKS_DEVELOPERS;
    }
}
