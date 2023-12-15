package combit.hu.porphyr.controller;

import combit.hu.porphyr.controller.helpers.SelectedOperationDataBean;
import combit.hu.porphyr.controller.helpers.WebErrorBean;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.service.ProjectService;
import combit.hu.porphyr.service.ServiceException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.ExecutionException;
import javax.annotation.Resource;

@Controller
public class HomeControllerProjectTasks {

    private ProjectService projectService;

    @Autowired
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Resource(name="getWebErrorBean")
    WebErrorBean webErrorBean;
    @Resource(name="getSelectedOperationDataBean")
    SelectedOperationDataBean selectedOperationDataBean;

    //-------------- Projekt feladatainak listája
    @RequestMapping("/project_tasks")
    public @NonNull String loadDataBeforeProjectTasks(
        Model model
    ) throws InterruptedException, ExecutionException {
        final ProjectEntity project = projectService.getProjectById(selectedOperationDataBean.getProjectId());
        if (project == null) {
            throw new ServiceException(ServiceException.Exceptions.NULL_VALUE);
        }
        final WebErrorBean webError = webErrorBean.getWebErrorBean();
        model.addAttribute("error", webError);
        model.addAttribute("project", project);
        model.addAttribute("selectedOperation", selectedOperationDataBean );
        return "project_tasks";
    }
}
