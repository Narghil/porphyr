package combit.hu.porphyr.controller;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.service.DeveloperService;
import combit.hu.porphyr.service.ProjectService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class HomeControllerRoot {

    private ProjectService projectService;
    private DeveloperService developerService;

    @Autowired
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Autowired
    public void setDeveloperService(DeveloperService developerService) {
        this.developerService = developerService;
    }

    @RequestMapping("/")
    public String root(Model model) {
        return "porphyr";
    }

    @RequestMapping("/projects")
    public @NonNull String projects(Model model) throws ExecutionException, InterruptedException {
        model.addAttribute("error", HomeControllerHelpers.getWebError());
        model.addAttribute("projectsList", projectService.getProjects());
        return "projects";
    }

    @RequestMapping("/developers")
    public @NonNull String developers(Model model) throws ExecutionException, InterruptedException {
        List<DeveloperEntity> developerList;
        developerList = developerService.getDevelopers();
        model.addAttribute("developers", developerList);
        return "developers";
    }
}
