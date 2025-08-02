package wargames.storage;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Files;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import wargames.factories.*;
import wargames.models.*;

public class JSONStorageTest {

    private final String fileExtension     = ".json";
    private final String generalToSaveName = "Saul";
    private final String generalToLoadName = "David";

    private final SoldierFactory soldierFactory = new SoldierFactory();
    private final GeneralFactory generalFactory = new GeneralFactory();

    private final ObjectMapper mapper = new ObjectMapper();
    private JSONStorage storage;

    @BeforeEach
    void setUp() {
        storage = new JSONStorage();
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(new File(generalToSaveName + fileExtension).toPath());
        Files.deleteIfExists(new File(generalToLoadName + fileExtension).toPath());
    }
    
    @Test
    @DisplayName("Should create a JSON ffile with correct content")
    void testSave() throws Exception {
        int    armySize = 3;
        String filename = generalToSaveName + fileExtension;

        General generalToSave = generalFactory.createGeneral(
            generalToLoadName, 0, storage
        );

        Army army = generalToSave.getArmy();
        for (int i = 1; i <= armySize; i++) {
            army.add(
                soldierFactory.createSoldier(Rank.fromValue(i), i)
            );
        }

        File out = new File(filename);

        if (out.exists()) assertTrue(out.delete());

        generalToSave.save();

        assertTrue(out.exists());
        assertJsonFileContents(generalToSave, out);
    }

    private void assertJsonFileContents(General general, File f) throws Exception {
        Army     army     = general.getArmy();
        JsonNode rootNode = mapper.readTree(f);

        assertJsonNodeContents(general, rootNode);

        JsonNode soldiersNode = rootNode.get("soldiers");
        assertJsonNodeContents(army, soldiersNode);

        for (int i = 0; i < army.getSize(); i++) {
            JsonNode soldierNode = soldiersNode.get(i);
            assertJsonNodeContents(army.getSoldiers().get(i), soldierNode);
        }
    }

    private void assertJsonNodeContents(General g, JsonNode jn) {
        assertEquals(g.getName(), jn.get("name").asText());
        assertEquals(g.getGold(), jn.get("gold").asInt());
    }

    private void assertJsonNodeContents(Army soldiers, JsonNode jn) {
        assertTrue(jn.isArray());
        assertEquals(soldiers.getSize(), jn.size());
    }

    private void assertJsonNodeContents(Soldier s, JsonNode jn) {
        assertEquals(s.getRank().name(), jn.get("rank").asText());
        assertEquals(s.getExp(), jn.get("exp").asInt());
        assertEquals(s.isAlive(), jn.get("alive").asBoolean());
    }
}
