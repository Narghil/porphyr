package combit.hu.porphyr;

import lombok.NonNull;
import org.jetbrains.annotations.TestOnly;

@TestOnly
public class TestConstants {
    protected static @NonNull
    final String[] PROJECT_NAMES = new String[]{
        "1. projekt", "2. projekt", "Projekt fejlesztővel", "Projekt feladattal"
    };

    protected static @NonNull
    final String[] DEVELOPER_NAMES = new String[]{
        "1. fejlesztő", "2. fejlesztő", "3. fejlesztő", "4. fejlesztő"
    };

    protected static @NonNull
    final String[] LOGIN_NAMES = new String[]{
        "user", "admin"
    };

    protected static @NonNull
    final String[] USER_FULL_NAMES = new String[]{
        "Felhasználó", "Rendszergazda"
    };

    protected static @NonNull
    final String[] ROLE_NAMES = new String[]{
        "USER", "ADMIN", "GUEST"
    };

    protected static @NonNull
    final String[][] TASK_NAMES = new String[][]{
        {"1. projekt 1. feladat", "1. projekt 2. feladat"},
        {"2. projekt 1. feladat", "2. projekt 2. feladat"},
        {"Projekt feladattal - feladat"}
    };
}
