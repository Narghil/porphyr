package combit.hu.porphyr.controller.helpers;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTaskDeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import combit.hu.porphyr.service.DeveloperService;
import combit.hu.porphyr.service.ProjectDeveloperService;
import combit.hu.porphyr.service.ProjectService;
import combit.hu.porphyr.service.ProjectTaskDeveloperService;
import combit.hu.porphyr.service.ProjectTaskService;
import combit.hu.porphyr.service.ServiceException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.concurrent.ExecutionException;

@Component
@Setter
@Getter
@ToString
public class SessionData {

    private ProjectService projectService;
    private DeveloperService developerService;
    private ProjectDeveloperService projectDeveloperService;
    private ProjectTaskService projectTaskService;
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
    public void setProjectDeveloperService(ProjectDeveloperService projectDeveloperService) {
        this.projectDeveloperService = projectDeveloperService;
    }

    @Autowired
    public void setProjectTaskService(ProjectTaskService projectTaskService) {
        this.projectTaskService = projectTaskService;
    }

    @Autowired
    public void setProjectTaskDeveloperService(ProjectTaskDeveloperService projectTaskDeveloperService) {
        this.projectTaskDeveloperService = projectTaskDeveloperService;
    }

    @Getter
    private @NonNull TemplateData dataFromTemplate;
    @Getter
    private @NonNull TemplateData dataToTemplate;

    private @NonNull Long selectedProjectId;
    private @NonNull Long selectedDeveloperId;
    private @NonNull Long selectedProjectDeveloperId;
    private @NonNull Long selectedProjectTaskId;
    private @NonNull Long selectedProjectTaskDeveloperId;

    public @NonNull ProjectEntity getSelectedProject() throws InterruptedException, ExecutionException {
        @Nullable ProjectEntity result;
        result = projectService.getProjectById(this.selectedProjectId);
        if (result == null) {
            throw new ServiceException(ServiceException.Exceptions.CONTROLLER_SELECTED_PROJECT_NOT_EXISTS);
        }
        return result;
    }

    public @NonNull DeveloperEntity getSelectedDeveloper() throws InterruptedException, ExecutionException {
        @Nullable DeveloperEntity result;
        result = developerService.getDeveloperById(this.selectedDeveloperId);
        if (result == null) {
            throw new ServiceException(ServiceException.Exceptions.CONTROLLER_SELECTED_DEVELOPER_NOT_EXISTS);
        }
        return result;
    }

    public @NonNull ProjectDeveloperEntity getSelectedProjectDeveloper()
        throws InterruptedException, ExecutionException {
        @Nullable ProjectDeveloperEntity result;
        result = projectDeveloperService.getProjectDeveloperById(this.selectedProjectDeveloperId);
        if (result == null) {
            throw new ServiceException(ServiceException.Exceptions.CONTROLLER_SELECTED_PROJECTDEVELOPER_NOT_EXISTS);
        }
        return result;
    }

    public @NonNull ProjectTaskEntity getSelectedProjectTask() throws InterruptedException, ExecutionException {
        @Nullable ProjectTaskEntity result;
        result = projectTaskService.getProjectTaskById(this.selectedProjectTaskId);
        if (result == null) {
            throw new ServiceException(ServiceException.Exceptions.CONTROLLER_SELECTED_PROJECTTASK_NOT_EXISTS);
        }
        return result;
    }

    public @NonNull ProjectTaskDeveloperEntity getSelectedProjectTaskDeveloper()  throws InterruptedException, ExecutionException {
        @Nullable ProjectTaskDeveloperEntity result;
        result = projectTaskDeveloperService.getProjectTaskDeveloperById(this.selectedProjectTaskDeveloperId);
        if (result == null) {
            throw new ServiceException(ServiceException.Exceptions.CONTROLLER_SELECTED_PROJECTTASKDEVELOPER_NOT_EXISTS);
        }
        return result;
    }

    public SessionData() {
        dataFromTemplate = new TemplateData();
        dataToTemplate = new TemplateData();
        selectedProjectId = 0L;
        selectedDeveloperId = 0L;
        selectedProjectDeveloperId = 0L;
        selectedProjectTaskId = 0L;
        selectedProjectTaskDeveloperId = 0L;
    }

    public void moveDataFromToTemplate() {
        this.dataToTemplate.setTemplateData(this.dataFromTemplate);
    }

    public void setDataFromTemplate(TemplateData dataFromTemplate) {
        this.dataFromTemplate.setTemplateData(dataFromTemplate);
    }

    @Bean
    @SessionScope
    public SessionData getSessionData() {
        return new SessionData();
    }
}


