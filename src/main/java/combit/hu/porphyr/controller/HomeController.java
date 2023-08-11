package combit.hu.porphyr.controller;

import javax.servlet.http.HttpServletRequest;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.service.DeveloperService;
import combit.hu.porphyr.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class HomeController {

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
    public String projects(Model model ) {
        List<ProjectEntity> projectList;
        projectList = projectService.getProjects();
        model.addAttribute("projects", projectList );
        return "projects";
    }

    @RequestMapping("/insertSimpleData")
    public String newProjects(Model model){
        projectService.insertNewProject(new ProjectEntity("Első projekt","Első projekt leírása."));
        projectService.insertNewProject(new ProjectEntity("Második projekt","Második projekt leírása."));
        projectService.insertNewProject(new ProjectEntity("Harmadik projekt","Harmadik projekt leírása."));

        developerService.insertNewDeveloper( new DeveloperEntity("Első fejlesztő"));
        developerService.insertNewDeveloper( new DeveloperEntity("Második fejlesztő"));
        developerService.insertNewDeveloper( new DeveloperEntity("Harmadik fejlesztő"));
        return "redirect:/";
    }

    @RequestMapping("/developers")
    public String developers(Model model ) {
        List<DeveloperEntity> developerList;
        developerList = developerService.getDevelopers();
        model.addAttribute("developers", developerList );
        return "developers";
    }

    @ExceptionHandler(Exception.class)
    public String exceptionHandler(HttpServletRequest rA, Exception ex, Model model) {
        model.addAttribute("errMessage", ex.getMessage());
        return "exceptionHandler";
    }
}
