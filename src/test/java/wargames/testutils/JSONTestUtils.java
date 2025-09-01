package wargames.testutils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import wargames.models.*;
import wargames.storage.Mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JSONTestUtils {

    private final static String FILE_EXTENSION = ".json";

    private JSONTestUtils() { }

    public static File getJSONFileFromDirectory(Path directoryPath, String fileName) {
        return TestUtils.getFileFromDirectory(
            directoryPath, fileName + FILE_EXTENSION
        );
    }

    public static <T> T readJson(File f, Class<T> type) {
        try { 
            ObjectMapper mapper = Mapper.getInstance();
            return mapper.readValue(f, type); 
        } catch (IOException e) { 
            fail(e); return null; 
        }
    }    

    public static void assertJsonNodeContents(General g, JsonNode jn) {
        assertNotNull(jn, "General JSON node shouldn't be null");

        assertNotNull(jn.get("name"), 
            "General JSON field \"name\" is null");
        String name = jn.get("name").asText();
        assertEquals(g.getName(), name, 
            "General JSON field \"name\" not equal to expected."
        );

        assertNotNull(jn.get("gold"),
            "General JSON field \"gold\" is null");
        int gold = jn.get("gold").asInt();
        assertEquals(g.getGold(), gold,
            "General JSON field \"gold\" not equal to expected." 
        );
    }

    public static void assertJsonNodeContents(Army a, JsonNode jn) {
        assertNotNull(jn, "Army JSON node shouldn't be null");

        assertNotNull(jn.get("soldiers"), 
            "Army JSON field \"soldiers\" is null");
        assertTrue(jn.get("soldiers").isArray());

        assertNotNull(jn.get("empty"),
            "Army JSON field \"empty\" is null");
        Boolean empty = jn.get("empty").asBoolean();
        assertEquals(a.isEmpty(), empty,
            "Army JSON field \"empty\" not equal to expected."
        );

        assertNotNull(jn.get("size"),
            "Army JSON field \"size\" is null");
        int size = jn.get("size").asInt();
        assertEquals(a.getSize(), size,
            "Army JSON field \"size\" not equal to expected." 
        );

        assertNotNull(jn.get("totalStrength"),
            "Army JSON field \"totalStrength\" is null");
        int totalStrength = jn.get("totalStrength").asInt();
        assertEquals(a.getTotalStrength(), totalStrength,
            "Army JSON field \"totalStrength\" not equal to expected." 
        );
    }

    public static void assertJsonNodeContents(Soldier s, JsonNode jn) {
        assertNotNull(jn, "Soldier JSON node shouldn't be null");

        assertNotNull(jn.get("rank"),
            "Soldier JSON field \"rank\" is null");
        String rank = jn.get("rank").asText();
        assertEquals(s.getRank().toString(), rank,
            "Soldier JSON field \"rank\" not equal to expected."
        );

        assertNotNull(jn.get("exp"),
            "Soldier JSON field \"exp\" is  null");
        int exp = jn.get("exp").asInt();
        assertEquals(s.getExp(), exp,
            "Soldier JSON field \"exp\" not equal to expected."
        );
        
        assertNotNull(jn.get("alive"),
            "Soldier JSON field \"alive\" is  null");
        Boolean alive = jn.get("alive").asBoolean();
        assertEquals(s.isAlive(), alive,
            "Soldier JSON field \"alive\" not equal to expected."
        );
        
        assertNotNull(jn.get("strength"),
            "Soldier JSON field \"strength\" is  null");
        int strength = jn.get("strength").asInt();
        assertEquals(s.getStrength(), strength,
            "Soldier JSON field \"strength\" not equal to expected.");
    }
}
