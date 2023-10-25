package combit.hu.porphyr.controller;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.service.DeveloperService;
import combit.hu.porphyr.service.ProjectService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
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

    static final @NonNull String REDIRECT_TO_ROOT = "redirect:/";

    @RequestMapping("/")
    public String root(Model model) {
        return "porphyr";
    }

    @RequestMapping("/projects")
    public String projects(Model model) throws ExecutionException, InterruptedException {
        final @NonNull String projects = "projects";
        model.addAttribute(projects, projectService.getProjects());
        return projects;
    }

    @RequestMapping("/insertSimpleData")
    public String insertSimpleData(Model model) throws ExecutionException, InterruptedException {
        projectService.insertNewProject(new ProjectEntity("Első projekt", "Első projekt leírása."));
        projectService.insertNewProject(new ProjectEntity("Második projekt", "Második projekt leírása."));
        projectService.insertNewProject(new ProjectEntity("Harmadik projekt", "Harmadik projekt leírása."));

        developerService.insertNewDeveloper(new DeveloperEntity("Első fejlesztő"));
        developerService.insertNewDeveloper(new DeveloperEntity("Második fejlesztő"));
        developerService.insertNewDeveloper(new DeveloperEntity("Harmadik fejlesztő"));
        return REDIRECT_TO_ROOT;
    }

    @RequestMapping("/developers")
    public String developers(Model model) throws ExecutionException, InterruptedException {
        List<DeveloperEntity> developerList;
        developerList = developerService.getDevelopers();
        model.addAttribute("developers", developerList);
        return "developers";
    }

    @ExceptionHandler(Exception.class)
    public String exceptionHandler(HttpServletRequest rA, Exception ex, Model model) {
        return new HomeControllerExceptionHandler().exceptionHandler( rA, ex, model );
    }


}
