package combit.hu.porphyr.controller;

import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.service.ProjectService;
import combit.hu.porphyr.service.ServiceException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.ExecutionException;

import static combit.hu.porphyr.controller.HomeControllerHelpers.*;

@Controller
public class HomeControllerProjectTasks {

    private ProjectService projectService;

    @Autowired
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    //-------------- Projekt feladatainak listája
    @RequestMapping("/project_tasks")
    public @NonNull String loadDataBeforeProjectTasks(
        Model model
    ) throws InterruptedException, ExecutionException {
        final ProjectEntity project = projectService.getProjectById(selectedOperationData.getProjectId());
        if (project == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        final HomeControllerHelpers.WebError webError = getWebError();
        model.addAttribute("error", webError);
        model.addAttribute("project", project);
        model.addAttribute("selectedOperation", selectedOperationData );
        return "project_tasks";
    }
}
