package wargames.storage;

import static org.junit.jupiter.api.Assertions.*;
import static wargames.testutils.JSONTestUtils.*;
import static wargames.testutils.ModelsTestUtils.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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

    @TempDir
    Path tempDir;

    @BeforeEach 
    void setUp() {
        storage = new JSONStorage();
        storage.setDirectory(tempDir.toString());
    }

    @Test
    @DisplayName("Should create a JSON file with correct content")
    void testSave() throws Exception {
        General generalToSave = generalFactory.createGeneral(
            GENERAL_NAME, 100, storage
        );
        populateGeneralArmy(generalToSave);

        File out = getGeneralFileFromDirectory(GENERAL_NAME, tempDir);

        generalToSave.save();

        assertTrue(out.exists());
        assertJsonFileContents(generalToSave, out);
    }

    @Test
    @DisplayName("Should correctly load General state from the JSON file")
    void testLoad() throws Exception {
        General generalToLoad = generalFactory.createGeneral(
            GENERAL_NAME, 0, storage
        );
        populateGeneralArmy(generalToLoad);  

        File in = getGeneralFileFromDirectory(GENERAL_NAME, tempDir);

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
    void testSaveAndLoad() {
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
    @DisplayName("Should throw IllegalStateException on when loading non-existent file")
    void testLoadNonExistentFile() {
        General general = generalFactory.createGeneral(
            GENERAL_NAME, 10, storage
        );

        getGeneralFileFromDirectory(GENERAL_NAME, tempDir).delete();

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
    void testLoadMalformedJson() throws Exception {
        File in = getGeneralFileFromDirectory(GENERAL_NAME, tempDir);

        Files.writeString(in.toPath(), "{ invalid_json ");
        General general = generalFactory.createGeneral(
            GENERAL_NAME, 0, storage
        );

        assertThrows(
            com.fasterxml.jackson.core.JsonProcessingException.class,
            general::load
        );
    }

    @Test
    @DisplayName("Should handle concurrent saves without errors and create all files")
    void testConcurrentSaves() {
        int threads = 10;
        ExecutorService    executor = Executors.newFixedThreadPool(threads);
        List<Future<Void>> futures  = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                General g = generalFactory.createGeneral(
                    GENERAL_NAME + idx, 100 + idx
                );
                g.save();
                return null;
            }));
        }

        for (Future<Void> f : futures) {
            assertDoesNotThrow(() -> f.get());
        }
        executor.shutdown();
        assertDoesNotThrow(() -> {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        });

        for (int i = 0; i < threads; i++) {
            File f = getGeneralFileFromDirectory(GENERAL_NAME + i, tempDir);
            assertTrue(f.exists());
            assertTrue(f.length() > 0);
        }
    }

    @Test
    @DisplayName("Should throw IllegalStateException when directory is invalid")
    void testSaveInvalidDirectory() {
        File bogus = tempDir.resolve("not_a_dir.json").toFile();
        assertDoesNotThrow(bogus::createNewFile);

        storage.setDirectory(bogus.getAbsolutePath());

        General g = generalFactory.createGeneral("ShouldFail", 999);

        IllegalStateException ex = assertThrows(IllegalStateException.class, 
            g::save
        );

        String msg = ex.getMessage();
        assertTrue(msg.contains("directory") || msg.contains("not a folder"));
    }

    @Test
    @DisplayName("Should wrap IOException when unable to write file")
    void testSaveNonWritableDir() {
        File readOnlyDir = tempDir.resolve("readonly").toFile();
        assertTrue(readOnlyDir.mkdir());
        assertTrue(readOnlyDir.setWritable(false));

        storage.setDirectory(readOnlyDir.getAbsolutePath());

        General general = generalFactory.createGeneral(GENERAL_NAME, 0);

        try {
            IllegalStateException ex = assertThrows(
                IllegalStateException.class, general::save
            );        
            String msg = ex.getMessage();
            assertTrue(msg.toLowerCase().contains("unable to write"));
        } finally {
            readOnlyDir.setWritable(true);
        }
    }

    private void populateGeneralArmy(General general) {
        Army army = general.getArmy();
        for (int i = 1; i <= GENERAL_ARMY_SIZE; i++) {
            army.add(
                soldierFactory.createSoldier(Rank.fromValue(i), i)
            );
        }
    }

    private File getGeneralFileFromDirectory(String generalName, Path directoryPath) {
        Path filePath = directoryPath.resolve(generalName + FILE_EXTENSION);
        return filePath.toFile();
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
