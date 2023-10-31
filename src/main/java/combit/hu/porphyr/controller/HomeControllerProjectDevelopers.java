package combit.hu.porphyr.controller;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTaskDeveloperEntity;
import combit.hu.porphyr.service.DeveloperService;
import combit.hu.porphyr.service.ProjectDeveloperService;
import combit.hu.porphyr.service.ProjectService;
import combit.hu.porphyr.service.ProjectTaskDeveloperService;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import combit.hu.porphyr.controller.HomeControllerHelpers.ProjectPOJO;

@Controller
public class HomeControllerProjectDevelopers {

    private ProjectService projectService;
    private DeveloperService developerService;
    private ProjectDeveloperService projectDeveloperService;
    private ProjectTaskDeveloperService projectTaskDeveloperService;

    @Autowired
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Autowired
    public void setDeveloperService(DeveloperService developerService) {
        this.developerService = developerService;
    }

    @Autowired
    public void setProjectDeveloperService(ProjectDeveloperService projectDeveloperService) {
        this.projectDeveloperService = projectDeveloperService;
    }

    @Autowired
    public void setProjectTaskDeveloperService(ProjectTaskDeveloperService projectTaskDeveloperService) {
        this.projectTaskDeveloperService = projectTaskDeveloperService;
    }

    static final @NonNull String REDIRECT_TO_PROJECTS = "redirect:/projects";
    static final @NonNull String REDIRECT_TO_PROJECTDEVELOPERS = "redirect:/project_developers";
    static final @NonNull String REDIRECT_TO_PROJECTDEVELOPERS_TASKS = "redirect:/project_developers_tasks";

    //------------------ Projekt fejlesztőinek listája  ---------------------------------
    @RequestMapping("/project_developers/{id}")
    public String projectDevelopers(
        Model model,
        @PathVariable(value = "id")
        Long id
    ) throws ExecutionException, InterruptedException {
        String result = "project_developers";
        final @Nullable ProjectEntity project = projectService.getProjectById(id);
        if (project != null) {
            final @Nullable ProjectPOJO projectPOJO = new ProjectPOJO();
            projectPOJO.setId(project.getId());
            projectPOJO.setName(project.getName());
            projectPOJO.setDescription(project.getDescription());
            projectPOJO.setDevelopers(project.getProjectDevelopers());
            model.addAttribute("project", projectPOJO);
        } else {
            result = REDIRECT_TO_PROJECTS;
        }
        return result;
    }

    //------------------ Új fejlesztő felvétele a projekthez---------------------------------
    @RequestMapping("/project_developers_new/{id}")
    public String newProjectDeveloper(
        Model model,
        @PathVariable(value = "id")
        Long id
    ) throws InterruptedException, ExecutionException {
        //A projekthez nem tartozó fejlesztők listájának összeállítása
        final @NonNull ProjectEntity project = Objects.requireNonNull(projectService.getProjectById(id));
        final @NonNull List<ProjectDeveloperEntity> projectDevelopers = project.getProjectDevelopers();
        final @NonNull List<DeveloperEntity> allDevelopers = developerService.getDevelopers();

        for (ProjectDeveloperEntity projectDeveloper : projectDevelopers) {
            allDevelopers.removeIf(aDeveloper -> (
                aDeveloper.getId() != null && aDeveloper.getId().equals(projectDeveloper.getDeveloperEntity().getId()))
            );
        }
        model.addAttribute("freeDevelopers", allDevelopers);
        model.addAttribute("projectId", id);
        model.addAttribute("projectName", project.getName());
        return "project_developers_new";
    }

    @RequestMapping("/insertNewProjectDeveloper/{projectId}/{developerId}")
    public String insertNewProjectDeveloper(
        @PathVariable(value = "projectId")
        Long projectId,
        @PathVariable(value = "developerId")
        Long developerId
    ) throws InterruptedException, ExecutionException {
        projectDeveloperService.insertNewProjectDeveloper(
            new ProjectDeveloperEntity(
                Objects.requireNonNull(projectService.getProjectById(projectId)),
                Objects.requireNonNull(developerService.getDeveloperById(developerId))
            )
        );
        return REDIRECT_TO_PROJECTDEVELOPERS + "/" + projectId;
    }

    //------------------------ Fejlesztő eltávolítása a projektből
    @RequestMapping("/project_developers_delete/{projectId}/{developerId}")
    public @NonNull String deleteProjectDeveloper(
        @PathVariable(value = "projectId")
        Long projectId,
        @PathVariable(value = "developerId")
        Long developerId
    ) throws InterruptedException, ExecutionException {
        projectDeveloperService.deleteProjectDeveloper(
            Objects.requireNonNull(
                projectDeveloperService.getProjectDeveloperByProjectAndDeveloper(
                    Objects.requireNonNull(projectService.getProjectById(projectId)),
                    Objects.requireNonNull(developerService.getDeveloperById(developerId))
                ))
        );
        return REDIRECT_TO_PROJECTDEVELOPERS + "/" + projectId;
    }

    //------------------------ Fejlesztő feladatai a projektben
    @RequestMapping("/project_developers_tasks/{projectId}/{developerId}")
    public @NonNull String tasksOfProjectDeveloper(
        Model model,
        @PathVariable(value = "projectId")
        Long projectId,
        @PathVariable(value = "developerId")
        Long developerId
    ) throws InterruptedException, ExecutionException {
        ProjectEntity project = projectService.getProjectById(projectId);
        DeveloperEntity developer = developerService.getDeveloperById(developerId);

        List<ProjectTaskDeveloperEntity> tasks = projectTaskDeveloperService.getProjectTaskDeveloperByProjectIdAndDeveloperId( projectId, developerId) ;

        model.addAttribute("tasks", tasks );
        model.addAttribute( "project", project);
        model.addAttribute( "developer", developer);
        return "project_developers_tasks";
    }
}
