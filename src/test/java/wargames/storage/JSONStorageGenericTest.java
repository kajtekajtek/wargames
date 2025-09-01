package wargames.storage;

import static org.junit.jupiter.api.Assertions.*;
import static wargames.testutils.TestUtils.*;
import static wargames.testutils.JSONTestUtils.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import wargames.models.*;
import wargames.testutils.testmodels.TestPojo;
import wargames.exceptions.StorageExceptions.JSONStorageExceptions.LoadJSONStorageException;
import wargames.exceptions.StorageExceptions.JSONStorageExceptions.SaveJSONStorageException;

public class JSONStorageGenericTest {

    private final static String FILE_EXTENSION = ".json";
    private final static String POJO_ID        = "p0j0";

    private ObjectMapper          mapper;
    private JSONStorage/*<TestPojo>*/ storage;

    @TempDir
    Path tempDir;

    @BeforeEach 
    void setUp() {
        mapper = Mapper.getInstance();
        /* storage = new JSONStorage<>(
            tempDir.toString(),
            TestPojo.class,
            TestPojo::getId
        ); */
    }

    @Nested
    class SaveTest { 

        @Test
        @DisplayName("Should create a JSON file with correct content")
        void testSave() {
            String   name    = "Alice";
            int      score   = 42;
            String[] tags    = { "a", "b" };
            String   cntrKey = "wins";
            int      cntrVal = 3;

            TestPojo pojoToSave = new TestPojo(POJO_ID, name, score)
                .withTags(tags)
                .withCounter(cntrKey, cntrVal);

            //storage.save(p);

            File out = getJSONFileFromDirectory(tempDir, POJO_ID);
            assertTrue(out.exists());
            TestPojo onDisk = readJson(out, TestPojo.class);
            assertEquals(pojoToSave, onDisk);
        }

        @Test
        @DisplayName("Should throw SaveJSONStorageException when directory is invalid")
        void testSaveInvalidDirectory() {
            File bogus = tempDir.resolve("not_a_dir.json").toFile();
            assertDoesNotThrow(bogus::createNewFile);

            storage = new JSONStorage(bogus.toString());

            TestPojo pojoToSave = new TestPojo(POJO_ID, "X", 1);

            SaveJSONStorageException ex = assertThrows(
                SaveJSONStorageException.class, 
                () -> /*storage.save(p)*/ {}
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
            File readOnlyDir = tempDir.resolve("ro").toFile();
            assertTrue(readOnlyDir.mkdir());
            assertTrue(readOnlyDir.setWritable(false));

            try {
                /*JSONStorage<TestPojo> storage = new JSONStorage<>(
                    ro.getAbsolutePath(), TestPojo.class, TestPojo::getId
                );*/
                TestPojo pojoToSave = new TestPojo(POJO_ID, "ReadOnly", 7);

                /*SaveJSONStorageException ex = assertThrows(
                    SaveJSONStorageException.class,
                    () -> storage.save(p)
                );
                String msg = ex.getMessage();
                assertStringContains(msg,
                    "could not save to JSON file: ", "unable to write");*/
            } finally {
                readOnlyDir.setWritable(true);
            }
        }

        @Test
        @DisplayName("Should handle concurrent saves of different files without errors and create all files")
        void testSaveConcurrentDifferentFiles() {
            final int          threads  = 10;
            ExecutorService    executor = Executors.newFixedThreadPool(threads);
            List<Future<Void>> futures  = new ArrayList<>();
            List<TestPojo>     pojos    = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                final int idx = i;

                TestPojo p = new TestPojo(POJO_ID + idx, "N" + idx, idx)
                    .withCounter("i", idx);
                pojos.add(p);

                futures.add(executor.submit(() -> {
                    //storage.save(p);
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
                File f = getJSONFileFromDirectory(tempDir, POJO_ID + i);
                assertTrue(f.exists());
                assertTrue(f.length() > 0);
                General onDisk = readJson(f, General.class);
                assertEquals(pojos.get(i), onDisk);
            }
        }

        @Test
        @DisplayName("Should handle concurrent saves of the same file without error and produce a valid JSON")
        void testSaveConcurrentSameFile() {
            final int       threads  = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            List<Future<?>> futures  = new ArrayList<>();

            TestPojo pojo = new TestPojo(POJO_ID, "pojo", 0);

            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    /*storage.save(pojo);*/
                    return null;
                }));
            }

            for (Future<?> f : futures) assertDoesNotThrow(() -> f.get());
            executor.shutdown();
            assertDoesNotThrow(() -> {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            });

