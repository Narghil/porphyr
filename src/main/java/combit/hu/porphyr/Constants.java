package combit.hu.porphyr;

import lombok.NonNull;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class Constants {
    public static final @NonNull String NEWLINE = System.lineSeparator();
    public static final @NonNull String WEB_NEWLINE = "<br/>";
    public static final @NonNull String ON = "ON";
    public static final @NonNull String OFF = "OFF";
    public static final @NonNull String ROLE_ADMIN = "ADMIN";
    public static final Logger log = LoggerFactory.getLogger(Constants.class);

    private Constants() {
    }
}
