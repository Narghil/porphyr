package combit.hu.porphyr.controller;

import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.service.ProjectService;
import combit.hu.porphyr.service.ServiceException;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.ExecutionException;

import static combit.hu.porphyr.controller.HomeControllerHelpers.*;

@Controller
public class HomeControllerProjects {

    private ProjectService projectService;

    @Autowired
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    static final @NonNull String REDIRECT_TO_PROJECTS = "redirect:/projects";
    static final @NonNull String REDIRECT_TO_MODIFY = "redirect:/project_modify";
    static final @NonNull String REDIRECT_TO_NEW = "redirect:/project_new";

    //------------------ Műveletválasztó ----------------------------------------
    @RequestMapping("/selectProjectOperation")
    public String selectOperation(
        @ModelAttribute
        SelectedOperationData selectedOperation
    ) {
        @NonNull String result;
        selectedOperationData.setProjectId(selectedOperation.getProjectId());

        switch (selectedOperation.getOperation()) {
            case DEVELOPERS: {
                result = "redirect:/project_developers";
                break;
            }
            case TASKS: {
                result = "redirect:/project_tasks";
                break;
            }
            case MODIFY: {
                projectPOJO.setId(null);
                result = REDIRECT_TO_MODIFY;
                break;
            }
            default:
                throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        return result;
    }

    //------------------ Új projekt felvitele ---------------------------------
    @RequestMapping("/project_new_start")
    public String startNewProject(Model model) {
        projectPOJO.set(null, "Teszt projekt", "Teszt projekt leírása");
        return REDIRECT_TO_NEW;
    }

    @RequestMapping("/project_new")
    public String newProject(Model model) {
        model.addAttribute("newProject", projectPOJO);
        model.addAttribute("error", HomeControllerHelpers.getWebError());
        return "project_new";
    }

    @PostMapping("/insertNewProject")
    public String insertNewProject(
        @ModelAttribute
        ProjectPOJO project
    ) throws InterruptedException, ExecutionException
    {
        @NonNull String result = REDIRECT_TO_PROJECTS;
        final @NonNull ProjectEntity newProject = new ProjectEntity();
        newProject.setName(project.getName());
        newProject.setDescription(project.getDescription());
        try {
            projectService.insertNewProject(newProject);
        } catch (ServiceException serviceException) {
            HomeControllerHelpers.webError.setError("ON", ERROR_TITLE, serviceException.getMessage());
            projectPOJO.set(project);
            result = REDIRECT_TO_NEW;
        }
        return result;
    }

    //------------------ Projekt törlése ---------------------------------
    @RequestMapping("/project_delete")
    public String deleteProject(
    ) throws InterruptedException, ExecutionException //Here SonarLint does not accepts "Exception."
    {
        @NonNull String result = REDIRECT_TO_PROJECTS;
        final @NonNull Long id = selectedOperationData.getProjectId();
        final @Nullable ProjectEntity project = projectService.getProjectById(id);
        if (project != null) {
            try {
                projectService.deleteProject(project);
            } catch (ServiceException serviceException) {
                result = REDIRECT_TO_MODIFY;
                HomeControllerHelpers.webError.setError("ON", ERROR_TITLE, serviceException.getMessage());
            }
        }else {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        return result;
    }

    //------------------ Projekt módosítása ---------------------------------
    @RequestMapping("/project_modify")
    public String loadDataBeforeModifyProject(
        Model model
    ) throws InterruptedException,ExecutionException {
        String result = "project_modify";
        if (projectPOJO.getId() == null) {
            final @NonNull Long id = selectedOperationData.getProjectId();
            final @Nullable ProjectEntity project = projectService.getProjectById(id);
            if (project != null) {
                projectPOJO.set(project);
            } else {
                HomeControllerHelpers.webError.setError("ON", ERROR_TITLE,
                    ServiceException.Exceptions.NULL_VALUE.getDescription()
                )
                ;
            }
        }
        model.addAttribute("error", getWebError());
        model.addAttribute("project", projectPOJO);

        return result;
    }

    @PostMapping("/modifyProject")
    public String modifyProject(
        @ModelAttribute
        ProjectPOJO project
    ) throws InterruptedException, ExecutionException
    {
        @NonNull String result = REDIRECT_TO_PROJECTS;
        if (project.getId() != null) {
            final @Nullable ProjectEntity modifiedProject = projectService.getProjectById(project.getId());
            if( modifiedProject != null ) {
                modifiedProject.setName(project.getName());
                modifiedProject.setDescription(project.getDescription());
                try {
                    projectService.modifyProject(modifiedProject);
                    projectPOJO.setId(null);
                } catch (ServiceException serviceException) {
                    HomeControllerHelpers.webError.setError("ON", ERROR_TITLE, serviceException.getMessage());
                    result = REDIRECT_TO_MODIFY;
                }
            } else {
                throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
            }
        } else {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        return result;
    }
}
