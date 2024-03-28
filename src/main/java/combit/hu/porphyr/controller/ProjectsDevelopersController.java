package combit.hu.porphyr.controller;

import combit.hu.porphyr.controller.helpers.TemplateData;
import combit.hu.porphyr.controller.helpers.SessionData;
import combit.hu.porphyr.controller.helpers.WebErrorBean;
import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.service.DeveloperService;
import combit.hu.porphyr.service.ProjectDeveloperService;
import combit.hu.porphyr.service.PorphyrServiceException;
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
import static combit.hu.porphyr.controller.helpers.ControllerConstants.*;

@Controller
public class ProjectsDevelopersController {

    private final @NonNull DeveloperService developerService;
    private final @NonNull ProjectDeveloperService projectDeveloperService;

    @Autowired
    public ProjectsDevelopersController(
        final @NonNull DeveloperService developerService,
        final @NonNull ProjectDeveloperService projectDeveloperService
    ) {
        this.developerService = developerService;
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
        final TemplateData dataFromTemplate
    ) throws ExecutionException, InterruptedException {
        @NonNull
        final String result;
        @Nullable Long developerId = dataFromTemplate.getId();
        if (developerId == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        @Nullable
        final DeveloperEntity developer = developerService.getDeveloperById(dataFromTemplate.getId());
        if (developer == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        sessionData.setSelectedDeveloperId(developerId);
        switch (dataFromTemplate.getOperation()) {
            case MENU_ITEM_TASKS: {
                result = REDIRECT_TO_PROJECTDEVELOPERS_TASKS;
                break;
            }
            case MENU_ITEM_DELETE: {
                deleteProjectDeveloper();
                result = REDIRECT_TO_PROJECTDEVELOPERS;
                break;
            }
            default:
                throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        return result;
    }

    //------------------ Projekt fejlesztőinek listája  ---------------------------------
    @RequestMapping("/project_developers")
    public @NonNull String projectDevelopers(
        final @NonNull Model model
    ) throws ExecutionException, InterruptedException {
        final @NonNull String result = "project_developers";
        final @NonNull List<DeveloperEntity> allDevelopers = developerService.getDevelopers();
        final @Nullable ProjectEntity project = sessionData.getSelectedProject();
        final @NonNull List<ProjectDeveloperEntity> projectDeveloperList = project.getProjectDevelopers();

        for( ProjectDeveloperEntity projectDeveloper : projectDeveloperList){
            projectDeveloperService.getDeveloperFullTimeInProject( projectDeveloper );
        }

        model.addAttribute("error", webErrorBean.getWebErrorData());
        model.addAttribute("project_name", project.getName());
        model.addAttribute("projectDevelopers", projectDeveloperList);
        model.addAttribute("assignAbleDevelopers", allDevelopers.size() - projectDeveloperList.size());
        model.addAttribute("dataFromTemplate", sessionData.getDataFromTemplate());
        return result;
    }

    //------------------ Új fejlesztő felvétele a projekthez---------------------------------
    @RequestMapping("/project_developers_new")
    public @NonNull String newProjectDeveloper(
        final @NonNull Model model
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
        final TemplateData dataFromTemplate
    ) throws InterruptedException, ExecutionException {
        @Nullable
        final Long developerId = dataFromTemplate.getId();
        if (developerId == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        @Nullable DeveloperEntity selectedDeveloper = developerService.getDeveloperById(developerId);
        if (selectedDeveloper == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        sessionData.setSelectedDeveloperId(developerId);
        try {
            projectDeveloperService.insertNewProjectDeveloper(
                new ProjectDeveloperEntity(
                    sessionData.getSelectedProject(),
                    sessionData.getSelectedDeveloper()
                )
            );
        } catch (PorphyrServiceException porphyrServiceException) {
            webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
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
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        try {
            projectDeveloperService.deleteProjectDeveloper(projectDeveloper);
        } catch (PorphyrServiceException porphyrServiceException) {
            webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
        }
    }
}
