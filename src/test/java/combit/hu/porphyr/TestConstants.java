package combit.hu.porphyr;
import lombok.NonNull;
import org.jetbrains.annotations.TestOnly;

@TestOnly
public class TestConstants {
    static @NonNull
    final String[] projectNames = new String[]{
        "1. projekt", "2. projekt", "Projekt fejlesztővel", "Projekt feladattal"
    };

    static @NonNull
    final String[] developerNames = new String[]{
        "1. fejlesztő", "2. fejlesztő", "3. fejlesztő", "4. fejlesztő"
    };

    static @NonNull
    final String[][] taskNames = new String[][]{
        {"1. projekt 1. feladat", "1. projekt 2. feladat"},
        {"2. projekt 1. feladat", "2. projekt 2. feladat"},
        {"Projekt feladattal - feladat"}
    };
}
