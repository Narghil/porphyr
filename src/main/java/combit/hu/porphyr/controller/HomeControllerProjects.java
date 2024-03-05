package combit.hu.porphyr.controller;

import combit.hu.porphyr.controller.helpers.TemplateData;
import combit.hu.porphyr.controller.helpers.SessionData;
import combit.hu.porphyr.controller.helpers.WebErrorBean;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.service.ProjectService;
import combit.hu.porphyr.service.PorphyrServiceException;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static combit.hu.porphyr.Constants.ON;
import static combit.hu.porphyr.controller.helpers.HomeControllerConstants.*;

@Controller
public class HomeControllerProjects {

    private final @NonNull ProjectService projectService;

    @Autowired
    public HomeControllerProjects(final @NonNull ProjectService projectService) {
        this.projectService = projectService;
    }

    @Resource(name = "getWebErrorBean")
    WebErrorBean webErrorBean;
    @Resource(name = "getSessionData")
    SessionData sessionData;

    static final @NonNull String REDIRECT_TO_PROJECTS = "redirect:/projects";
    static final @NonNull String REDIRECT_TO_PROJECT_MODIFY = "redirect:/project_modify";
    static final @NonNull String REDIRECT_TO_PROJECT_NEW = "redirect:/project_new";

    //------------------ Műveletválasztó ----------------------------------------
    @RequestMapping("/selectProjectOperation")
    public @NonNull String selectOperation(
        @ModelAttribute
        @NonNull
        final TemplateData dataFromTemplate
    ) throws InterruptedException, ExecutionException {
        @NonNull
        final String result;
        @Nullable
        final Long id = dataFromTemplate.getId();
        if (id == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        @Nullable ProjectEntity project = projectService.getProjectById(id);
        if (project == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        sessionData.setDataFromTemplate(dataFromTemplate);
        sessionData.setSelectedProjectId(id);
        switch (dataFromTemplate.getOperation()) {
            case MENU_ITEM_DEVELOPERS: {
                result = "redirect:/project_developers";
                break;
            }
            case MENU_ITEM_TASKS: {
                result = "redirect:/project_tasks";
                break;
            }
            case MENU_ITEM_MODIFY: {
                result = startModifyProject();
                break;
            }
            default:
                throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        return result;
    }

    //------------------ Új projekt felvitele ---------------------------------
    @RequestMapping("/project_new_start")
    public @NonNull String startNewProject(final @NonNull Model model) {
        final @NonNull TemplateData dataFromTemplate = sessionData.getDataFromTemplate();
        dataFromTemplate.setId(null);
        dataFromTemplate.setName(null);
        dataFromTemplate.setDescription(null);
        return REDIRECT_TO_PROJECT_NEW;
    }

    @RequestMapping("/project_new")
    public @NonNull String newProject(final @NonNull Model model) {
        sessionData.moveDataFromToTemplate();
        final @NonNull TemplateData dataToTemplate = sessionData.getDataToTemplate();
        dataToTemplate.setId(null);
        model.addAttribute("newProject", dataToTemplate);
        model.addAttribute("error", webErrorBean.getWebErrorData());
        return "project_new";
    }

    @PostMapping("/insertNewProject")
    public @NonNull String insertNewProject(
        @ModelAttribute
        @NonNull
        final TemplateData dataFromTemplate
    ) throws InterruptedException, ExecutionException {
        @NonNull String result = REDIRECT_TO_PROJECTS;
        final @NonNull ProjectEntity newProject = new ProjectEntity();
        newProject.setName(Objects.requireNonNull(dataFromTemplate.getName()));
        newProject.setDescription(dataFromTemplate.getDescription());
        sessionData.setDataFromTemplate(dataFromTemplate);
        try {
            projectService.insertNewProject(newProject);
        } catch (PorphyrServiceException porphyrServiceException) {
            webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
            result = REDIRECT_TO_PROJECT_NEW;
        }
        return result;
    }

    //------------------ Projekt törlése ---------------------------------
    @RequestMapping("/project_delete")
    public @NonNull String deleteProject(
    ) throws InterruptedException, ExecutionException //Here SonarLint does not accepts "Exception."
    {
        @NonNull String result = REDIRECT_TO_PROJECTS;
        final @Nullable ProjectEntity selectedProject = sessionData.getSelectedProject();
        try {
            projectService.deleteProject(selectedProject);
        } catch (PorphyrServiceException porphyrServiceException) {
            result = REDIRECT_TO_PROJECT_MODIFY;
            webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
        }
        return result;
    }

    //------------------ Projekt módosítása ---------------------------------
    public @NonNull String startModifyProject()
        throws InterruptedException, ExecutionException {
        final @NonNull ProjectEntity project = sessionData.getSelectedProject();
        sessionData.getDataFromTemplate().setName(project.getName());
        sessionData.getDataFromTemplate().setDescription(project.getDescription());
        return REDIRECT_TO_PROJECT_MODIFY;
    }

    @RequestMapping("/project_modify")
    public @NotNull String loadDataBeforeModifyProject(
        final @NonNull Model model
    ) {
        String result = "project_modify";
        sessionData.moveDataFromToTemplate();
        final @NotNull TemplateData dataToTemplate = sessionData.getDataToTemplate();
        model.addAttribute("error", webErrorBean.getWebErrorData());
        model.addAttribute("project", dataToTemplate);

        return result;
    }

    @PostMapping("/modifyProject")
    public @NonNull String modifyProject(
        @ModelAttribute
        @NonNull
        final TemplateData dataFromTemplate
    ) throws InterruptedException, ExecutionException {
        @NonNull String result = REDIRECT_TO_PROJECTS;
        sessionData.setDataFromTemplate(dataFromTemplate);

        final @Nullable ProjectEntity modifiedProject = sessionData.getSelectedProject();
        modifiedProject.setName(Objects.requireNonNull(dataFromTemplate.getName()));
        modifiedProject.setDescription(dataFromTemplate.getDescription());

        try {
            projectService.modifyProject(modifiedProject);
        } catch (PorphyrServiceException porphyrServiceException) {
            webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
            result = REDIRECT_TO_PROJECT_MODIFY;
        }

        return result;
    }
}
