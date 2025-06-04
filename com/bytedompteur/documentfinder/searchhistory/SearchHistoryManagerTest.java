package com.bytedompteur.documentfinder.searchhistory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchHistoryManagerTest {

    private SearchHistoryManager searchHistoryManager;
    private Path historyFilePath;

    @TempDir
    Path tempDir; // JUnit 5 temporary directory

    @BeforeEach
    void setUp() throws IOException {
        // Override the default history file path to use a temporary directory for tests
        // This requires a way to inject the path or modify PathUtil for testing,
        // or change SearchHistoryManager to accept a Path in constructor.
        // For now, let's assume SearchHistoryManager uses a PathProvider that can be mocked,
        // or we directly manipulate where it writes if possible.

        // To correctly test, we need to control where SearchHistoryManager writes its file.
        // One way: modify SearchHistoryManager to accept a Path in its constructor (preferred for testability).
        // Another way: use a static setter in PathUtil for tests (less clean).
        // For this example, let's assume we can't modify SearchHistoryManager constructor directly now.
        // We will create the instance, get the path, and then manipulate that file.

        // Create a temporary file path for the history within the tempDir
        historyFilePath = tempDir.resolve("search_history.txt");

        // "Hack" to make SearchHistoryManager use the temp path.
        // This is not ideal. A better solution would be to make SearchHistoryManager
        // accept the history file path in its constructor or use a configurable PathProvider.
        // For the purpose of this exercise, we'll create a new SearchHistoryManager
        // and then check its expected file path. We will create a dummy file there before
        // SearchHistoryManager is instantiated in tests that require a pre-existing file.
        // And for saving, we'll check the file at the path it *would* create.

        // To simulate PathUtil.getApplicationDataFolder() returning tempDir for tests:
        // We can't directly change System.getProperty("user.home") easily or PathUtil.
        // So, we'll create a SearchHistoryManager, get its *intended* path, and then
        // ensure our tests interact with *that* path within our @TempDir.

        // This setup assumes PathUtil.getApplicationDataFolder() can be influenced or mocked.
        // If PathUtil is static and not easily mockable, testing file persistence becomes harder
        // without refactoring SearchHistoryManager to accept a Path.

        // Let's proceed by creating a SearchHistoryManager and then we'll know where it *tries* to save.
        // For the sake of this test, we will create the history file *manually* in the tempDir
        // and then instantiate SearchHistoryManager in specific tests that load history.
        // For tests that save, we will instantiate and then check the file in tempDir.
        // This is a bit of a workaround due to not being able to inject the path easily.
    }

    private SearchHistoryManager createManagerUsingTempFile(Path specificPath) {
        // This is a conceptual helper. In reality, SearchHistoryManager needs to be refactored
        // to take a path, or PathUtil needs to be testable.
        // For now, we'll assume that if we create a file at `specificPath`
        // *before* SearchHistoryManager is created, it will pick it up if its internal logic matches.
        // This is highly dependent on SearchHistoryManager's internal PathUtil behavior.

        // Let's assume PathUtil can be "tricked" for testing or SearchHistoryManager is refactored.
        // If SearchHistoryManager was: public SearchHistoryManager(Path historyFilePath)
        // then we could do: return new SearchHistoryManager(specificPath);

        // Since we cannot change SearchHistoryManager, we will rely on its default behavior
        // and ensure our test file operations are on the *expected* default path,
        // but within our @TempDir.
        // This means PathUtil.getApplicationDataFolder() needs to point to tempDir.
        // This is the tricky part. A simple way:
        // Create a "dummy" SearchHistoryManager to find out its path, then use that.
        SearchHistoryManager dummyManager = new SearchHistoryManager();
        Path actualPathUsedByManager = dummyManager.getHistoryFilePath();

        // For a robust test, we'd need to ensure actualPathUsedByManager is within tempDir.
        // This requires SearchHistoryManager to be test-friendly.
        // Let's assume for now `historyFilePath` (member of this test class) is the one used.
        // If SearchHistoryManager always creates files in e.g. ~/.DocumentFinderAppData,
        // then these tests would write there, which is not ideal for unit tests.

        // The provided SearchHistoryManager uses a hardcoded "search_history.txt"
        // and PathUtil.getApplicationDataFolder(). We need PathUtil to point to tempDir.
        // One "hacky" way for PathUtil if it were:
        // class PathUtil { public static Path BASE_PATH = null; ... }
        // Then in test setup: PathUtil.BASE_PATH = tempDir;

        // Given the current PathUtil, the tests will try to write to the actual user's app data folder.
        // This is not good.
        // To make this testable without changing SearchHistoryManager, we'd have to use PowerMock
        // to mock the static PathUtil.getApplicationDataFolder() or mock `Paths.get()`.

        // Let's assume we *could* refactor SearchHistoryManager:
        // public SearchHistoryManager(Path historyFilePath) {
        // this.historyFilePath = historyFilePath;
        // this.searchQueries = loadHistory();
        // }
        // Then in setUp():
        // this.historyFilePath = tempDir.resolve("test_search_history.txt");
        // this.searchHistoryManager = new SearchHistoryManager(this.historyFilePath);

        // Sticking to the current structure, the best we can do is test the logic,
        // and separately verify file content if we know the fixed path.
        // For this exercise, I will write tests assuming historyFilePath can be controlled.
        // I will proceed as if SearchHistoryManager *was* refactored to take the path.
        return new SearchHistoryManager(specificPath);
    }


    @BeforeEach
    void setUpRefactored() {
        // This setup assumes SearchHistoryManager is refactored to accept a Path
        historyFilePath = tempDir.resolve("test_search_history.txt");
        // To make this compile, we'd need to add a constructor to SearchHistoryManager:
        // public SearchHistoryManager(Path specificPath) { this.historyFilePath = specificPath; this.searchQueries = loadHistory(); }
        // And modify the original constructor to call this one with PathUtil.getApplicationDataFolder().resolve(HISTORY_FILE_NAME)
        // For now, I will write the tests as if this refactoring was done.
        // If it's not, the file operations will go to the default location, not tempDir.
        searchHistoryManager = new SearchHistoryManager(historyFilePath); // Assumed refactored constructor
    }


    @AfterEach
    void tearDown() throws IOException {
        // Clean up the history file after each test
        Files.deleteIfExists(historyFilePath);
    }

    @Test
    void addSearchQuery_addNewQuery() {
        searchHistoryManager.addSearchQuery("test query 1");
        assertEquals(1, searchHistoryManager.getSearchHistory().size());
        assertEquals("test query 1", searchHistoryManager.getSearchHistory().get(0));
    }

    @Test
    void addSearchQuery_addMultipleQueries() {
        searchHistoryManager.addSearchQuery("query 1");
        searchHistoryManager.addSearchQuery("query 2");
        assertEquals(2, searchHistoryManager.getSearchHistory().size());
        assertEquals("query 2", searchHistoryManager.getSearchHistory().get(0));
        assertEquals("query 1", searchHistoryManager.getSearchHistory().get(1));
    }

    @Test
    void addSearchQuery_addDuplicateQuery_shouldMoveToTop() {
        searchHistoryManager.addSearchQuery("query 1");
        searchHistoryManager.addSearchQuery("query 2");
        searchHistoryManager.addSearchQuery("query 1"); // Add duplicate
        assertEquals(2, searchHistoryManager.getSearchHistory().size());
        assertEquals("query 1", searchHistoryManager.getSearchHistory().get(0)); // Should be at the top
        assertEquals("query 2", searchHistoryManager.getSearchHistory().get(1));
    }

    @Test
    void addSearchQuery_exceedMaxHistorySize_shouldRemoveOldest() {
        for (int i = 1; i <= 10; i++) {
            searchHistoryManager.addSearchQuery("query " + i);
        }
        assertEquals(10, searchHistoryManager.getSearchHistory().size());
        assertEquals("query 10", searchHistoryManager.getSearchHistory().get(0)); // Most recent

        searchHistoryManager.addSearchQuery("query 11"); // Add 11th query
        assertEquals(10, searchHistoryManager.getSearchHistory().size());
        assertEquals("query 11", searchHistoryManager.getSearchHistory().get(0)); // Newest
        assertEquals("query 2", searchHistoryManager.getSearchHistory().get(9)); // Oldest should be query 1, so query 2 is 9th
        assertFalse(searchHistoryManager.getSearchHistory().contains("query 1"));
    }

    @Test
    void addSearchQuery_addNullOrEmptyQuery_shouldNotAdd() {
        searchHistoryManager.addSearchQuery(null);
        assertEquals(0, searchHistoryManager.getSearchHistory().size());
        searchHistoryManager.addSearchQuery("");
        assertEquals(0, searchHistoryManager.getSearchHistory().size());
        searchHistoryManager.addSearchQuery("   ");
        assertEquals(0, searchHistoryManager.getSearchHistory().size());
    }

    @Test
    void loadHistory_fileExistsAndNotEmpty() throws IOException {
        // Prepare a history file
        List<String> initialQueries = Arrays.asList("old query 1", "old query 2");
        Files.createDirectories(historyFilePath.getParent());
        Files.write(historyFilePath, initialQueries);

        SearchHistoryManager newManager = new SearchHistoryManager(historyFilePath); // Assumed refactored constructor
        List<String> loadedHistory = newManager.getSearchHistory();

        assertEquals(2, loadedHistory.size());
        assertEquals("old query 1", loadedHistory.get(0));
        assertEquals("old query 2", loadedHistory.get(1));
    }

    @Test
    void loadHistory_fileDoesNotExist_shouldReturnEmptyList() {
        // Ensure file does not exist (redundant with @AfterEach but good for clarity)
        assertFalse(Files.exists(historyFilePath));

        SearchHistoryManager newManager = new SearchHistoryManager(historyFilePath); // Assumed refactored constructor
        assertTrue(newManager.getSearchHistory().isEmpty());
    }

    @Test
    void loadHistory_emptyFile_shouldReturnEmptyList() throws IOException {
        Files.createDirectories(historyFilePath.getParent());
        Files.createFile(historyFilePath); // Create an empty file

        SearchHistoryManager newManager = new SearchHistoryManager(historyFilePath); // Assumed refactored constructor
        assertTrue(newManager.getSearchHistory().isEmpty());
    }


    @Test
    void saveHistory_isCorrectlyWrittenToFile() throws IOException {
        searchHistoryManager.addSearchQuery("save test 1");
        searchHistoryManager.addSearchQuery("save test 2");

        // History is saved on each add. Verify file content.
        List<String> lines = Files.readAllLines(historyFilePath);
        assertEquals(2, lines.size());
        assertEquals("save test 2", lines.get(0)); // Most recent
        assertEquals("save test 1", lines.get(1));
    }

    @Test
    void getSearchHistory_returnsImmutableOrCopy() {
        searchHistoryManager.addSearchQuery("query test");
        List<String> history = searchHistoryManager.getSearchHistory();

        assertThrows(UnsupportedOperationException.class, () -> {
            history.add("another query"); // Should fail if immutable
        }, "Modifying the list returned by getSearchHistory should not be allowed or should not affect the original.");
    }

    @Test
    void getHistoryFilePath_returnsCorrectPath() {
        assertEquals(historyFilePath, searchHistoryManager.getHistoryFilePath());
    }

    @Test
    void persistenceAcrossInstances() throws IOException {
        // Instance 1 adds queries
        searchHistoryManager.addSearchQuery("persist query 1");
        searchHistoryManager.addSearchQuery("persist query 2");
        // Queries are saved by addSearchQuery

        // Instance 2 should load these queries
        SearchHistoryManager newManager = new SearchHistoryManager(historyFilePath); // Assumed refactored constructor
        List<String> loadedHistory = newManager.getSearchHistory();
        assertEquals(2, loadedHistory.size());
        assertEquals("persist query 2", loadedHistory.get(0));
        assertEquals("persist query 1", loadedHistory.get(1));
    }
}
