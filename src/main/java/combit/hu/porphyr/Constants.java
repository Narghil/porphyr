package combit.hu.porphyr;
import lombok.NonNull;
import lombok.Value;

@Value
public class Constants {
    public static final @NonNull String NEWLINE = System.lineSeparator();
    public static final @NonNull String WEB_NEWLINE = "<br/>";
    public static final @NonNull String ON = "ON";
    public static final @NonNull String OFF = "OFF";

    private Constants(){}
}
