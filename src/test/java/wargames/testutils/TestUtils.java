package wargames.testutils;

import static org.junit.jupiter.api.Assertions.*;

public class TestUtils {

    private TestUtils () { }

    public static void assertStringContains(String string, String... substrings) {
        for (String s : substrings) {
            assertTrue(string.contains(s));
        }
    }

}
