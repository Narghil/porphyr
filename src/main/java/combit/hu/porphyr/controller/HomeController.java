package combit.hu.porphyr.controller;

import combit.hu.porphyr.controller.helpers.SessionData;
import combit.hu.porphyr.controller.helpers.WebErrorBean;
import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.service.DeveloperService;
import combit.hu.porphyr.service.ProjectService;
import combit.hu.porphyr.config.service.UserService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class HomeController {

    private final @NonNull ProjectService projectService;
    private final @NonNull DeveloperService developerService;
    private final @NonNull UserService userService;

    @Autowired
    public HomeController(
        final @NonNull ProjectService projectService,
        final @NonNull DeveloperService developerService,
        final @NonNull UserService userService
    ) {
        this.projectService = projectService;
        this.developerService = developerService;
        this.userService = userService;
    }

    @Resource(name = "getWebErrorBean")
    WebErrorBean webErrorBean;

    @Resource(name = "getSessionData")
    SessionData sessionData;

    private static final String ERROR = "error";

    @RequestMapping("/")
    public String root(final @NonNull Model model) {

        ArrayList<String> userRoleNames = new ArrayList<>();
        ArrayList<String> userPermitNames = new ArrayList<>();
        ArrayList<String> userPermittedRequestCalls = new ArrayList<>();
        ArrayList<DeveloperEntity> userDevelopers = new ArrayList<>();

        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        sessionData.setSelectedProjectId(0L);
        if (userService.getActualUserPermits(
            userRoleNames,
            userPermitNames,
            userPermittedRequestCalls,
            userDevelopers
        )) {
            sessionData.setUserLoginName(auth.getName());
            sessionData.setUserRoleNames(userRoleNames);
            sessionData.setUserPermitNames(userPermitNames);
            sessionData.setUserPermittedRequestCalls(userPermittedRequestCalls);
            sessionData.setUserDevelopers(userDevelopers);
        }

        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        model.addAttribute("userPermitNames", sessionData.getUserPermitNames());
        return "porphyr";
    }

    @RequestMapping("/projects")
    public @NonNull String projects(final @NonNull Model model) throws ExecutionException, InterruptedException {
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        model.addAttribute("projectsList", projectService.getActualUserProjects());
        model.addAttribute("dataFromTemplate", sessionData.getDataFromTemplate());
        model.addAttribute("userEditLevel", sessionData.getUserEditLevel());
        return "projects";
    }

    @RequestMapping("/developers")
    public @NonNull String developers(final @NonNull Model model) throws ExecutionException, InterruptedException {
        final @NonNull List<DeveloperEntity> developerList = developerService.getDevelopers();
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        model.addAttribute("developers", developerList);
        model.addAttribute("dataFromTemplate", sessionData.getDataFromTemplate());
        model.addAttribute("userEditLevel", sessionData.getUserEditLevel());
        return "developers";
    }
}