            File out = getJSONFileFromDirectory(tempDir, POJO_ID);
            assertTrue(out.exists());
            assertTrue(out.length() > 0);
            General onDisk = readJson(out, General.class);
            assertEquals(pojo, onDisk);
        }

        @Test
        @DisplayName("Should keep JSON valid when saving the same object while its state changes")
        void testSaveConcurrentSameFileWithStateMutation() {
            final int       writers   = 6, iterations = 25;
            ExecutorService executor  = Executors.newFixedThreadPool(writers);
            CountDownLatch  startGate = new CountDownLatch(1);
            List<Future<?>> futures   = new ArrayList<>();
            ConcurrentLinkedQueue<JsonNode> snapshots = new ConcurrentLinkedQueue<>();

            TestPojo pojo = new TestPojo(POJO_ID, "Start", -1);
            assertDoesNotThrow(() -> /*storage.save(pojo)*/ {});

            for (int w = 0; w < writers; w++) {
                final int writerId = w;

                futures.add(executor.submit(() -> {
                    startGate.await();

                    for (int it = 0; it < iterations; it++) {
                        synchronized (pojo) {
                            pojo.setScore(pojo.getScore() + 1);
                            pojo.getCounters().put("w" + writerId, it);

                            JsonNode snapshot = mapper.valueToTree(pojo);
                            snapshots.add(snapshot);

                            //storage.save(pojo);
                        }
                    }

                    return null;
                }));

            }

            startGate.countDown();
            for (Future<?> f : futures) assertDoesNotThrow(() -> f.get());
            executor.shutdown();
            assertDoesNotThrow(
                () -> executor.awaitTermination(10, TimeUnit.SECONDS)
            );

            File out = getJSONFileFromDirectory(tempDir, POJO_ID);
            assertTrue(out.exists());
            assertTrue(out.length() > 0);

            JsonNode finalOnDisk = assertDoesNotThrow(() -> mapper.readTree(out));
            boolean matchesAnySnapshot = snapshots.stream()
                .anyMatch(finalOnDisk::equals);
            assertTrue(matchesAnySnapshot);
        }

    }

    @Nested
    class LoadTest {

        @Test
        @DisplayName("Should correctly load TestPojo state from the JSON file")
        void testLoad() throws Exception {
            File     in       = getJSONFileFromDirectory(tempDir, POJO_ID);
            TestPojo expected = new TestPojo(POJO_ID, "pojo-jojo", 7)
                                    .withTags("x","y")
                                    .withCounter("c", 5);

            mapper.writeValue(in, expected);

            TestPojo loaded = new TestPojo(POJO_ID, "name", 0);
            /*storage.load(loaded);*/

            assertEquals(expected, loaded);
        }

        @Test
        @DisplayName("Should throw LoadJSONStorageException when loading non-existent file")
        void testLoadNonExistentFile() {
            TestPojo toLoad = new TestPojo(POJO_ID, "name", 57);

            getJSONFileFromDirectory(tempDir, POJO_ID).delete();

            LoadJSONStorageException ex = assertThrows(
                LoadJSONStorageException.class,
                () -> /*storage.load(toLoad)*/ {}
            );
            String msg = ex.getMessage();
            assertStringContains(msg, 
                "could not load from JSON file: ",
                POJO_ID + FILE_EXTENSION,
                "unable to read"
            );
        }

        @Test
        @DisplayName("Should throw LoadJSONStorageException when trying to load malformed JSON")
        void testLoadMalformedJson() throws Exception {
            File     in     = getJSONFileFromDirectory(tempDir, POJO_ID);
            TestPojo toLoad = new TestPojo(POJO_ID, "name", 3);

            Files.writeString(in.toPath(), "{ invalid_json ");

            LoadJSONStorageException ex = assertThrows(
                LoadJSONStorageException.class,
                () -> /*storage.load(toLoad)*/{}
            );
            String msg = ex.getMessage();
            assertStringContains(msg, 
                "could not load from JSON file: ", 
                POJO_ID + FILE_EXTENSION, 
                "unable to parse"
            );
        }

        @Test
        @DisplayName("Should handle concurrent loads of the same file into separate objects")
        void testLoadConcurrentSameFile() {
            final int              threads    = 10;
            List<Future<TestPojo>> futures    = new ArrayList<>();
            ExecutorService        executor   = Executors.newFixedThreadPool(threads);
            CountDownLatch         start      = new CountDownLatch(1);
            List<TestPojo>         loadedList = new ArrayList<>();
            
            TestPojo original = new TestPojo(POJO_ID, "namee", 2137)
                .withCounter("i", 42)
                .withTags("a", "b", "c" );
            assertDoesNotThrow(() -> /*storage.save(original)*/{});

            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    TestPojo p = new TestPojo(POJO_ID, "", 0);
                    start.await();
                    /*storage.load(p);*/
                    return p;
                }));
            }

            start.countDown();

            for (Future<TestPojo> f : futures) {
                assertDoesNotThrow(() -> {
                    loadedList.add(f.get());
                });
            }
            executor.shutdown();
            assertDoesNotThrow(() -> 
                executor.awaitTermination(5, TimeUnit.SECONDS)
            );

            File out = getJSONFileFromDirectory(tempDir, POJO_ID);
            assertTrue(out.exists()); assertTrue(out.length() > 0);

            General onDisk = readJson(out, General.class);
            assertEquals(original, onDisk);

            for (TestPojo p : loadedList) { assertEquals(original, p); }
        }

        @Test
        @DisplayName("Should handle concurrent loads of different files")
        void testLoadConcurrentDifferentFiles() throws Exception {
            final int       files    = 10;
            List<Future<?>> futures  = new ArrayList<>();
            ExecutorService executor = Executors.newFixedThreadPool(files);

            ConcurrentHashMap<Integer, TestPojo> originalsMap, loadedMap;
            loadedMap    = new ConcurrentHashMap<Integer, TestPojo>(files);
            originalsMap = new ConcurrentHashMap<Integer, TestPojo>(files);

            for (int i = 0; i < files; i++) {
                TestPojo p = new TestPojo(POJO_ID + i, "name" + i, 200 + i)
                    .withCounter("i", i)
                    .withTags("" + i);
                /*storage.save(p);*/
                originalsMap.put(i, p);
            }

            for (int i = 0; i < files; i++) {
                final int idx = i;
                futures.add(executor.submit(() -> {
                    TestPojo p = new TestPojo(POJO_ID + idx, "", 0);
                    /*storage.load(p);*/
                    loadedMap.put(idx, p);
                    return null;
                }));
            }

            for (Future<?> f : futures) {
                assertDoesNotThrow(() -> f.get());
            }
            executor.shutdown();
            assertDoesNotThrow(() -> 
                executor.awaitTermination(5, TimeUnit.SECONDS)
            );

            IntStream
                .range(0, files)
                .forEach((i) -> assertEquals(
                    originalsMap.get(i), loadedMap.get(i)
                ));
        }

    }

    @Nested
    class SaveAndLoadTest {

        @Test
        @DisplayName("Should preserve full TestPojo state")
        void testSaveAndLoad() {
            TestPojo original = new TestPojo(POJO_ID, "name", 57)
                .withCounter("k", 5)
                .withTags("7");
            assertDoesNotThrow(() -> /*storage.save(original)*/{});

            TestPojo loaded = new TestPojo(POJO_ID, "", 0);
            assertDoesNotThrow(() -> /*storage.load(loaded)*/{});

            assertEquals(original, loaded);
        }

        @Test
        @DisplayName("Should allow concurrent save and load on the same file without corruption")
        void testSaveAndLoadConcurrentSameFile() {
            final int       writers    = 3, 
                            readers    = 7, 
                            iterations = 25;
            ExecutorService executor   = Executors.newFixedThreadPool(writers + readers);
            CountDownLatch  start      = new CountDownLatch(1);
            List<Future<?>> futures    = new ArrayList<>();
            List<TestPojo>  loadedList = new ArrayList<>();
            
            TestPojo original = new TestPojo(POJO_ID, "name", 64)
                .withCounter("k", 32)
                .withTags("8");
            assertDoesNotThrow(() -> /*storage.save(original)*/{});

            for (int i = 0; i < writers; i++) {
                futures.add(executor.submit(() -> {
                    start.await();

                    for (int j = 0; j < iterations; j++)/*storage.save(original)*/;

                    return null;
                }));
            }

            for (int i = 0; i < readers; i++) {
                futures.add(executor.submit(() -> {
                    TestPojo target = new TestPojo(POJO_ID, "", 0);
                    start.await();
                    for (int it = 0; it < iterations; it++) {
                        /*storage.load(target);*/
                        loadedList.add(target);
                    }
                    return null;
                }));
            }

            start.countDown();
            for (Future<?> f : futures) assertDoesNotThrow(() -> f.get());
            executor.shutdown();
            assertDoesNotThrow(() -> executor.awaitTermination(10, TimeUnit.SECONDS));

            loadedList.forEach(p -> assertEquals(original, p));

            File out = getJSONFileFromDirectory(tempDir, POJO_ID);
            assertTrue(out.exists()); assertTrue(out.length() > 0);

            General onDisk = readJson(out, General.class);
            assertEquals(original, onDisk);
        }

        @Test
        @DisplayName("Should handle mixed save and load tasks for different files in parallel")
        void testSaveAndLoadConcurrentDifferentFilesMixed() {
            final int       tasks    = 10;
            ExecutorService executor = Executors.newFixedThreadPool(tasks);
            CountDownLatch  start    = new CountDownLatch(1);
            List<Future<?>> futures  = new ArrayList<>();
            ConcurrentHashMap<Integer, TestPojo> 
                originalsMap = new ConcurrentHashMap<Integer, TestPojo>(), 
                loadedMap    = new ConcurrentHashMap<Integer, TestPojo>();
            ConcurrentHashMap<Integer, File> 
                outMap = new ConcurrentHashMap<Integer, File>();

            for (int i = 0; i < tasks; i++) {
                final int idx = i;
                futures.add(executor.submit(() -> {
                    TestPojo original = new TestPojo(POJO_ID + idx, "name" + idx, 200 + idx)
                        .withCounter("i", idx)
                        .withTags("" + idx);
                    start.await();
                    /*storage.save(original);*/ originalsMap.put(idx, original);

                    TestPojo loaded = new TestPojo(POJO_ID + idx, "", 0);
                    /*storage.load(loaded);*/ loadedMap.put(idx, loaded);

                    outMap.put(idx, getJSONFileFromDirectory(tempDir, POJO_ID + idx));

                    return null;
                }));
            }

            start.countDown();
            for (Future<?> f : futures) assertDoesNotThrow(() -> f.get());
            executor.shutdown();
            assertDoesNotThrow(() -> executor.awaitTermination(10, TimeUnit.SECONDS));

            IntStream
                .range(0, tasks)
                .forEach(i -> {
                    TestPojo original = originalsMap.get(i);
                    TestPojo loaded   = loadedMap.get(i);
                    File     out      = outMap.get(i);
                    
                    assertEquals(original, loaded);
                    assertTrue(out.exists()); assertTrue(out.length() > 0);
                    General onDisk = readJson(out, General.class);
                    assertEquals(original, onDisk);
                });
        }

        @Test
        @DisplayName("Should allow concurrent save (with mutations) and load of the same file without corruption")
        void testSaveAndLoadConcurrentSameFileWithStateMutation() throws Exception {
            final int       writers    = 3,
                            readers    = 5,
                            iterations = 25;
            ExecutorService executor   = Executors.newFixedThreadPool(writers + readers);
            CountDownLatch  startGate  = new CountDownLatch(1);
            List<Future<?>> futures    = new ArrayList<>();
            ConcurrentLinkedQueue<JsonNode> 
                            snapshots  = new ConcurrentLinkedQueue<>();

            TestPojo original = new TestPojo(POJO_ID, "namee", 300)
                .withCounter("key", 7)
                .withTags("a", "B");
            /*storage.save(original);*/

            snapshots.add(mapper.valueToTree(original));

            for (int w = 0; w < writers; w++) {
                final int writerId = w;

                futures.add(executor.submit(() -> {
                    startGate.await();

                    for (int it = 0; it < iterations; it++) {
                        synchronized (original) {
                            original.setScore(original.getScore() + 1);
                            original.getCounters().put("w" + writerId, it);

                            snapshots.add(mapper.valueToTree(original));

                            /*storage.save(original);*/
                        }
                    }

                    return null;
                }));

            }

            for (int r = 0; r < readers; r++) {
                futures.add(executor.submit(() -> {
                    TestPojo loaded = new TestPojo(POJO_ID, "", 0);

                    startGate.await();
                    for (int it = 0; it < iterations; it++) {
                        /*storage.load(loaded);*/

                        JsonNode loadedNode = mapper.valueToTree(loaded);

                        boolean matchesAnySnapshot = snapshots.stream()
                            .anyMatch(loadedNode::equals);
                        assertTrue(matchesAnySnapshot);
                    }

                    return null;
                }));
            }

            startGate.countDown();
            for (Future<?> f : futures) assertDoesNotThrow(() -> f.get());
            executor.shutdown();
            assertDoesNotThrow(() -> 
                executor.awaitTermination(15, TimeUnit.SECONDS)
            );

            File out = getJSONFileFromDirectory(tempDir, POJO_ID);
            assertTrue(out.exists()); assertTrue(out.length() > 0);

            JsonNode finalOnDisk        = mapper.readTree(out);
            boolean  matchesAnySnapshot = snapshots.stream()
                .anyMatch(finalOnDisk::equals);
            assertTrue(matchesAnySnapshot);
        }

    }

}
