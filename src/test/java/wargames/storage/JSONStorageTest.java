package wargames.storage;

import static org.junit.jupiter.api.Assertions.*;
import static wargames.testutils.JSONTestUtils.*;
import static wargames.testutils.ModelsTestUtils.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

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

    @Test
    @DisplayName("Should create a JSON file with correct content")
    void testSave(@TempDir Path tempDir) throws Exception {
        storage = new JSONStorage();
        storage.setDirectory(tempDir.toString());

        General generalToSave = generalFactory.createGeneral(
            GENERAL_TO_SAVE_NAME, 0, storage
        );
        populateGeneralArmy(generalToSave);

        Path filePath = tempDir.resolve(
            GENERAL_TO_SAVE_NAME + FILE_EXTENSION
        );
        File out = filePath.toFile();

        generalToSave.save();

        assertTrue(out.exists());
        assertJsonFileContents(generalToSave, out);
    }

    @Test
    @DisplayName("Should correctly load General state from the JSON file")
    void testLoad(@TempDir Path tempDir) throws Exception {
        storage = new JSONStorage();
        storage.setDirectory(tempDir.toString());

        General generalToLoad = generalFactory.createGeneral(
            GENERAL_TO_LOAD_NAME, 0, storage
        );
        populateGeneralArmy(generalToLoad);  

        Path filePath = tempDir.resolve(
            GENERAL_TO_LOAD_NAME + FILE_EXTENSION
        );
        File in = filePath.toFile();
        JsonNode root = serializeGeneralToJsonNode(generalToLoad);
        mapper.writeValue(in, root);

        General generalLoaded = generalFactory.createGeneral(
            GENERAL_TO_LOAD_NAME, 0, storage
        );
        generalLoaded.load();

        assertEqualGenerals(generalToLoad, generalLoaded);
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

    private JsonNode serializeGeneralToJsonNode(General general) {
        return mapper.createObjectNode()
            .put("name", general.getName())
            .put("gold", general.getGold())
            .set("army", mapper.valueToTree(general.getArmy()));
    }
}
