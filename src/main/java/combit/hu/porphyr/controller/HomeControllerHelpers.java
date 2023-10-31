package combit.hu.porphyr.controller;

import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class HomeControllerHelpers{
    @Setter
    @Getter
    public static final class ProjectPOJO {
        private @Nullable Long id;
        private @NonNull String name;
        private @Nullable String description;
        private @NonNull List<ProjectDeveloperEntity> developers;
    }
}


