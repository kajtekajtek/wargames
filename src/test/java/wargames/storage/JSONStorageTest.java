package wargames.storage;

import static org.junit.jupiter.api.Assertions.*;
import static wargames.testutils.TestUtils.*;
import static wargames.testutils.JSONTestUtils.*;
import static wargames.testutils.ModelsTestUtils.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import wargames.factories.*;
import wargames.models.*;
import wargames.exceptions.StorageExceptions.JSONStorageExceptions.LoadJSONStorageException;
import wargames.exceptions.StorageExceptions.JSONStorageExceptions.SaveJSONStorageException;

public class JSONStorageTest {

    private final static String FILE_EXTENSION    = ".json";
    private final static String GENERAL_NAME      = "David";
    private final static int    GENERAL_ARMY_SIZE = 4;

    private final SoldierFactory soldierFactory = new SoldierFactory();
    private final GeneralFactory generalFactory = new GeneralFactory();

    private final ObjectMapper mapper = Mapper.getInstance();

    private JSONStorage storage;

    @TempDir
    Path tempDir;

    @BeforeEach 
    void setUp() {
        storage = new JSONStorage(tempDir.toString());
    }

    /* TODO: abstract object tests */

    @Nested
    class SaveTest {

        @Test
        @DisplayName("Should create a JSON file with correct content")
        void testSave() throws Exception {
            General generalToSave = generalFactory.createGeneral(
                GENERAL_NAME, 100, storage
            );
            populateGeneralArmy(generalToSave);

            File out = getGeneralFileFromDirectory(GENERAL_NAME, tempDir);

            storage.save(generalToSave);

            assertTrue(out.exists());
            assertJsonFileContents(generalToSave, out);
        }

        @Test
        @DisplayName("Should throw SaveJSONStorageException when directory is invalid")
        void testSaveInvalidDirectory() {
            File bogus = tempDir.resolve("not_a_dir.json").toFile();
            assertDoesNotThrow(bogus::createNewFile);

            storage = new JSONStorage(bogus.toString());

            General g = generalFactory.createGeneral(
                "ShouldFail", 999, storage
            );

            SaveJSONStorageException ex = assertThrows(
                SaveJSONStorageException.class, 
                () -> storage.save(g)
            );

            String msg = ex.getMessage();
            assertStringContains(msg, 
                "could not save to JSON file: ",
                "directory",
                "not_a_dir.json"
            );
        }

        @Test
        @DisplayName("Should throw SaveJSONStorageException when unable to write file")
        void testSaveNonWritableDir() {
            File readOnlyDir = tempDir.resolve("readonly").toFile();
            assertTrue(readOnlyDir.mkdir());
            assertTrue(readOnlyDir.setWritable(false));

            storage = new JSONStorage(readOnlyDir.toString());

            General general = generalFactory.createGeneral(
                GENERAL_NAME, 0, storage
            );

            try {
                SaveJSONStorageException ex = assertThrows(
                    SaveJSONStorageException.class, 
                    () -> storage.save(general)
                );        
                String msg = ex.getMessage();
                assertStringContains(msg, 
                    "could not save to JSON file: ", "unable to write");
            } finally {
                readOnlyDir.setWritable(true);
            }
        }

