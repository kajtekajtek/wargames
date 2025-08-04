package wargames.testutils;

import static org.junit.jupiter.api.Assertions.*;

import wargames.models.*;

import com.fasterxml.jackson.databind.JsonNode;

public final class JSONTestUtils {

    private JSONTestUtils() { }

    public static void assertJsonNodeContents(General g, JsonNode jn) {
        assertEquals(g.getName(), jn.get("name").asText());
        assertEquals(g.getGold(), jn.get("gold").asInt());
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
