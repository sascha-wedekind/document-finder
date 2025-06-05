package com.bytedompteur.documentfinder.searchhistory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList; // To match SearchHistoryManager's internal list type for some tests

import static org.junit.jupiter.api.Assertions.*;

class SearchHistoryManagerTest {

    private SearchHistoryManager searchHistoryManager;
    private Path historyFilePath; // Path to the history file in the temp directory

    @TempDir
    Path tempDir; // JUnit 5 temporary directory, a new one for each test method

    @BeforeEach
    void setUp() throws IOException {
        // Define a unique history file path for each test within the tempDir
        historyFilePath = tempDir.resolve("test_search_history.json");
        // Use the constructor that accepts a Path to control file location for tests
        searchHistoryManager = new SearchHistoryManager(historyFilePath);
    }

    // @AfterEach is not strictly necessary for file cleanup when using @TempDir,
    // as JUnit handles cleanup of the tempDir. However, if tests created files
    // outside SearchHistoryManager's direct control but within tempDir,
    // explicit cleanup might be considered. For this class, it should be fine.

    @Test
    void testAddQuery_newList_shouldAddQuery() {
        searchHistoryManager.addSearchQuery("query1");
        List<String> history = searchHistoryManager.getSearchHistory();
        assertEquals(1, history.size());
        assertEquals("query1", history.get(0));
    }

    @Test
    void testAddQuery_existingQuery_shouldMoveToTop() {
        searchHistoryManager.addSearchQuery("query1");
        searchHistoryManager.addSearchQuery("query2");
        searchHistoryManager.addSearchQuery("query1"); // Add existing query

        List<String> history = searchHistoryManager.getSearchHistory();
        assertEquals(2, history.size());
        assertEquals("query1", history.get(0), "Existing query should move to the top.");
        assertEquals("query2", history.get(1));
    }

    @Test
    void testAddQuery_historyAtLimit_shouldRemoveOldest() {
        // Fill history to MAX_HISTORY_SIZE (10)
        for (int i = 1; i <= 10; i++) {
            searchHistoryManager.addSearchQuery("query" + i);
        }
        assertEquals(10, searchHistoryManager.getSearchHistory().size());
        assertEquals("query10", searchHistoryManager.getSearchHistory().get(0)); // Most recent
        assertEquals("query1", searchHistoryManager.getSearchHistory().get(9));  // Oldest

        searchHistoryManager.addSearchQuery("query11"); // Add 11th query
        List<String> history = searchHistoryManager.getSearchHistory();
        assertEquals(10, history.size(), "History size should not exceed MAX_HISTORY_SIZE.");
        assertEquals("query11", history.get(0), "Newest query should be at the top.");
        assertEquals("query2", history.get(9), "Oldest query 'query1' should have been removed.");
        assertFalse(history.contains("query1"), "query1 should have been removed.");
    }

    @Test
    void testAddQuery_addMultipleQueries_verifyOrderAndLimit() {
        searchHistoryManager.addSearchQuery("first");
        searchHistoryManager.addSearchQuery("second");
        searchHistoryManager.addSearchQuery("third");

        List<String> history = searchHistoryManager.getSearchHistory();
        assertEquals(3, history.size());
        assertEquals("third", history.get(0));
        assertEquals("second", history.get(1));
        assertEquals("first", history.get(2));

        // Add more to reach and exceed limit
        for (int i = 4; i <= 11; i++) { // Add 8 more queries (total 3+8=11)
            searchHistoryManager.addSearchQuery("query" + i);
        }

        history = searchHistoryManager.getSearchHistory();
        assertEquals(10, history.size(), "History should be trimmed to MAX_HISTORY_SIZE.");
        assertEquals("query11", history.get(0)); // Last added
        assertEquals("query10", history.get(1));
        // ...
        assertEquals("query2", history.get(9)); // "second" was query2 effectively
        assertFalse(history.contains("first"), "'first' should be pushed out.");
        assertFalse(history.contains("second"), "'second' should be pushed out by query11.");
        assertTrue(history.contains("third"), "'third' should remain initially.");
        // Correction: after query4..query11 are added, 'third' would be pushed out too.
        // query11, query10, query9, query8, query7, query6, query5, query4, third, second -> first (out)
        // query11 is added: query11, query10, query9, query8, query7, query6, query5, query4, third, second. (size 10)
        // Let's re-verify the expected oldest element.
        // Initial: third, second, first
        // Add query4: q4,third,second,first
        // ...
        // Add query10: q10,q9,q8,q7,q6,q5,q4,third,second,first
        // Add query11: q11,q10,q9,q8,q7,q6,q5,q4,third,second (first is out)
        // So, the oldest should be "second". My previous assertion was slightly off.
        assertEquals("second", history.get(9));
        assertFalse(history.contains("first"));
    }