        @Test
        @DisplayName("Should handle concurrent saves of different files without errors and create all files")
        void testSaveConcurrentDifferentFiles() {
            int threads = 10;
            ExecutorService    executor = Executors.newFixedThreadPool(threads);
            List<Future<Void>> futures  = new ArrayList<>();
            List<General>      generals = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                final int idx = i;

                General general = generalFactory.createGeneral(
                    GENERAL_NAME + idx, 100 + idx, storage
                );
                generals.add(general);

                futures.add(executor.submit(() -> {
                    storage.save(general);
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
                assertJsonFileContents(generals.get(i), f);
            }
        }

        @Test
        @DisplayName("Should handle concurrent saves of the same file without error and produce a valid JSON")
        void testSaveConcurrentSameFile() {
            General general = generalFactory.createGeneral(
                GENERAL_NAME, 150, storage
            );
            populateGeneralArmy(general);
            File outFile = getGeneralFileFromDirectory(GENERAL_NAME, tempDir);
            int  threads = 5;
            ExecutorService    executor = Executors.newFixedThreadPool(threads);
            List<Future<Void>> futures  = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    storage.save(general);
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

            assertTrue(outFile.exists());
            assertTrue(outFile.length() > 0);

            assertJsonFileContents(general, outFile);
        }

    }

    @Nested
    class LoadTest {

        @Test
        @DisplayName("Should correctly load General state from the JSON file")
        void testLoad() throws Exception {
            General generalToLoad = generalFactory.createGeneral(
                GENERAL_NAME, 0, storage
            );
            populateGeneralArmy(generalToLoad);  

            File in = getGeneralFileFromDirectory(GENERAL_NAME, tempDir);

            mapper.writeValue(in, generalToLoad);

            General generalLoaded = generalFactory.createGeneral(
                GENERAL_NAME, 0, storage
            );
            storage.load(generalLoaded);

            assertEqualGenerals(generalToLoad, generalLoaded);
        }

        @Test
        @DisplayName("Should throw LoadJSONStorageException when loading non-existent file")
        void testLoadNonExistentFile() {
            General general = generalFactory.createGeneral(
                GENERAL_NAME, 10, storage
            );

            getGeneralFileFromDirectory(GENERAL_NAME, tempDir).delete();

            LoadJSONStorageException ex = assertThrows(
                LoadJSONStorageException.class,
                () -> storage.load(general)
            );
            String msg = ex.getMessage();
            assertStringContains(msg, 
                "could not load from JSON file: ",
                GENERAL_NAME + FILE_EXTENSION,
                "unable to read"
            );
        }

        @Test
        @DisplayName("Should throw LoadJSONStorageException when trying to load malformed JSON")
        void testLoadMalformedJson() throws Exception {
            File in = getGeneralFileFromDirectory(GENERAL_NAME, tempDir);

            Files.writeString(in.toPath(), "{ invalid_json ");
            General general = generalFactory.createGeneral(
                GENERAL_NAME, 0, storage
            );

            LoadJSONStorageException ex = assertThrows(
                LoadJSONStorageException.class,
                () -> storage.load(general)
            );
            String msg = ex.getMessage();
            assertStringContains(msg, 
                "could not load from JSON file: ", 
                GENERAL_NAME + FILE_EXTENSION, 
                "unable to parse"
            );
        }

        @Test
        @DisplayName("Should handle concurrent loads of the same file into separate objects")
        void testLoadConcurrentSameFile() {
            General original = generalFactory.createGeneral(
                GENERAL_NAME, 123, storage
            ); 
            populateGeneralArmy(original);
            assertDoesNotThrow(() -> storage.save(original));

            final int threads = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            CountDownLatch  start    = new CountDownLatch(1);
            List<Future<General>> futures = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    General g = generalFactory.createGeneral(
                        GENERAL_NAME, 0, storage
                    );
                    start.await();
                    storage.load(g);
                    return g;
                }));
            }

            start.countDown();

            for (Future<General> f : futures) {
                assertDoesNotThrow(() -> {
                    General loaded = f.get();
                    assertEqualGenerals(original, loaded);
                });
            }
            executor.shutdown();
            assertDoesNotThrow(() -> executor.awaitTermination(5, TimeUnit.SECONDS));

            File out = getGeneralFileFromDirectory(GENERAL_NAME, tempDir);
            assertTrue(out.exists());
            assertTrue(out.length() > 0);
            assertJsonFileContents(original, out);
        }

        @Test
        @DisplayName("Should handle concurrent loads of different files")
        void testLoadConcurrentDifferentFiles() throws Exception {
            final int files = 8;
            List<General> originals = new ArrayList<>();
            for (int i = 0; i < files; i++) {
                General g = generalFactory.createGeneral(
                    GENERAL_NAME + i, 200 + i, storage
                );
                populateGeneralArmy(g);
                storage.save(g);
                originals.add(g);
            }

            ExecutorService    executor = Executors.newFixedThreadPool(files);
            List<Future<Void>> futures  = new ArrayList<>();

            for (int i = 0; i < files; i++) {
                final int idx = i;
                futures.add(executor.submit(() -> {
                    General loaded = generalFactory.createGeneral(
                        GENERAL_NAME + idx, 0, storage
                    );
                    storage.load(loaded);
                    assertEqualGenerals(originals.get(idx), loaded);
                    return null;
                }));
            }

            for (Future<Void> f : futures) {
                assertDoesNotThrow(() -> f.get());
            }
            executor.shutdown();
            assertDoesNotThrow(() -> executor.awaitTermination(5, TimeUnit.SECONDS));
        }

    }

    @Nested
    class SaveAndLoadTest {

        @Test
        @DisplayName("Should preserve full General state")
        void testSaveAndLoad() {
            General original = generalFactory.createGeneral(
                GENERAL_NAME, 50, storage
            );
            populateGeneralArmy(original);

            assertDoesNotThrow(() -> storage.save(original));
            General loaded = generalFactory.createGeneral(
                GENERAL_NAME, 0, storage
            );
            assertDoesNotThrow(() -> storage.load(loaded));

            assertEqualGenerals(original, loaded);
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

    private void assertJsonFileContents(General general, File file) {
        Army     army     = general.getArmy();
        JsonNode rootNode = mapper.createObjectNode();

        try {
            rootNode = mapper.readTree(file);
        } catch (IOException e) {
            assertNull(e, "could not read file " + file.getAbsolutePath());
        }

        assertJsonNodeContents(general, rootNode);

        JsonNode armyNode = rootNode.get("army");
        assertJsonNodeContents(army, armyNode);

        JsonNode soldiersNode = armyNode.get("soldiers");
        for (int i = 0; i < army.getSize(); i++) {
            JsonNode soldierNode = soldiersNode.get(i);
            assertJsonNodeContents(army.getSoldiers().get(i), soldierNode);
        }
    }
}
