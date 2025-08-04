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

    private final static String FILE_EXTENSION    = ".json";
    private final static String GENERAL_NAME      = "David";
    private final static int    GENERAL_ARMY_SIZE = 4;

    private final SoldierFactory soldierFactory = new SoldierFactory();
    private final GeneralFactory generalFactory = new GeneralFactory();

    private final ObjectMapper mapper = new ObjectMapper();

    private JSONStorage storage;

    private File getGeneralFileFromTempDir(@TempDir Path tempDir) {
        Path filePath = tempDir.resolve(GENERAL_NAME + FILE_EXTENSION);
        return filePath.toFile();
    }

    @Test
    @DisplayName("Should create a JSON file with correct content")
    void testSave(@TempDir Path tempDir) throws Exception {
        storage = new JSONStorage();
        storage.setDirectory(tempDir.toString());

        General generalToSave = generalFactory.createGeneral(
            GENERAL_NAME, 100, storage
        );
        populateGeneralArmy(generalToSave);

        File out = getGeneralFileFromTempDir(tempDir);

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
            GENERAL_NAME, 0, storage
        );
        populateGeneralArmy(generalToLoad);  

        File in = getGeneralFileFromTempDir(tempDir);

        JsonNode root = serializeGeneralToJsonNode(generalToLoad);
        mapper.writeValue(in, root);

        General generalLoaded = generalFactory.createGeneral(
            GENERAL_NAME, 0, storage
        );
        generalLoaded.load();

        assertEqualGenerals(generalToLoad, generalLoaded);
    }

    @Test
    @DisplayName("Should preserve full General state")
    void testSaveAndLoad(@TempDir Path tempDir) {
        storage = new JSONStorage();
        storage.setDirectory(tempDir.toString());

        General original = generalFactory.createGeneral(
            GENERAL_NAME, 50, storage
        );
        populateGeneralArmy(original);

        original.save();
        General loaded = generalFactory.createGeneral(GENERAL_NAME, 0, storage);
        loaded.load();

        assertEqualGenerals(original, loaded);
    }

    @Test
    @DisplayName("Should throw IllegalStateExceptionon when loading non-existent file")
    void testLoadNonExistentFile(@TempDir Path tempDir) {
        storage = new JSONStorage();
        storage.setDirectory(tempDir.toString());

        General general = generalFactory.createGeneral(GENERAL_NAME, 10, storage);

        getGeneralFileFromTempDir(tempDir).delete();

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            general::load
        );
        assertTrue(ex.getMessage().contains(
            "cannot load " + GENERAL_NAME + FILE_EXTENSION
        ));
    }

    @Test
    @DisplayName("Should throw JsonProcessingException when trying to load malformed JSON")
    void testLoadMalformedJson(@TempDir Path tempDir) throws Exception {
        JSONStorage storage = new JSONStorage();
        storage.setDirectory(tempDir.toString());

        File in = getGeneralFileFromTempDir(tempDir);

        Files.writeString(in.toPath(), "{ invalid_json ");
        General general = generalFactory.createGeneral(
            GENERAL_NAME, 0, storage
        );

        assertThrows(
            com.fasterxml.jackson.core.JsonProcessingException.class,
            general::load
        );
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
