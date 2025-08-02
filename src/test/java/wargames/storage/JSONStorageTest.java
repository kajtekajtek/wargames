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
    @DisplayName("Should create a JSON file with correct content")
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

    @Test
    @DisplayName("Should correctly load General state from the JSON file")
    void testLoad() throws Exception {
        General generalToLoad = generalFactory.createGeneral(
            generalToLoadName, 0, storage
        );

        String jsonString = prepareJSONString(generalToLoad);
        String filename = generalToLoadName + fileExtension;

        File in = new File(filename);
        Files.writeString(in.toPath(), jsonString);

        General generalLoaded = generalFactory.createGeneral("", 0, storage);
        generalLoaded.load();

        assertEqualGenerals(generalToLoad, generalLoaded);
    }

    private String prepareJSONString(General general) {
        StringBuilder sb = new StringBuilder();

        sb.append("{");

        sb.append("\"name\":\"")
        .append(general.getName())
        .append("\",");

        sb.append("\"gold\":")
        .append(general.getGold())
        .append(",");

        sb.append("\"soldiers\":[");
        Army army = general.getArmy();
        for (int i = 0; i < army.getSize(); i++) {
            Soldier s = army.getSoldiers().get(i);
            sb.append("{")
            .append("\"rank\":\"").append(s.getRank().name()).append("\",")
            .append("\"exp\":").append(s.getExp()).append(",")
            .append("\"alive\":").append(s.isAlive())
            .append("}");
            if (i < army.getSize() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");

        sb.append("}");

        return sb.toString();
    }

    private void assertEqualGenerals(General expected, General actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getGold(), actual.getGold());

        Army expectedArmy = expected.getArmy();
        Army actualArmy   = actual.getArmy(); 
        assertEqualArmies(expectedArmy, actualArmy); 
    }

    private void assertEqualArmies(Army expected, Army actual) {
        assertEquals(expected.getSize(), actual.getSize());

        for (int i = 0; i < expected.getSize(); i++) {
            Soldier expectedSoldier = expected.getSoldiers().get(i);
            Soldier actualSoldier   = actual.getSoldiers().get(i);
            assertEqualSoldiers(expectedSoldier, actualSoldier);
        }
    }

    private void assertEqualSoldiers(Soldier expected, Soldier actual) {
        assertEquals(expected.getExp(),  actual.getExp());
        assertEquals(expected.getRank(), actual.getRank());
        assertEquals(expected.isAlive(), actual.isAlive());
    }


}
