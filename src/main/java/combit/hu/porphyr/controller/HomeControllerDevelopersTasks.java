package combit.hu.porphyr.controller;

import combit.hu.porphyr.controller.helpers.SessionData;
import combit.hu.porphyr.controller.helpers.TemplateData;
import combit.hu.porphyr.controller.helpers.WebErrorBean;
import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskDeveloperEntity;
import combit.hu.porphyr.service.ProjectTaskDeveloperService;
import combit.hu.porphyr.service.ServiceException;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static combit.hu.porphyr.Constants.ON;
import static combit.hu.porphyr.controller.helpers.HomeControllerConstants.*;

@Controller
public class HomeControllerDevelopersTasks {

    private ProjectTaskDeveloperService projectTaskDeveloperService;

    @Autowired
    public void setProjectTaskDeveloperService(ProjectTaskDeveloperService projectTaskDeveloperService) {
        this.projectTaskDeveloperService = projectTaskDeveloperService;
    }

    @Resource(name = "getWebErrorBean")
    WebErrorBean webErrorBean;
    @Resource(name = "getSessionData")
    SessionData sessionData;

    static final @NonNull String REDIRECT_TO_DEVELOPER_TASKS = "redirect:/developer_tasks";

    //------------------ Fejlesztő feladatai ------------------------------------
    @RequestMapping("/developer_tasks")
    public @NonNull String tasksOfDeveloper(
        Model model
    ) throws InterruptedException, ExecutionException {
        final @NonNull DeveloperEntity developer = sessionData.getSelectedDeveloper();
        final @Nullable Long developerId = developer.getId();

        if (developerId == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        List<ProjectTaskDeveloperEntity> allProjectTasksDeveloper = projectTaskDeveloperService.getProjectTasksDeveloperByDeveloperId(
            Objects.requireNonNull(developerId)
        );
        model.addAttribute("error", webErrorBean.getWebErrorData());
        model.addAttribute("allProjectTasksDeveloper", allProjectTasksDeveloper);
        model.addAttribute("developer", developer);
        model.addAttribute("dataFromTemplate", sessionData.getDataFromTemplate());
        return "developer_tasks";
    }

    //---------------- Fejlesztő feladatának módosítása
    @PostMapping("/modifyDeveloperTask")
    public @NonNull String modifyDeveloperTask(
        @ModelAttribute
        @NonNull
        TemplateData dataFromTemplate
    ) throws InterruptedException, ExecutionException {
        @Nullable Long id = dataFromTemplate.getId();
        @Nullable Long time = dataFromTemplate.getLongData();
        @NonNull String result = REDIRECT_TO_DEVELOPER_TASKS;
        if (id == null || time == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        ProjectTaskDeveloperEntity projectTaskDeveloper = projectTaskDeveloperService.getProjectTaskDeveloperById(id);
        if (projectTaskDeveloper == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        projectTaskDeveloper.setSpendTime(time);
        try {
            projectTaskDeveloperService.modifyProjectTaskDeveloper(projectTaskDeveloper);
        } catch (ServiceException serviceException) {
            webErrorBean.setError(ON, ERROR_TITLE, serviceException.getMessage());
        }
        return result;
    }
}