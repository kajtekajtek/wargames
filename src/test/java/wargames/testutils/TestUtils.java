package wargames.testutils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;

public class TestUtils {

    private TestUtils () { }

    public static void assertStringContains(String string, String... substrings) {
        for (String s : substrings) {
            assertTrue(string.contains(s));
        }
    }

    public static File getFileFromDirectory(Path directoryPath, String fileName) {
        Path filePath = directoryPath.resolve(fileName);
        return filePath.toFile();
    }

}
