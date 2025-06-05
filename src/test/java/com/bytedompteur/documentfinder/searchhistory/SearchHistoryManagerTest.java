package com.bytedompteur.documentfinder.searchhistory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.*; // AssertJ imports

class SearchHistoryManagerTest {

    private SearchHistoryManager sut; // Renamed from searchHistoryManager
    private Path historyFilePath;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // arrange
        historyFilePath = tempDir.resolve("test_search_history.json");
        sut = new SearchHistoryManager(historyFilePath); // Use sut
    }

    @Test
    void testAddQuery_newList_shouldAddQuery() {
        // arrange
        String query = "query1";

        // act
        sut.addSearchQuery(query);
        List<String> history = sut.getSearchHistory();

        // assert
        assertThat(history).hasSize(1);
        assertThat(history.get(0)).isEqualTo(query);
    }

    @Test
    void testAddQuery_existingQuery_shouldMoveToTop() {
        // arrange
        sut.addSearchQuery("query1");
        sut.addSearchQuery("query2");

        // act
        sut.addSearchQuery("query1"); // Add existing query
        List<String> history = sut.getSearchHistory();

        // assert
        assertThat(history).hasSize(2);
        assertThat(history).containsExactly("query1", "query2");
    }

    @Test
    void testAddQuery_historyAtLimit_shouldRemoveOldest() {
        // arrange
        for (int i = 1; i <= 10; i++) {
            sut.addSearchQuery("query" + i);
        }
        // Initial assertions to confirm setup
        assertThat(sut.getSearchHistory()).hasSize(10);
        assertThat(sut.getSearchHistory().get(0)).isEqualTo("query10");
        assertThat(sut.getSearchHistory().get(9)).isEqualTo("query1");

        // act
        sut.addSearchQuery("query11"); // Add 11th query
        List<String> history = sut.getSearchHistory();

        // assert
        assertThat(history).hasSize(10).as("History size should not exceed MAX_HISTORY_SIZE.");
        assertThat(history.get(0)).isEqualTo("query11").as("Newest query should be at the top.");
        assertThat(history.get(9)).isEqualTo("query2").as("Oldest query 'query1' should have been removed.");
        assertThat(history).doesNotContain("query1").as("query1 should have been removed.");
    }

    @Test
    void testAddQuery_addMultipleQueries_verifyOrderAndLimit() {
        // arrange
        sut.addSearchQuery("first");
        sut.addSearchQuery("second");
        sut.addSearchQuery("third");

        // act
        List<String> initialHistory = sut.getSearchHistory();

        // assert
        assertThat(initialHistory).hasSize(3);
        assertThat(initialHistory).containsExactly("third", "second", "first");

        // arrange more
        for (int i = 4; i <= 11; i++) { // Add 8 more queries
            sut.addSearchQuery("query" + i);
        }

        // act
        List<String> finalHistory = sut.getSearchHistory();

        // assert
        assertThat(finalHistory).hasSize(10).as("History should be trimmed to MAX_HISTORY_SIZE.");
        assertThat(finalHistory.get(0)).isEqualTo("query11");
        assertThat(finalHistory).doesNotContain("first");
        assertThat(finalHistory.get(9)).isEqualTo("query2"); // Oldest remaining after 'first' pushed out
    }

    @Test
    void testAddQuery_nullOrEmptyQuery_shouldNotAdd() {
        // act
        sut.addSearchQuery(null);
        // assert
        assertThat(sut.getSearchHistory()).isEmpty();

        // act
        sut.addSearchQuery("");
        // assert
        assertThat(sut.getSearchHistory()).isEmpty();

        // act
        sut.addSearchQuery("   "); // Whitespace only
        // assert
        assertThat(sut.getSearchHistory()).isEmpty();
    }

    @Test
    void testLoadHistory_fileExists_shouldLoadQueries() throws IOException {
        // arrange
        List<String> initialQueries = Arrays.asList("old_query1", "old_query2", "old_query3");
        Files.write(historyFilePath, initialQueries, StandardCharsets.UTF_8);
        SearchHistoryManager newManager = new SearchHistoryManager(historyFilePath); // Renamed for clarity

        // act
        List<String> loadedHistory = newManager.getSearchHistory();

        // assert
        assertThat(loadedHistory).hasSize(3);
        assertThat(loadedHistory).containsExactly("old_query1", "old_query2", "old_query3");
    }

    @Test
    void testLoadHistory_fileNotExists_shouldReturnEmptyHistory() {
        // arrange
        // sut is initialized with historyFilePath which doesn't exist yet (or is empty from @TempDir)
        // act
        List<String> history = sut.getSearchHistory();
        // assert
        assertThat(history).isEmpty();

        // arrange more for clarity with a different path
        Path nonExistentPath = tempDir.resolve("non_existent_history.json");
        SearchHistoryManager newManager = new SearchHistoryManager(nonExistentPath);

        // act
        List<String> newHistory = newManager.getSearchHistory();

        // assert
        assertThat(newHistory).isEmpty();
        assertThat(nonExistentPath).doesNotExist(); // Confirming the file itself doesn't get created by load
    }

    @Test
    void testLoadHistory_emptyFile_shouldReturnEmptyHistory() throws IOException {
        // arrange
        Files.createFile(historyFilePath); // Create an empty file
        SearchHistoryManager newManager = new SearchHistoryManager(historyFilePath);

        // act
        List<String> loadedHistory = newManager.getSearchHistory();

        // assert
        assertThat(loadedHistory).isEmpty();
        assertThat(historyFilePath).exists(); // File should exist
        assertThat(Files.size(historyFilePath)).isEqualTo(0); // And be empty
    }

    @Test
    void testPersistence_saveAndLoad_queriesShouldPersist() {
        // arrange
        sut.addSearchQuery("persist_query1");
        sut.addSearchQuery("persist_query2"); // This saves the history

        // act: Create a new manager instance, it should load from historyFilePath
        SearchHistoryManager newManager = new SearchHistoryManager(historyFilePath);
        List<String> loadedHistory = newManager.getSearchHistory();

        // assert
        assertThat(loadedHistory).hasSize(2);
        assertThat(loadedHistory).containsExactly("persist_query2", "persist_query1");
    }

    @Test
    void testGetSearchHistory_returnsUnmodifiableList() {
        // arrange
        sut.addSearchQuery("query1");

        // act
        List<String> history = sut.getSearchHistory();

        // assert
        assertThatThrownBy(() -> history.add("attempt_to_modify"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> history.remove(0))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testLoadHistory_fileWithMoreThanMaxEntries_shouldLoadOnlyMaxEntriesFromFileOrder() throws IOException {
        // arrange
        List<String> oversizedQueries = new LinkedList<>();
        for (int i = 1; i <= 15; i++) {
            oversizedQueries.add("oversized_query" + i);
        }
        Files.write(historyFilePath, oversizedQueries, StandardCharsets.UTF_8);
        SearchHistoryManager newManager = new SearchHistoryManager(historyFilePath);

        // act
        List<String> loadedHistory = newManager.getSearchHistory();

        // assert
        assertThat(loadedHistory).hasSize(10).as("Should only load up to MAX_HISTORY_SIZE entries.");
        assertThat(loadedHistory.get(0)).isEqualTo("oversized_query1");
        assertThat(loadedHistory.get(9)).isEqualTo("oversized_query10");
    }

    @Test
    void testLoadHistory_fileWithEmptyLines_shouldIgnoreThem() throws IOException {
        // arrange
        List<String> queriesWithEmptyLines = Arrays.asList("queryA", "", "queryB", "   ", "queryC", "");
        Files.write(historyFilePath, queriesWithEmptyLines, StandardCharsets.UTF_8);
        SearchHistoryManager newManager = new SearchHistoryManager(historyFilePath);

        // act
        List<String> loadedHistory = newManager.getSearchHistory();

        // assert
        assertThat(loadedHistory).hasSize(3).as("Empty lines should be ignored during loading.");
        assertThat(loadedHistory).containsExactly("queryA", "queryB", "queryC");
    }
}
