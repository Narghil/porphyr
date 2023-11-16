package combit.hu.porphyr.controller;

import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.context.annotation.SessionScope;

import java.util.List;
import static lombok.AccessLevel.PRIVATE;

@SessionScope
@NoArgsConstructor(access = PRIVATE)
public class HomeControllerHelpers{

    public static final int DEVELOPERS=1;
    public static final int TASKS=2;
    public static final int MODIFY=3;
    public static final int DELETE=4;
    static final @NonNull String ERROR_TITLE = "Hiba!";

    @Setter
    @Getter
    @ToString
    public static final class ProjectPOJO {
        private @Nullable Long id;
        private @NonNull String name;
        private @Nullable String description;
        private @NonNull List<ProjectDeveloperEntity> developers;

        public void set(final @Nullable Long id, final @NonNull String name, final @Nullable String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public void set( final @NonNull ProjectPOJO project){
                this.id = project.getId();
                this.name = project.getName();
                this.description = project.getDescription();
        }

        public void set( final @NonNull ProjectEntity project){
            this.id = project.getId();
            this.name = project.getName();
            this.description = project.getDescription();
        }
    }

    public static final @NonNull ProjectPOJO projectPOJO = new ProjectPOJO();

    @Setter
    @Getter
    @AllArgsConstructor
    public static class WebError {
        private @Nullable String onOff;
        private @Nullable String title;
        private @Nullable String message;

        public void setError(final @NonNull String onOff, final @NonNull String title, final @NonNull String message) {
            this.onOff = onOff;
            this.title = title;
            this.message = message;
        }
    }

    public static final @NonNull WebError webError = new WebError("OFF", "", "");

    public static WebError getWebError() {
        WebError result = new WebError(
            webError.getOnOff(), webError.getTitle(), webError.getMessage()
        );
        webError.setOnOff("OFF");
        webError.setTitle("");
        webError.setMessage("");
        return result;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    public static class SelectedOperationData {
        private int operation;
        private @NonNull Long projectId;
        private @NonNull Long developerId;

        public SelectedOperationData(){
            operation = 0;
            projectId = 0L;
            developerId = 0L;
        }
    }
    public static final @NonNull SelectedOperationData selectedOperationData = new SelectedOperationData();

}

