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

    private final static String FILE_EXTENSION       = ".json";
    private final static String GENERAL_TO_SAVE_NAME = "Saul";
    private final static String GENERAL_TO_LOAD_NAME = "David";
    private final static int    GENERAL_ARMY_SIZE    = 4;

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
        Files.deleteIfExists(new File(
            GENERAL_TO_SAVE_NAME + FILE_EXTENSION
        ).toPath());
        Files.deleteIfExists(new File(
            GENERAL_TO_LOAD_NAME + FILE_EXTENSION)
        .toPath());
    }
    
    @Test
    @DisplayName("Should create a JSON file with correct content")
    void testSave() throws Exception {
        General generalToSave = generalFactory.createGeneral(
            GENERAL_TO_SAVE_NAME, 0, storage
        );
        populateGeneralArmy(generalToSave);

        String filename = GENERAL_TO_SAVE_NAME + FILE_EXTENSION;
        File   out      = new File(filename);
        if (out.exists()) assertTrue(out.delete());

        generalToSave.save();

        assertTrue(out.exists());
        assertJsonFileContents(generalToSave, out);
    }

    private void populateGeneralArmy(General general) {
        Army army = general.getArmy();
        for (int i = 1; i <= GENERAL_ARMY_SIZE; i++) {
            army.add(
                soldierFactory.createSoldier(Rank.fromValue(i), i)
            );
        }
    }

    private void assertJsonFileContents(General general, File file) throws Exception {
        Army     army     = general.getArmy();
        JsonNode rootNode = mapper.readTree(file);

        assertJsonNodeContents(general, rootNode);

        JsonNode armyNode = rootNode.get("army");
        assertJsonNodeContents(army, armyNode);

        for (int i = 0; i < army.getSize(); i++) {
            JsonNode soldierNode = armyNode.get(i);
            assertJsonNodeContents(army.getSoldiers().get(i), soldierNode);
        }
    }

    private void assertJsonNodeContents(General g, JsonNode jn) {
        assertEquals(g.getName(), jn.get("name").asText());
        assertEquals(g.getGold(), jn.get("gold").asInt());
    }

    private void assertJsonNodeContents(Army a, JsonNode jn) {
        assertTrue(jn.isArray());
        assertEquals(a.getSize(), jn.size());
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
            GENERAL_TO_LOAD_NAME, 0, storage
        );
        populateGeneralArmy(generalToLoad);  

        JsonNode root     = serializeGeneralToJsonNode(generalToLoad);
        String   filename = GENERAL_TO_LOAD_NAME + FILE_EXTENSION;
        File     in       = new File(filename);
        mapper.writeValue(in, root);

        General generalLoaded = generalFactory.createGeneral(
            GENERAL_TO_LOAD_NAME, 0, storage
        );
        generalLoaded.load();

        assertEqualGenerals(generalToLoad, generalLoaded);
    }

    private JsonNode serializeGeneralToJsonNode(General general) {
        return mapper.createObjectNode()
            .put("name", general.getName())
            .put("gold", general.getGold())
            .set("army", mapper.valueToTree(general.getArmy()));
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
