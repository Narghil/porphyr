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
import combit.hu.porphyr.service.PorphyrServiceException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * A template-nek átadott és azoktól visszakapott adatok session szintű tárolására szolgáló objektum,
 * további műveletek és további átadások céljára.
 *
 * @see TemplateData
 */
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

    /**
     * A template-től kapott adatok.
     */
    @Getter
    private @NonNull TemplateData dataFromTemplate;
    /**
     * A template-nek átadandó adatok.
     */
    @Getter
    private @NonNull TemplateData dataToTemplate;

    private @NonNull Long selectedProjectId;
    private @NonNull Long selectedDeveloperId;
    private @NonNull Long selectedProjectDeveloperId;
    private @NonNull Long selectedProjectTaskId;
    private @NonNull Long selectedProjectTaskDeveloperId;
    private @NonNull String userLoginName;
    private @NonNull ArrayList<String> userPermitNames;
    private @NonNull ArrayList<String> userPermittedRequestCalls;
    private @NonNull ArrayList<DeveloperEntity> userDevelopers;

    public @NonNull ProjectEntity getSelectedProject() throws InterruptedException, ExecutionException {
        @Nullable ProjectEntity result;
        result = projectService.getProjectById(this.selectedProjectId);
        if (result == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.CONTROLLER_SELECTED_PROJECT_NOT_EXISTS);
        }
        return result;
    }

    public @NonNull DeveloperEntity getSelectedDeveloper() throws InterruptedException, ExecutionException {
        @Nullable DeveloperEntity result;
        result = developerService.getDeveloperById(this.selectedDeveloperId);
        if (result == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.CONTROLLER_SELECTED_DEVELOPER_NOT_EXISTS);
        }
        return result;
    }

    public @NonNull ProjectDeveloperEntity getSelectedProjectDeveloper()
        throws InterruptedException, ExecutionException {
        @Nullable ProjectDeveloperEntity result;
        result = projectDeveloperService.getProjectDeveloperById(this.selectedProjectDeveloperId);
        if (result == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.CONTROLLER_SELECTED_PROJECTDEVELOPER_NOT_EXISTS);
        }
        return result;
    }

    public @NonNull ProjectTaskEntity getSelectedProjectTask() throws InterruptedException, ExecutionException {
        @Nullable ProjectTaskEntity result;
        result = projectTaskService.getProjectTaskById(this.selectedProjectTaskId);
        if (result == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.CONTROLLER_SELECTED_PROJECTTASK_NOT_EXISTS);
        }
        return result;
    }

    public @NonNull ProjectTaskDeveloperEntity getSelectedProjectTaskDeveloper()
        throws InterruptedException, ExecutionException {
        @Nullable ProjectTaskDeveloperEntity result;
        result = projectTaskDeveloperService.getProjectTaskDeveloperById(this.selectedProjectTaskDeveloperId);
        if (result == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.CONTROLLER_SELECTED_PROJECTTASKDEVELOPER_NOT_EXISTS);
        }
        return result;
    }

    @Autowired
    public SessionData(final @NonNull ProjectService projectService,
        final @NonNull DeveloperService developerService,
        final @NonNull ProjectDeveloperService projectDeveloperService,
        final @NonNull ProjectTaskService projectTaskService,
        final @NonNull ProjectTaskDeveloperService projectTaskDeveloperService
    ) {
        this.projectService = projectService;
        this.developerService = developerService;
        this.projectDeveloperService = projectDeveloperService;
        this.projectTaskService = projectTaskService;
        this.projectTaskDeveloperService = projectTaskDeveloperService;

        dataFromTemplate = new TemplateData();
        dataToTemplate = new TemplateData();
        selectedProjectId = 0L;
        selectedDeveloperId = 0L;
        selectedProjectDeveloperId = 0L;
        selectedProjectTaskId = 0L;
        selectedProjectTaskDeveloperId = 0L;
        userLoginName = "anonymus";
        userPermitNames = new ArrayList<>() ;
        userPermittedRequestCalls = new ArrayList<>();
        userDevelopers = new ArrayList<>();
    }

    public void moveDataFromToTemplate() {
        this.dataToTemplate.setTemplateData(this.dataFromTemplate);
    }

    public void setDataFromTemplate(TemplateData dataFromTemplate) {
        this.dataFromTemplate.setTemplateData(dataFromTemplate);
    }

    @Bean
    @SessionScope
    @Autowired
    public SessionData getSessionData(
        ProjectService projectService,
        DeveloperService developerService,
        ProjectDeveloperService projectDeveloperService,
        ProjectTaskService projectTaskService,
        ProjectTaskDeveloperService projectTaskDeveloperService
    ){
        return new SessionData(
            projectService,
            developerService,
            projectDeveloperService,
            projectTaskService,
            projectTaskDeveloperService
        );
    }
}