    @Test
    void testAddQuery_nullOrEmptyQuery_shouldNotAdd() {
        searchHistoryManager.addSearchQuery(null);
        assertEquals(0, searchHistoryManager.getSearchHistory().size(), "Null query should not be added.");

        searchHistoryManager.addSearchQuery("");
        assertEquals(0, searchHistoryManager.getSearchHistory().size(), "Empty query should not be added.");

        searchHistoryManager.addSearchQuery("   "); // Whitespace only
        assertEquals(0, searchHistoryManager.getSearchHistory().size(), "Whitespace-only query should not be added.");
    }

    @Test
    void testLoadHistory_fileExists_shouldLoadQueries() throws IOException {
        // Prepare a history file manually
        List<String> initialQueries = Arrays.asList("old_query1", "old_query2", "old_query3");
        Files.write(historyFilePath, initialQueries, StandardCharsets.UTF_8);

        // Create a new manager instance to load from this file
        SearchHistoryManager newManager = new SearchHistoryManager(historyFilePath);
        List<String> loadedHistory = newManager.getSearchHistory();

        assertEquals(3, loadedHistory.size());
        assertEquals("old_query1", loadedHistory.get(0)); // Order should be as read from file
        assertEquals("old_query2", loadedHistory.get(1));
        assertEquals("old_query3", loadedHistory.get(2));
    }

    @Test
    void testLoadHistory_fileNotExists_shouldReturnEmptyHistory() {
        // historyFilePath is in tempDir, and we haven't written to it.
        // The searchHistoryManager in setUp() already calls loadHistory().
        assertTrue(searchHistoryManager.getSearchHistory().isEmpty(), "History should be empty if file doesn't exist.");

        // For absolute clarity, create another manager pointing to a definitely non-existent file
        Path nonExistentPath = tempDir.resolve("non_existent_history.json");
        SearchHistoryManager newManager = new SearchHistoryManager(nonExistentPath);
        assertTrue(newManager.getSearchHistory().isEmpty());
    }

    @Test
    void testLoadHistory_emptyFile_shouldReturnEmptyHistory() throws IOException {
        Files.createFile(historyFilePath); // Create an empty file

        SearchHistoryManager newManager = new SearchHistoryManager(historyFilePath);
        assertTrue(newManager.getSearchHistory().isEmpty(), "History should be empty if file is empty.");
    }

    @Test
    void testPersistence_saveAndLoad_queriesShouldPersist() {
        searchHistoryManager.addSearchQuery("persist_query1");
        searchHistoryManager.addSearchQuery("persist_query2");
        // Queries are saved to historyFilePath by addSearchQuery via saveHistory()

        // Create a new manager instance; it should load from historyFilePath
        SearchHistoryManager newManager = new SearchHistoryManager(historyFilePath);
        List<String> loadedHistory = newManager.getSearchHistory();

        assertEquals(2, loadedHistory.size());
        assertEquals("persist_query2", loadedHistory.get(0), "Most recent query should be first after loading.");
        assertEquals("persist_query1", loadedHistory.get(1));
    }

    @Test
    void testGetSearchHistory_returnsUnmodifiableList() {
        searchHistoryManager.addSearchQuery("query1");
        List<String> history = searchHistoryManager.getSearchHistory();

        assertThrows(UnsupportedOperationException.class, () -> history.add("attempt_to_modify"),
                "Should not be able to add to the list returned by getSearchHistory.");
        assertThrows(UnsupportedOperationException.class, () -> history.remove(0),
                "Should not be able to remove from the list returned by getSearchHistory.");
    }

    @Test
    void testLoadHistory_fileWithMoreThanMaxEntries_shouldLoadOnlyMaxEntriesFromFileOrder() throws IOException {
        List<String> oversizedQueries = new LinkedList<>();
        for (int i = 1; i <= 15; i++) {
            oversizedQueries.add("oversized_query" + i);
        }
        Files.write(historyFilePath, oversizedQueries, StandardCharsets.UTF_8);

        SearchHistoryManager newManager = new SearchHistoryManager(historyFilePath);
        List<String> loadedHistory = newManager.getSearchHistory();

        assertEquals(10, loadedHistory.size(), "Should only load up to MAX_HISTORY_SIZE entries.");
        // The loadHistory method should load the first MAX_HISTORY_SIZE entries from the file.
        assertEquals("oversized_query1", loadedHistory.get(0));
        assertEquals("oversized_query10", loadedHistory.get(9));
    }

    @Test
    void testLoadHistory_fileWithEmptyLines_shouldIgnoreThem() throws IOException {
        List<String> queriesWithEmptyLines = Arrays.asList("queryA", "", "queryB", "   ", "queryC", "");
        Files.write(historyFilePath, queriesWithEmptyLines, StandardCharsets.UTF_8);

        SearchHistoryManager newManager = new SearchHistoryManager(historyFilePath);
        List<String> loadedHistory = newManager.getSearchHistory();

        assertEquals(3, loadedHistory.size(), "Empty lines should be ignored during loading.");
        assertEquals("queryA", loadedHistory.get(0));
        assertEquals("queryB", loadedHistory.get(1));
        assertEquals("queryC", loadedHistory.get(2));
    }
}
