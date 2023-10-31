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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.ExecutionException;
import static combit.hu.porphyr.controller.HomeControllerHelpers.ProjectPOJO;

@Controller
public class HomeControllerProjects {

    private ProjectService projectService;

    @Autowired
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    static final @NonNull String REDIRECT_TO_PROJECTS = "redirect:/projects";
    static final @NonNull String REDIRECT_TO_ROOT = "redirect:/";

    //------------------ Új projekt felvitele ---------------------------------
    @RequestMapping("/project_new")
    public String newProject(Model model) {
        final @NonNull ProjectPOJO newProject = new ProjectPOJO();
        newProject.setName("Teszt <b>Projekt</b>");
        newProject.setDescription("Teszt <b>projekt</b> leírása");
        model.addAttribute("newProject", newProject);
        return "project_new";
    }

    @PostMapping("/insertNewProject")
    public String insertNewProject(
        @ModelAttribute
        ProjectPOJO project
    ) throws InterruptedException, ExecutionException
    {
        final @NonNull ProjectEntity newProject = new ProjectEntity();
        newProject.setName(project.getName());
        newProject.setDescription(project.getDescription());
        projectService.insertNewProject(newProject);
        return REDIRECT_TO_PROJECTS;
    }

    //------------------ Projekt törlése ---------------------------------
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

    //------------------ Projekt módosítása ---------------------------------
    @RequestMapping("/project_modify/{id}")
    public String modifyProjectBefore(
        Model model,
        @PathVariable(value = "id")
        Long id
    ) throws InterruptedException,ExecutionException {
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
    ) throws InterruptedException, ExecutionException
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
}
