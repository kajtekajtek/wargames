package wargames.testutils;

import static org.junit.jupiter.api.Assertions.*;

import wargames.models.*;

import com.fasterxml.jackson.databind.JsonNode;

public final class JSONTestUtils {

    private JSONTestUtils() { }

    public static void assertJsonNodeContents(General g, JsonNode jn) {
        assertNotNull(jn);

        assertNotNull(jn.get("name"));
        String nameField = jn.get("name").asText();

        assertNotNull(jn.get("gold"));
        int goldField = jn.get("gold").asInt();

        assertEquals(g.getName(), nameField);
        assertEquals(g.getGold(), goldField);
    }

    public static void assertJsonNodeContents(Army a, JsonNode jn) {
        assertTrue(jn.isArray());
        assertEquals(a.getSize(), jn.size());
    }

    public static void assertJsonNodeContents(Soldier s, JsonNode jn) {
        assertEquals(s.getRank().name(), jn.get("rank").asText());
        assertEquals(s.getExp(), jn.get("exp").asInt());
        assertEquals(s.isAlive(), jn.get("alive").asBoolean());
    }
}
