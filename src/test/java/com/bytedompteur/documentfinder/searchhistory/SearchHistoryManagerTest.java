package com.bytedompteur.documentfinder.searchhistory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchHistoryManagerTest {

    private SearchHistoryManager searchHistoryManager;
    private Path historyFilePath;

    @TempDir
    Path tempDir; // JUnit 5 temporary directory for each test method

    @BeforeEach
    void setUp() throws IOException {
        // The historyFilePath will be unique for each test, inside tempDir
        historyFilePath = tempDir.resolve("test_search_history.json");
        // Use the constructor that accepts a Path to control file location
        searchHistoryManager = new SearchHistoryManager(historyFilePath);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Not strictly necessary with @TempDir as JUnit cleans it up,
        // but good for explicitness if any test manipulates files outside the manager.
        // Files.deleteIfExists(historyFilePath);
    }

    @Test
    void testAddQuery_newList() {
        searchHistoryManager.addSearchQuery("query1");
        List<String> history = searchHistoryManager.getSearchHistory();
        assertEquals(1, history.size());
        assertEquals("query1", history.get(0));
    }

    @Test
    void testAddQuery_existingQuery() {
        searchHistoryManager.addSearchQuery("query1");
        searchHistoryManager.addSearchQuery("query2");
        searchHistoryManager.addSearchQuery("query1"); // Add existing query

        List<String> history = searchHistoryManager.getSearchHistory();
        assertEquals(2, history.size());
        assertEquals("query1", history.get(0)); // Should be at the top
        assertEquals("query2", history.get(1));
    }

    @Test
    void testAddQuery_historyAtLimit() {
        // Fill history to MAX_HISTORY_SIZE (default 10)
        for (int i = 1; i <= 10; i++) {
            searchHistoryManager.addSearchQuery("query" + i);
        }
        assertEquals(10, searchHistoryManager.getSearchHistory().size());
        assertEquals("query10", searchHistoryManager.getSearchHistory().get(0)); // Most recent
        assertEquals("query1", searchHistoryManager.getSearchHistory().get(9));  // Oldest

        searchHistoryManager.addSearchQuery("query11"); // Add 11th query
        List<String> history = searchHistoryManager.getSearchHistory();
        assertEquals(10, history.size());
        assertEquals("query11", history.get(0)); // Newest
        assertEquals("query2", history.get(9));  // Oldest should be query1, so query2 is now oldest among remaining
        assertFalse(history.contains("query1"), "query1 should have been removed");
    }

    @Test
    void testAddQuery_addMultipleQueries() {
        searchHistoryManager.addSearchQuery("first");
        searchHistoryManager.addSearchQuery("second");
        searchHistoryManager.addSearchQuery("third");

        List<String> history = searchHistoryManager.getSearchHistory();
        assertEquals(3, history.size());
        assertEquals("third", history.get(0));
        assertEquals("second", history.get(1));
        assertEquals("first", history.get(2));

        // Add more to reach limit
        for (int i = 4; i <= 10; i++) {
            searchHistoryManager.addSearchQuery("query" + i);
        }
        searchHistoryManager.addSearchQuery("query11"); // Exceed limit
        searchHistoryManager.addSearchQuery("query12"); // Exceed limit again

        history = searchHistoryManager.getSearchHistory();
        assertEquals(10, history.size());
        assertEquals("query12", history.get(0));
        assertEquals("query11", history.get(1));
        assertEquals("query10", history.get(2));
        // ...
        assertEquals("third", history.get(9)); // "third" should be the oldest now
        assertFalse(history.contains("first"));
        assertFalse(history.contains("second"));
    }

    @Test
    void addSearchQuery_addNullOrEmptyQuery_shouldNotAdd() {
        searchHistoryManager.addSearchQuery(null);
        assertEquals(0, searchHistoryManager.getSearchHistory().size());
        searchHistoryManager.addSearchQuery("");
        assertEquals(0, searchHistoryManager.getSearchHistory().size());
        searchHistoryManager.addSearchQuery("   "); // Whitespace only
        assertEquals(0, searchHistoryManager.getSearchHistory().size());
    }


    @Test
    void testLoadHistory_fileExists() throws IOException {
        // Prepare a history file manually
        List<String> initialQueries = Arrays.asList("old_query1", "old_query2", "old_query3");
        Files.write(historyFilePath, initialQueries); // Write directly to the temp path

        // Create a new manager instance to load from this file
        SearchHistoryManager newManager = new SearchHistoryManager(historyFilePath);
        List<String> loadedHistory = newManager.getSearchHistory();

        assertEquals(3, loadedHistory.size());
        assertEquals("old_query1", loadedHistory.get(0));
        assertEquals("old_query2", loadedHistory.get(1));
        assertEquals("old_query3", loadedHistory.get(2));
    }

    @Test
    void testLoadHistory_fileNotExists() {
        // historyFilePath is in tempDir, and we haven't written to it.
        // SearchHistoryManager constructor calls loadHistory, so it's already loaded.
        assertTrue(searchHistoryManager.getSearchHistory().isEmpty());

        // For clarity, create a new manager pointing to a non-existent file path
        SearchHistoryManager newManager = new SearchHistoryManager(tempDir.resolve("non_existent_history.json"));
        assertTrue(newManager.getSearchHistory().isEmpty());
    }

    @Test
    void testLoadHistory_emptyFile() throws IOException {
        Files.createFile(historyFilePath); // Create an empty file

        SearchHistoryManager newManager = new SearchHistoryManager(historyFilePath);
        assertTrue(newManager.getSearchHistory().isEmpty());
    }

    @Test
    void testPersistence_saveAndLoad() {
        searchHistoryManager.addSearchQuery("persist_query1");
        searchHistoryManager.addSearchQuery("persist_query2");
        // Queries are saved to historyFilePath by addSearchQuery via saveHistory()

        // Create a new manager instance, it should load from historyFilePath
        SearchHistoryManager newManager = new SearchHistoryManager(historyFilePath);
        List<String> loadedHistory = newManager.getSearchHistory();

        assertEquals(2, loadedHistory.size());
        assertEquals("persist_query2", loadedHistory.get(0)); // Most recent
        assertEquals("persist_query1", loadedHistory.get(1));
    }

    @Test
    void getSearchHistory_returnsUnmodifiableList() {
        searchHistoryManager.addSearchQuery("query1");
        List<String> history = searchHistoryManager.getSearchHistory();

        assertThrows(UnsupportedOperationException.class, () -> {
            history.add("attempt_to_modify");
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            history.remove(0);
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            history.clear();
        });
    }

    @Test
    void loadHistory_fileWithMoreThanMaxEntries_shouldLoadOnlyMaxEntries() throws IOException {
        List<String> oversizedQueries = new java.util.ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            oversizedQueries.add("oversized_query" + i);
        }
        Files.write(historyFilePath, oversizedQueries);

        SearchHistoryManager newManager = new SearchHistoryManager(historyFilePath);
        List<String> loadedHistory = newManager.getSearchHistory();

        assertEquals(10, loadedHistory.size(), "Should only load up to MAX_HISTORY_SIZE entries.");
        // It should load the first MAX_HISTORY_SIZE entries from the file.
        assertEquals("oversized_query1", loadedHistory.get(0));
        assertEquals("oversized_query10", loadedHistory.get(9));
    }

    @Test
    void loadHistory_fileWithEmptyLines_shouldIgnoreEmptyLines() throws IOException {
        List<String> queriesWithEmptyLines = Arrays.asList("queryA", "", "queryB", "   ", "queryC");
        Files.write(historyFilePath, queriesWithEmptyLines);

        SearchHistoryManager newManager = new SearchHistoryManager(historyFilePath);
        List<String> loadedHistory = newManager.getSearchHistory();

        assertEquals(3, loadedHistory.size());
        assertEquals("queryA", loadedHistory.get(0));
        assertEquals("queryB", loadedHistory.get(1));
        assertEquals("queryC", loadedHistory.get(2));
    }
}
