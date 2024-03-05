package combit.hu.porphyr.controller;

import combit.hu.porphyr.controller.helpers.SessionData;
import combit.hu.porphyr.controller.helpers.WebErrorBean;
import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.service.DeveloperService;
import combit.hu.porphyr.service.ProjectService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class HomeControllerRoot {

    private final @NonNull ProjectService projectService;
    private final @NonNull DeveloperService developerService;

    @Autowired
    public HomeControllerRoot(
        final @NonNull ProjectService projectService,
        final @NonNull DeveloperService developerService
    ) {
        this.projectService = projectService;
        this.developerService = developerService;
    }

    @Resource(name = "getWebErrorBean")
    WebErrorBean webErrorBean;

    @Resource(name = "getSessionData")
    SessionData sessionData;

    private static final String ERROR = "error";

    @RequestMapping("/")
    public String root(final @NonNull Model model) {
        sessionData.setSelectedProjectId(0L);
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        return "porphyr";
    }

    @RequestMapping("/projects")
    public @NonNull String projects(final @NonNull Model model) throws ExecutionException, InterruptedException {
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        model.addAttribute("projectsList", projectService.getProjects());
        model.addAttribute("dataFromTemplate", sessionData.getDataFromTemplate());
        return "projects";
    }

    @RequestMapping("/developers")
    public @NonNull String developers(final @NonNull Model model) throws ExecutionException, InterruptedException {
        final @NonNull List<DeveloperEntity> developerList = developerService.getDevelopers();
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        model.addAttribute("developers", developerList);
        model.addAttribute("dataFromTemplate", sessionData.getDataFromTemplate());
        return "developers";
    }
}
