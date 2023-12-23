package combit.hu.porphyr.controller;

import combit.hu.porphyr.controller.helpers.TemplateData;
import combit.hu.porphyr.controller.helpers.SessionData;
import combit.hu.porphyr.controller.helpers.WebErrorBean;
import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.service.DeveloperService;
import combit.hu.porphyr.service.ProjectDeveloperService;
import combit.hu.porphyr.service.ServiceException;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.Resource;

import static combit.hu.porphyr.Constants.ON;
import static combit.hu.porphyr.controller.helpers.HomeControllerConstants.*;

@Controller
public class HomeControllerProjectsDevelopers {

    private DeveloperService developerService;
    private ProjectDeveloperService projectDeveloperService;

    @Autowired
    public void setDeveloperService(DeveloperService developerService) {
        this.developerService = developerService;
    }

    @Autowired
    public void setProjectDeveloperService(ProjectDeveloperService projectDeveloperService) {
        this.projectDeveloperService = projectDeveloperService;
    }

    @Resource(name = "getWebErrorBean")
    WebErrorBean webErrorBean;
    @Resource(name = "getSessionData")
    SessionData sessionData;

    static final @NonNull String REDIRECT_TO_PROJECTDEVELOPERS = "redirect:/project_developers";
    static final @NonNull String REDIRECT_TO_PROJECTDEVELOPERS_TASKS = "redirect:/project_developers_tasks";

    //------------------ Műveletválasztó ----------------------------------------
    @RequestMapping("/selectProjectDeveloperOperation")
    public @NonNull String selectOperation(
        @ModelAttribute
        @NonNull
        TemplateData dataFromTemplate
    ) throws ExecutionException, InterruptedException {
        @NonNull String result;
        @Nullable Long developerId = dataFromTemplate.getId();
        if (developerId == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        @Nullable DeveloperEntity developer = developerService.getDeveloperById(dataFromTemplate.getId());
        if (developer == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        sessionData.setSelectedDeveloperId(developerId);
        switch (dataFromTemplate.getOperation()) {
            case TASKS: {
                result = REDIRECT_TO_PROJECTDEVELOPERS_TASKS;
                break;
            }
            case DELETE: {
                deleteProjectDeveloper();
                result = REDIRECT_TO_PROJECTDEVELOPERS;
                break;
            }
            default:
                throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        return result;
    }

    //------------------ Projekt fejlesztőinek listája  ---------------------------------
    @RequestMapping("/project_developers")
    public @NonNull String projectDevelopers(
        Model model
    ) throws ExecutionException, InterruptedException {
        String result = "project_developers";
        final @NonNull List<DeveloperEntity> allDevelopers = developerService.getDevelopers();
        final @Nullable ProjectEntity project = sessionData.getSelectedProject();
        List<ProjectDeveloperEntity> projectDeveloperList = project.getProjectDevelopers();

        model.addAttribute("error", webErrorBean.getWebErrorData());
        model.addAttribute("project_name", project.getName());
        model.addAttribute("projectDevelopers", projectDeveloperList);
        final int assignAbleDevelopers = allDevelopers.size() - projectDeveloperList.size();
        model.addAttribute("assignAbleDevelopers", assignAbleDevelopers);
        model.addAttribute( "dataFromTemplate", sessionData.getDataFromTemplate());
        return result;
    }

    //------------------ Új fejlesztő felvétele a projekthez---------------------------------
    @RequestMapping("/project_developers_new")
    public @NonNull String newProjectDeveloper(
        Model model
    ) throws InterruptedException, ExecutionException {
        //A projekthez nem tartozó fejlesztők listájának összeállítása
        final @NonNull ProjectEntity project = sessionData.getSelectedProject();
        final @NonNull List<ProjectDeveloperEntity> projectDevelopers = project.getProjectDevelopers();
        final @NonNull List<DeveloperEntity> allDevelopers = developerService.getDevelopers();

        for (ProjectDeveloperEntity projectDeveloper : projectDevelopers) {
            allDevelopers.removeIf(aDeveloper -> (
                aDeveloper.getId() != null && aDeveloper.getId().equals(projectDeveloper.getDeveloperEntity().getId()))
            );
        }
        model.addAttribute("freeDevelopers", allDevelopers);
        model.addAttribute("projectName", project.getName());
        model.addAttribute("dataFromTemplate", sessionData.getDataFromTemplate());
        return "project_developers_new";
    }

    @RequestMapping("/insertNewProjectDeveloper")
    public @NonNull String insertNewProjectDeveloper(
        @ModelAttribute
        @NonNull
        TemplateData dataFromTemplate
    ) throws InterruptedException, ExecutionException {
        @Nullable Long developerId = dataFromTemplate.getId();
        if (developerId == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        @Nullable DeveloperEntity selectedDeveloper = developerService.getDeveloperById(developerId);
        if (selectedDeveloper == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        sessionData.setSelectedDeveloperId(developerId);
        try {
            projectDeveloperService.insertNewProjectDeveloper(
                new ProjectDeveloperEntity(
                    sessionData.getSelectedProject(),
                    sessionData.getSelectedDeveloper()
                )
            );
        } catch (ServiceException serviceException) {
            webErrorBean.setError(ON, ERROR_TITLE, serviceException.getMessage());
        }
        return REDIRECT_TO_PROJECTDEVELOPERS;
    }

    //------------------------ Fejlesztő eltávolítása a projektből
    public void deleteProjectDeveloper(
    ) throws InterruptedException, ExecutionException {
        final @Nullable ProjectEntity project = sessionData.getSelectedProject();
        final @Nullable DeveloperEntity developer = sessionData.getSelectedDeveloper();
        final @Nullable ProjectDeveloperEntity projectDeveloper =
            projectDeveloperService.getProjectDeveloperByProjectAndDeveloper(project, developer);
        if (projectDeveloper == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        try {
            projectDeveloperService.deleteProjectDeveloper(projectDeveloper);
        } catch (ServiceException serviceException) {
            webErrorBean.setError(ON, ERROR_TITLE, serviceException.getMessage());
        }
    }
}
