package combit.hu.porphyr.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectDevelopersEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTasksEntity;
import combit.hu.porphyr.service.DeveloperService;
import combit.hu.porphyr.service.ProjectService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import static combit.hu.porphyr.Constants.WEBNEWLINE;

@RestController
public class ApiController {

    private ProjectService projectService;
    private DeveloperService developerService;

    private final @NonNull ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    @Autowired
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Autowired
    public void setDeveloperService(DeveloperService developerService) {
        this.developerService = developerService;
    }

    @RequestMapping("/projects_rest")
    public String projects() throws JsonProcessingException {
        return( ow.writeValueAsString(projectService.getProjects()));
    }

    @RequestMapping("/developers_rest")
    public String developers() throws JsonProcessingException {
        return( ow.writeValueAsString(developerService.getDevelopers()));
    }
}
