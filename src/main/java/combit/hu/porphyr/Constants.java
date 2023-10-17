package combit.hu.porphyr;
import lombok.NonNull;
import lombok.Value;

@Value
public class Constants {
    public static final @NonNull String NEWLINE = System.getProperty("line.separator");
    public static final @NonNull String WEB_NEWLINE = "<br/>";

    private Constants(){}
}
