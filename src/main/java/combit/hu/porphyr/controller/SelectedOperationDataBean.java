package combit.hu.porphyr.controller;

import combit.hu.porphyr.domain.ProjectEntity;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@Setter
@Getter
@ToString
public class SelectedOperationDataBean {

    private int operation;
    private @NonNull Long projectId;
    private @NonNull Long developerId;
    private @NonNull Long projectTaskId;
    private @NonNull Long projectDeveloperId;
    private @NonNull Long projectTaskDeveloperId;
    private @Nullable Long longData;
    private @NonNull ProjectEntity editedProject;

    public SelectedOperationDataBean() {
        operation = 0;
        projectId = 0L;
        developerId = 0L;
        projectTaskId = 0L;
        projectDeveloperId = 0L;
        projectTaskDeveloperId = 0L;
        longData = 0L;
        editedProject = new ProjectEntity();
    }

    @Bean
    @SessionScope
    public SelectedOperationDataBean getSelectedOperationDataBean() {
        return new SelectedOperationDataBean();
    }

}


