package combit.hu.porphyr.controller.helpers;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class TemplateData {
    private int operation;
    private @Nullable Long id;
    private @Nullable String name;
    private @Nullable String description;
    private @Nullable Long longData;

    public void setTemplateData( final @NonNull TemplateData fromTemplate ){
        this.operation = fromTemplate.operation;
        this.id = fromTemplate.id;
        this.name = fromTemplate.name;
        this.description = fromTemplate.description;
        this.longData = fromTemplate.longData;
    }
}
