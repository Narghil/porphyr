package combit.hu.porphyr.controller.helpers;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@ToString
public class DataFromTemplate {
    private @Nullable Long id;
    private @Nullable String name;
    private @Nullable String description;

    public DataFromTemplate() {
        this.id = null;
        this.name = "";
        this.description = "";
    }
}
