package com.bytedompteur.documentfinder.searchhistory.adapter.out;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SearchHistoryFileRepositoryTest {

    private SearchHistoryFileRepository sut;
    private Path historyFile; // Specific file path within tempDir

    @TempDir
    Path tempDir; // JUnit 5 temporary directory for each test

    @BeforeEach
    void setUp() {
        // arrange
        historyFile = tempDir.resolve("test_history.json");
        sut = new SearchHistoryFileRepository(historyFile);
    }

    @Test
    void saveAndLoad_shouldPersistAndRetrieveQueries_whenListIsNotEmpty() throws IOException {
        // arrange
        List<String> queriesToSave = Arrays.asList("query1", "query2", "query3");

        // act
        sut.save(queriesToSave);
        List<String> loadedQueries = sut.load();

        // assert
        assertThat(historyFile).exists();
        assertThat(loadedQueries).hasSize(3).containsExactlyElementsOf(queriesToSave);

        // Verify file content directly
        List<String> fileLines = Files.readAllLines(historyFile, StandardCharsets.UTF_8);
        assertThat(fileLines).isEqualTo(queriesToSave);
    }

    @Test
    void save_shouldOverwriteExistingFile_whenSavingNewList() throws IOException {
        // arrange
        List<String> initialQueries = Arrays.asList("initial1", "initial2");
        sut.save(initialQueries); // Save initial list
        assertThat(Files.readAllLines(historyFile)).hasSize(2);

        List<String> newQueries = Arrays.asList("new1", "new2", "new3");

        // act
        sut.save(newQueries);
        List<String> loadedQueries = sut.load();

        // assert
        assertThat(loadedQueries).hasSize(3).containsExactlyElementsOf(newQueries);
    }

    @Test
    void save_shouldCreateParentDirectories_whenTheyDoNotExist() throws IOException {
        // arrange
        Path deepHistoryFile = tempDir.resolve("deep/new/parent/dir/history.json");
        SearchHistoryFileRepository deepSut = new SearchHistoryFileRepository(deepHistoryFile);
        List<String> queriesToSave = Arrays.asList("queryA", "queryB");
        assertThat(deepHistoryFile.getParent()).doesNotExist();

        // act
        deepSut.save(queriesToSave);

        // assert
        assertThat(deepHistoryFile).exists();
        assertThat(Files.readAllLines(deepHistoryFile)).isEqualTo(queriesToSave);
    }


    @Test
    void load_shouldReturnEmptyList_whenFileDoesNotExist() {
        // arrange
        // File does not exist as it's not created by setUp's sut or this test yet.
        assertThat(historyFile).doesNotExist();

        // act
        List<String> loadedQueries = sut.load();

        // assert
        assertThat(loadedQueries).isEmpty();
    }

    @Test
    void load_shouldReturnEmptyList_whenFileIsEmpty() throws IOException {
        // arrange
        Files.createFile(historyFile); // Create an empty file
        assertThat(historyFile).isEmptyFile();


        // act
        List<String> loadedQueries = sut.load();

        // assert
        assertThat(loadedQueries).isEmpty();
    }

    @Test
    void load_shouldIgnoreEmptyOrBlankLines_whenFileContainsThem() throws IOException {
        // arrange
        List<String> queriesWithBlanks = Arrays.asList("query1", "", "  ", "query2", "query3", "");
        Files.write(historyFile, queriesWithBlanks, StandardCharsets.UTF_8);

        // act
        List<String> loadedQueries = sut.load();

        // assert
        assertThat(loadedQueries).hasSize(3).containsExactly("query1", "query2", "query3");
    }

    @Test
    void save_shouldHandleEmptyList_creatingEmptyFile() throws IOException {
        // arrange
        List<String> emptyList = Collections.emptyList();

        // act
        sut.save(emptyList);

        // assert
        assertThat(historyFile).exists().isEmptyFile();
        List<String> loadedQueries = sut.load();
        assertThat(loadedQueries).isEmpty();
    }

    @Test
    void load_shouldHandleNonReadableFile_returningEmptyList() throws IOException {
        // arrange
        Files.writeString(historyFile, "some content", StandardCharsets.UTF_8);
        assertThat(historyFile.toFile().setReadable(false)).isTrue(); // Make file non-readable

        // act
        List<String> loadedQueries = sut.load();

        // assert
        assertThat(loadedQueries).isEmpty();

        // Cleanup: make readable again so @TempDir can clean it up without issues on some OS
        historyFile.toFile().setReadable(true);
    }
}
