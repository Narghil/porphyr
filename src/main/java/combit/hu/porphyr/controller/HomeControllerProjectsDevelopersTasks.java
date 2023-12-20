package combit.hu.porphyr.controller;

import combit.hu.porphyr.controller.helpers.SelectedOperationDataBean;
import combit.hu.porphyr.controller.helpers.WebErrorBean;
import combit.hu.porphyr.domain.DeveloperEntity;
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

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static combit.hu.porphyr.controller.helpers.HomeControllerConstants.*;

@Controller
public class HomeControllerProjectsDevelopersTasks {

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

    @Resource(name="getWebErrorBean")
    WebErrorBean webErrorBean;
    @Resource(name="getSelectedOperationDataBean")
    SelectedOperationDataBean selectedOperationDataBean;

    static final @NonNull String REDIRECT_TO_PROJECTDEVELOPERS_TASKS = "redirect:/project_developers_tasks";

    //------------------ Műveletválasztó ----------------------------------------
    @RequestMapping("/selectProjectDeveloperTaskOperation")
    public @NonNull String selectOperation(
        @ModelAttribute
        @NonNull
        SelectedOperationDataBean selectedOperation
    ) throws InterruptedException, ExecutionException {
        @NonNull String result;
        selectedOperationDataBean.setOperation(selectedOperation.getOperation());
        selectedOperationDataBean.setProjectTaskDeveloperId(selectedOperation.getProjectTaskDeveloperId());
        switch (selectedOperation.getOperation()) {
            case MODIFY: {
                selectedOperationDataBean.setLongData(selectedOperation.getLongData());
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
        final @NonNull Long projectId = selectedOperationDataBean.getProjectId();
        final @NonNull Long developerId = selectedOperationDataBean.getDeveloperId();

        ProjectEntity project = projectService.getProjectById(projectId);
        DeveloperEntity developer = developerService.getDeveloperById(developerId);

        List<ProjectTaskDeveloperEntity> projectTaskDevelopers =
            projectTaskDeveloperService.getProjectTaskDeveloperByProjectIdAndDeveloperId(projectId, developerId);

        List<ProjectTaskEntity> allProjectTasks = projectTaskService.getProjectTasksByProjectEntity(
            Objects.requireNonNull(project)
        );
        List<ProjectTaskDeveloperEntity> allProjectTasksDeveloper =
            projectTaskDeveloperService.getProjectTaskDeveloperByProjectIdAndDeveloperId(projectId, developerId);

        model.addAttribute("error", webErrorBean.getWebErrorData());
        model.addAttribute("projectTaskDevelopers", projectTaskDevelopers);
        model.addAttribute("project", project);
        model.addAttribute("developer", developer);
        model.addAttribute("selectedOperationData", selectedOperationDataBean);
        model.addAttribute(
            "assignAbleProjectTasks",
            allProjectTasks.size() - allProjectTasksDeveloper.size()
        );

        return "project_developers_tasks";
    }

    //-------------------------- Idő módosítása a feladatban
    public void projectDevelopersTasksSpendTime(
    ) throws InterruptedException, ExecutionException {
        if (selectedOperationDataBean.getLongData() != null) {
            final ProjectTaskDeveloperEntity projectTaskDeveloper =
                projectTaskDeveloperService.getProjectTaskDeveloperById(selectedOperationDataBean.getProjectTaskDeveloperId());
            if (projectTaskDeveloper != null) {
                projectTaskDeveloper.setSpendTime(selectedOperationDataBean.getLongData());
                try {
                    projectTaskDeveloperService.modifyProjectTaskDeveloper(projectTaskDeveloper);
                } catch (ServiceException serviceException) {
                    webErrorBean.setError("ON", ERROR_TITLE, serviceException.getMessage());
                }
            } else {
                throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
            }
        } else {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
    }

    //---------------------------- Projektfeladat eltávolítása a fejlesztőtől
    public void removeProjectTaskFromProjectDeveloper(
    ) throws InterruptedException, ExecutionException {
        final ProjectTaskDeveloperEntity projectTaskDeveloper =
            projectTaskDeveloperService.getProjectTaskDeveloperById(selectedOperationDataBean.getProjectTaskDeveloperId());
        if (projectTaskDeveloper != null) {
            try {
                projectTaskDeveloperService.deleteProjectTaskDeveloper(projectTaskDeveloper);
            } catch (ServiceException serviceException) {
                webErrorBean.setError("ON", ERROR_TITLE, serviceException.getMessage());
            }
        } else {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
    }

    //---------------------------- Új projektfeladat hozzárendelése a fejlesztőhöz.
    @RequestMapping("/project_developers_tasks_new")
    public @NonNull String insertNewProjectDeveloperTask(
        Model model
    ) throws InterruptedException, ExecutionException {
        final @NonNull Long projectId = selectedOperationDataBean.getProjectId();
        final @Nullable ProjectEntity projectEntity = projectService.getProjectById(projectId);
        if (projectEntity == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        final @NonNull Long developerId = selectedOperationDataBean.getDeveloperId();
        final @Nullable DeveloperEntity developerEntity = developerService.getDeveloperById(projectId);
        if (developerEntity == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        selectedOperationDataBean.setProjectDeveloperId(
            Objects.requireNonNull(Objects.requireNonNull(projectDeveloperService.getProjectDeveloperByProjectAndDeveloper(
                Objects.requireNonNull(projectService.getProjectById(selectedOperationDataBean.getProjectId())),
                Objects.requireNonNull(developerService.getDeveloperById(selectedOperationDataBean.getDeveloperId()))
            )).getId())
        );
        //A projekten belül, a fejlesztőhöz nem tartozó feladatok listájának összeállítsa
        //-- Az összes projektfeladat
        final @NonNull List<ProjectTaskEntity> projectTasks = projectTaskService.getProjectTasksByProjectEntity(
            projectEntity);
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
        model.addAttribute("projectName", projectEntity.getName());
        model.addAttribute("developerName", developerEntity.getName());
        model.addAttribute("formData", selectedOperationDataBean);

        return "project_developers_tasks_new";
    }

    @RequestMapping("/insertNewProjectTaskDeveloper")
    public @NonNull String insertNewProjectTaskDeveloper(
        @ModelAttribute
        @NonNull
        SelectedOperationDataBean formData
    ) throws InterruptedException, ExecutionException {
        try {
            ProjectTaskEntity projectTaskEntity = projectTaskService.getProjectTaskById(formData.getProjectTaskId());
            if (projectTaskEntity == null) {
                throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
            }
            ProjectTaskDeveloperEntity projectTaskDeveloperEntity = new ProjectTaskDeveloperEntity();
            projectTaskDeveloperEntity.setProjectTaskEntity(projectTaskEntity);
            projectTaskDeveloperEntity.setProjectDeveloperEntity(Objects.requireNonNull(
                projectDeveloperService.getProjectDeveloperById(selectedOperationDataBean.getProjectDeveloperId())
            ));
            projectTaskDeveloperService.insertNewProjectTaskDeveloper(projectTaskDeveloperEntity);
        } catch (ServiceException serviceException) {
            webErrorBean.setError("ON", ERROR_TITLE, serviceException.getMessage());
        }
        return REDIRECT_TO_PROJECTDEVELOPERS_TASKS;
    }
}
