package combit.hu.porphyr.controller.helpers;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

/**
 * A template-től kapott vagy a template-nek átadott adatok osztálya.
 *
 * @see SessionData
 */
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
    private @Nullable String stringData1;
    private @Nullable String stringData2;

    public void setTemplateData(final @NonNull TemplateData fromTemplate) {
        this.operation = fromTemplate.operation;
        this.id = fromTemplate.id;
        this.name = fromTemplate.name;
        this.description = fromTemplate.description;
        this.longData = fromTemplate.longData;
        this.stringData1 = fromTemplate.stringData1;
        this.stringData2 = fromTemplate.stringData2;
    }
}
