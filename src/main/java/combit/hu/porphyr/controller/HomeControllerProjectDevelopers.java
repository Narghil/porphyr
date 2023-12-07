package combit.hu.porphyr.controller;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTaskDeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import combit.hu.porphyr.service.DeveloperService;
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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import combit.hu.porphyr.controller.HomeControllerHelpers.ProjectPOJO;

import static combit.hu.porphyr.controller.HomeControllerHelpers.*;

@Controller
public class HomeControllerProjectDevelopers {

    private ProjectService projectService;
    private DeveloperService developerService;
    private ProjectTaskService projectTaskService;
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

    static final @NonNull String REDIRECT_TO_PROJECTS = "redirect:/projects";
    static final @NonNull String REDIRECT_TO_PROJECTDEVELOPERS = "redirect:/project_developers";
    static final @NonNull String REDIRECT_TO_PROJECTDEVELOPERS_TASKS = "redirect:/project_developers_tasks";
    static final @NonNull String REDIRECT_TO_PROJECTDEVELOPERS_DELETE = "redirect:/project_developers_delete";

    //------------------ Műveletválasztó ----------------------------------------
    @RequestMapping("/selectProjectDeveloperOperation")
    public String selectOperation(
        @ModelAttribute
        SelectedOperationData selectedOperation
    ) {
        @NonNull String result;
        selectedOperationData.setProjectId(selectedOperation.getProjectId());
        selectedOperationData.setDeveloperId(selectedOperation.getDeveloperId());

        switch (selectedOperation.getOperation()) {
            case TASKS: {
                result = REDIRECT_TO_PROJECTDEVELOPERS_TASKS;
                break;
            }
            case DELETE: {
                result = REDIRECT_TO_PROJECTDEVELOPERS_DELETE;
                break;
            }
            default:
                throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        return result;
    }

    //------------------ Projekt fejlesztőinek listája  ---------------------------------
    @RequestMapping("/project_developers")
    public String projectDevelopers(
        Model model
    ) throws ExecutionException, InterruptedException {
        String result = "project_developers";
        final @NonNull Long id = selectedOperationData.getProjectId();
        final @Nullable ProjectEntity project = projectService.getProjectById(id);
        final @NonNull List<DeveloperEntity> allDevelopers = developerService.getDevelopers();
        if (project != null) {
            final @Nullable ProjectPOJO projectPOJO = new ProjectPOJO();
            projectPOJO.setId(project.getId());
            projectPOJO.setName(project.getName());
            projectPOJO.setDescription(project.getDescription());
            projectPOJO.setDevelopers(project.getProjectDevelopers());
            model.addAttribute("project", projectPOJO);
            model.addAttribute("error", HomeControllerHelpers.getWebError());
            model.addAttribute(
                "assignAbleDevelopers",
                allDevelopers.size() - projectPOJO.getDevelopers().size()
            );
        } else {
            result = REDIRECT_TO_PROJECTS;
        }
        return result;
    }

    //------------------ Új fejlesztő felvétele a projekthez---------------------------------
    @RequestMapping("/project_developers_new")
    public String newProjectDeveloper(
        Model model
    ) throws InterruptedException, ExecutionException {
        final @NonNull Long projectId = selectedOperationData.getProjectId();
        //A projekthez nem tartozó fejlesztők listájának összeállítása
        final @NonNull ProjectEntity project = Objects.requireNonNull(projectService.getProjectById(projectId));
        final @NonNull List<ProjectDeveloperEntity> projectDevelopers = project.getProjectDevelopers();
        final @NonNull List<DeveloperEntity> allDevelopers = developerService.getDevelopers();
        final @NonNull SelectedOperationData formProjectIdDeveloperId = new SelectedOperationData();

        formProjectIdDeveloperId.setProjectId(projectId);
        for (ProjectDeveloperEntity projectDeveloper : projectDevelopers) {
            allDevelopers.removeIf(aDeveloper -> (
                aDeveloper.getId() != null && aDeveloper.getId().equals(projectDeveloper.getDeveloperEntity().getId()))
            );
        }
        model.addAttribute("freeDevelopers", allDevelopers);
        model.addAttribute("projectName", project.getName());
        model.addAttribute("newProjectIdDeveloperId", formProjectIdDeveloperId);
        return "project_developers_new";
    }

    @RequestMapping("/insertNewProjectDeveloper")
    public String insertNewProjectDeveloper(
        @ModelAttribute
        SelectedOperationData newProjectIdDeveloperId
    ) throws InterruptedException, ExecutionException {
        try {
            projectDeveloperService.insertNewProjectDeveloper(
                new ProjectDeveloperEntity(
                    Objects.requireNonNull(projectService.getProjectById(newProjectIdDeveloperId.getProjectId())),
                    Objects.requireNonNull(developerService.getDeveloperById(newProjectIdDeveloperId.getDeveloperId()))
                )
            );
        } catch (ServiceException serviceException) {
            HomeControllerHelpers.webError.setError("ON", ERROR_TITLE, serviceException.getMessage());
        }
        return REDIRECT_TO_PROJECTDEVELOPERS;
    }

    //------------------------ Fejlesztő eltávolítása a projektből
    @RequestMapping("/project_developers_delete")
    public @NonNull String deleteProjectDeveloper(
    ) throws InterruptedException, ExecutionException {
        final @Nullable ProjectEntity project = projectService.getProjectById(selectedOperationData.getProjectId());
        final @Nullable DeveloperEntity developer = developerService.getDeveloperById(selectedOperationData.getDeveloperId());
        if (project != null && developer != null) {
            final @Nullable ProjectDeveloperEntity projectDeveloper =
                projectDeveloperService.getProjectDeveloperByProjectAndDeveloper(project, developer);
            if (projectDeveloper != null) {
                try {
                    projectDeveloperService.deleteProjectDeveloper(projectDeveloper);
                } catch (ServiceException serviceException) {
                    HomeControllerHelpers.webError.setError("ON", ERROR_TITLE, serviceException.getMessage());
                }
            } else {
                throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
            }
        } else {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        return REDIRECT_TO_PROJECTDEVELOPERS;
    }

    //------------------------ Fejlesztő feladatai a projektben
    @RequestMapping("/project_developers_tasks")
    public @NonNull String tasksOfProjectDeveloper(
        Model model
    ) throws InterruptedException, ExecutionException {
        final @NonNull Long projectId = selectedOperationData.getProjectId();
        final @NonNull Long developerId = selectedOperationData.getDeveloperId();

        ProjectEntity project = projectService.getProjectById(projectId);
        DeveloperEntity developer = developerService.getDeveloperById(developerId);

        List<ProjectTaskDeveloperEntity> tasks = projectTaskDeveloperService.getProjectTaskDeveloperByProjectIdAndDeveloperId(
            projectId, developerId
        );

        List<ProjectTaskEntity> allProjectTasks = projectTaskService.getProjectTasksByProjectEntity(
            Objects.requireNonNull(project)
        );
        List<ProjectTaskDeveloperEntity> allProjectTasksDeveloper =
            projectTaskDeveloperService.getProjectTaskDeveloperByProjectIdAndDeveloperId(
                projectId, developerId
            );

        model.addAttribute("error", HomeControllerHelpers.getWebError());
        model.addAttribute("tasks", tasks);
        model.addAttribute("project", project);
        model.addAttribute("developer", developer);
        model.addAttribute("selectedOperationData", selectedOperationData);
        model.addAttribute(
            "assignAbleProjectTasks",
            allProjectTasks.size() - allProjectTasksDeveloper.size()
        );
        return "project_developers_tasks";
    }

    @RequestMapping("/project_developers_tasks_modify_time")
    public @NonNull String projectDevelopersTasksSpendTime(
        @ModelAttribute
        SelectedOperationData spendTimeData
    ) throws InterruptedException, ExecutionException {
        if (spendTimeData.getLongData() != null) {
            final ProjectTaskDeveloperEntity projectDeveloperTask =
                projectTaskDeveloperService.getProjectTaskDeveloperById(spendTimeData.getProjectTaskId());
            if (projectDeveloperTask != null) {
                projectDeveloperTask.setSpendTime(spendTimeData.getLongData());
                projectTaskDeveloperService.modifyProjectTaskDeveloper(projectDeveloperTask);
            } else {
                throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
            }
        } else {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }

        return REDIRECT_TO_PROJECTDEVELOPERS_TASKS;
    }

    //---------------------------- Új projektfeladat hozzárendelése a fejlesztőhöz.
    @RequestMapping("/project_developers_tasks_new")
    public String newProjectDeveloperTask(
        Model model
    ) throws InterruptedException, ExecutionException {
        final @NonNull Long projectId = selectedOperationData.getProjectId();
        final @Nullable ProjectEntity projectEntity = projectService.getProjectById(projectId);
        if( projectEntity == null){
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        final @NonNull Long developerId = selectedOperationData.getDeveloperId();
        final @Nullable DeveloperEntity developerEntity = developerService.getDeveloperById(projectId);
        if( developerEntity == null){
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        selectedOperationData.setProjectDeveloperId(
            Objects.requireNonNull(Objects.requireNonNull(projectDeveloperService.getProjectDeveloperByProjectAndDeveloper(
                Objects.requireNonNull(projectService.getProjectById(selectedOperationData.getProjectId())),
                Objects.requireNonNull(developerService.getDeveloperById(selectedOperationData.getDeveloperId()))
            )).getId())
        );
        //A projekten belül, a fejlesztőhöz nem tartozó feladatok listájának összeállítsa
        //-- Az összes projektfeladat
        final @NonNull List<ProjectTaskEntity> projectTasks = projectTaskService.getProjectTasksByProjectEntity( projectEntity );
        //-- A fejlesztőnél lévő projektfeladatok
        final @NonNull List<ProjectTaskDeveloperEntity> projectTaskDevelopers =
            projectTaskDeveloperService.getProjectTaskDeveloperByProjectIdAndDeveloperId( projectId, developerId)
        ;
        for( ProjectTaskDeveloperEntity projectTaskDeveloper : projectTaskDevelopers ){
            projectTasks.removeIf( aProjectTask -> (
                    aProjectTask == projectTaskDeveloper.getProjectTaskEntity()
                )
            );
        }
        model.addAttribute("freeProjectTasks", projectTasks );
        model.addAttribute("projectName", projectEntity.getName());
        model.addAttribute("developerName", developerEntity.getName());
        model.addAttribute("formData", new SelectedOperationData());

        return "project_developers_tasks_new";
    }

    @RequestMapping("/insertNewProjectTaskDeveloper")
    public String insertNewProjectTaskDeveloper(
        @ModelAttribute
        SelectedOperationData formData
    ) throws InterruptedException, ExecutionException {
        try {
            ProjectTaskEntity projectTaskEntity = projectTaskService.getProjectTaskById(formData.getProjectTaskId());
            if( projectTaskEntity == null ){
                throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
            }
            ProjectTaskDeveloperEntity projectTaskDeveloperEntity = new ProjectTaskDeveloperEntity();
            projectTaskDeveloperEntity.setProjectTaskEntity( projectTaskEntity );
            projectTaskDeveloperEntity.setProjectDeveloperEntity( Objects.requireNonNull(
                projectDeveloperService.getProjectDeveloperById( selectedOperationData.getProjectDeveloperId() )
            ));
            projectTaskDeveloperService.insertNewProjectTaskDeveloper( projectTaskDeveloperEntity );
        } catch (ServiceException serviceException) {
            HomeControllerHelpers.webError.setError("ON", ERROR_TITLE, serviceException.getMessage());
        }
        return REDIRECT_TO_PROJECTDEVELOPERS_TASKS;
    }
}
