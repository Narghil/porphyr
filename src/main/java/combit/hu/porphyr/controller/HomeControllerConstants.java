package combit.hu.porphyr.controller;

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public class HomeControllerConstants {

    public static final int DEVELOPERS = 1;
    public static final int TASKS = 2;
    public static final int MODIFY = 3;
    public static final int DELETE = 4;
    public static final @NonNull String ERROR_TITLE = "Hiba!";

    private HomeControllerConstants(){}

    @Getter
    public static class ProjectDataFromTemplate{
        private final @Nullable Long id;
        private final @NonNull String name;
        private final @Nullable String description;

        public ProjectDataFromTemplate(){
            this.id = null;
            this.name = "";
            this.description = "";
        }
    }

}


