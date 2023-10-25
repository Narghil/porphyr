package combit.hu.porphyr.controller;

import javax.servlet.http.HttpServletRequest;

import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.service.ProjectService;
import combit.hu.porphyr.service.ServiceException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.ExecutionException;

@Controller
public class HomeControllerProjects {

    private ProjectService projectService;

    @Autowired
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    static final @NonNull String REDIRECT_TO_PROJECTS = "redirect:/projects";
    static final @NonNull String REDIRECT_TO_ROOT = "redirect:/";

    @RequestMapping("/project_new")
    public String newProject(Model model) {
        final @NonNull ProjectPOJO newProject = new ProjectPOJO();
        newProject.setName("Teszt Projekt");
        newProject.setDescription("Teszt projekt leírása");
        model.addAttribute("newProject", newProject);
        return "project_new";
    }

    @PostMapping("/insertNewProject")
    public String insertNewProject(
        @ModelAttribute
        ProjectPOJO project
    ) throws Exception
    {
        final @NonNull ProjectEntity newProject = new ProjectEntity();
        newProject.setName(project.getName());
        newProject.setDescription(project.getDescription());
        projectService.insertNewProject(newProject);
        return REDIRECT_TO_PROJECTS;
    }

    @RequestMapping("/project_delete/{id}")
    public String deleteProject(
        @PathVariable(value = "id")
        Long id
    ) throws InterruptedException, ExecutionException //Here SonarLint does not accepts "Exception."
    {
        final @Nullable ProjectEntity project = projectService.getProjectById(id);
        if (project != null) {
            projectService.deleteProject(project);
        }else {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        return REDIRECT_TO_PROJECTS;
    }

    //---------------------------------------------

    @RequestMapping("/project_modify/{id}")
    public String modifyProjectBefore(
        Model model,
        @PathVariable(value = "id")
        Long id
    ) throws Exception {
        String result = "project_modify";
        final @Nullable ProjectEntity project = projectService.getProjectById(id);
        if (project != null) {
            final @Nullable ProjectPOJO projectPOJO = new ProjectPOJO();
            projectPOJO.setId(project.getId());
            projectPOJO.setName(project.getName());
            projectPOJO.setDescription(project.getDescription());
            model.addAttribute("project", projectPOJO);
        } else {
            result = REDIRECT_TO_ROOT;
        }
        return result;
    }

    @PostMapping("/modifyProject")
    public String modifyProject(
        @ModelAttribute
        ProjectPOJO project
    ) throws Exception
    {
        if (project.getId() != null) {
            final @Nullable ProjectEntity modifiedProject = projectService.getProjectById(project.getId());
            if( modifiedProject != null ) {
                modifiedProject.setName(project.getName());
                modifiedProject.setDescription(project.getDescription());
                projectService.modifyProject(modifiedProject);
            } else {
                throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
            }
        } else {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        return REDIRECT_TO_PROJECTS;
    }

    @ExceptionHandler(Exception.class)
    public String exceptionHandler(HttpServletRequest rA, Exception ex, Model model) {
        return new HomeControllerExceptionHandler().exceptionHandler( rA, ex, model );
    }

    //------------ Helpers ----------------------
    @Setter
    @Getter
    static final class ProjectPOJO {
        private @Nullable Long id;
        private @NonNull String name;
        private @Nullable String description;
    }
}
