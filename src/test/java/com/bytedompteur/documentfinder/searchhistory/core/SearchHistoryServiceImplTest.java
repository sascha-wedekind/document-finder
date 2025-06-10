package com.bytedompteur.documentfinder.searchhistory.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchHistoryServiceImplTest {

    @Mock
    private SearchHistoryRepository mockRepository;

    private SearchHistoryServiceImpl sut;

    @Captor
    private ArgumentCaptor<List<String>> historyListCaptor;

    // MAX_HISTORY_SIZE is private in SearchHistoryServiceImpl, but we know it's 10 for testing.
    private static final int MAX_HISTORY_SIZE = 10;

    @BeforeEach
    void setUp() {
        // arrange
        // Mock the load behavior for most tests. Specific tests can override.
        when(mockRepository.load()).thenReturn(new LinkedList<>());
        sut = new SearchHistoryServiceImpl(mockRepository);
    }

    @Test
    void constructor_shouldLoadAndTrimHistory_whenRepositoryReturnsOversizedList() {
        // arrange
        List<String> oversizedList = new LinkedList<>();
        for (int i = 1; i <= MAX_HISTORY_SIZE + 5; i++) {
            oversizedList.add("query" + i);
        }
        when(mockRepository.load()).thenReturn(oversizedList);

        // act
        SearchHistoryServiceImpl newSut = new SearchHistoryServiceImpl(mockRepository);
        List<String> history = newSut.getSearchHistory();

        // assert
        assertThat(history).hasSize(MAX_HISTORY_SIZE);
        // It should keep the *earliest* MAX_HISTORY_SIZE items from the loaded list
        // because the constructor trims by removing from the end (removeLast).
        assertThat(history.get(0)).isEqualTo("query1");
        assertThat(history.get(MAX_HISTORY_SIZE - 1)).isEqualTo("query" + MAX_HISTORY_SIZE);
        verify(mockRepository).load(); // Verify load was called
    }

    @Test
    void constructor_shouldLoadHistory_whenRepositoryReturnsEmptyList() {
        // arrange: setUp already mocks load to return empty list
        // act
        List<String> history = sut.getSearchHistory();
        // assert
        assertThat(history).isEmpty();
        verify(mockRepository).load();
    }

    @Test
    void addSearchQuery_shouldAddQueryAndSave_whenHistoryIsEmpty() {
        // arrange
        String query = "new query";

        // act
        sut.addSearchQuery(query);
        List<String> history = sut.getSearchHistory();

        // assert
        assertThat(history).hasSize(1).containsExactly(query);
        verify(mockRepository).save(historyListCaptor.capture());
        assertThat(historyListCaptor.getValue()).containsExactly(query);
    }

    @Test
    void addSearchQuery_shouldMoveQueryToTopAndSave_whenQueryAlreadyExists() {
        // arrange
        sut.addSearchQuery("query1");
        sut.addSearchQuery("query2"); // Initial state: [query2, query1]

        // act
        sut.addSearchQuery("query1"); // Add existing query
        List<String> history = sut.getSearchHistory();

        // assert
        assertThat(history).hasSize(2).containsExactly("query1", "query2");
        verify(mockRepository, times(3)).save(historyListCaptor.capture()); // 2 initial + 1 for this act
        assertThat(historyListCaptor.getValue()).containsExactly("query1", "query2");
    }

    @Test
    void addSearchQuery_shouldRemoveOldestQueryAndSave_whenHistoryIsFull() {
        // arrange
        for (int i = 1; i <= MAX_HISTORY_SIZE; i++) {
            sut.addSearchQuery("query" + i);
        }
        // History is now [query10, query9, ..., query1]

        // act
        sut.addSearchQuery("query_new"); // This should push out "query1"
        List<String> history = sut.getSearchHistory();

        // assert
        assertThat(history).hasSize(MAX_HISTORY_SIZE);
        assertThat(history.get(0)).isEqualTo("query_new");
        assertThat(history).doesNotContain("query1");
        assertThat(history.get(MAX_HISTORY_SIZE - 1)).isEqualTo("query2"); // query2 becomes the oldest

        verify(mockRepository, times(MAX_HISTORY_SIZE + 1)).save(historyListCaptor.capture());
        assertThat(historyListCaptor.getValue().get(0)).isEqualTo("query_new");
        assertThat(historyListCaptor.getValue()).doesNotContain("query1");
    }

    @Test
    void addSearchQuery_shouldNotAddQuery_whenQueryIsNull() {
        // arrange
        // Initial history is empty or has some items, doesn't matter for this test

        // act
        sut.addSearchQuery(null);
        List<String> history = sut.getSearchHistory();

        // assert
        assertThat(history).isEmpty(); // Or its initial size if it had items
        verify(mockRepository, never()).save(anyList()); // Save should not be called
    }

    @Test
    void addSearchQuery_shouldNotAddQuery_whenQueryIsEmptyString() {
        // arrange
        // act
        sut.addSearchQuery("");
        List<String> history = sut.getSearchHistory();
        // assert
        assertThat(history).isEmpty();
        verify(mockRepository, never()).save(anyList());
    }

    @Test
    void addSearchQuery_shouldNotAddQuery_whenQueryIsWhitespaceOnly() {
        // arrange
        // act
        sut.addSearchQuery("   ");
        List<String> history = sut.getSearchHistory();
        // assert
        assertThat(history).isEmpty();
        verify(mockRepository, never()).save(anyList());
    }

    @Test
    void addSearchQuery_shouldTrimQuery_whenQueryHasLeadingOrTrailingWhitespace() {
        // arrange
        String queryWithSpace = "  spaced query  ";
        String trimmedQuery = "spaced query";

        // act
        sut.addSearchQuery(queryWithSpace);
        List<String> history = sut.getSearchHistory();

        // assert
        assertThat(history).hasSize(1).containsExactly(trimmedQuery);
        verify(mockRepository).save(historyListCaptor.capture());
        assertThat(historyListCaptor.getValue()).containsExactly(trimmedQuery);
    }


    @Test
    void getSearchHistory_shouldReturnUnmodifiableList_always() {
        // arrange
        sut.addSearchQuery("query1");

        // act
        List<String> history = sut.getSearchHistory();

        // assert
        assertThat(history).isUnmodifiable(); // AssertJ way to check for unmodifiability if available
                                            // Or check specific operations:
        assertThatThrownBy(() -> history.add("another"))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void getSearchHistory_shouldReturnCopyOfInternalList_notReference() {
        // arrange
        sut.addSearchQuery("query1");
        List<String> history1 = sut.getSearchHistory();
        assertThat(history1).containsExactly("query1");

        // act
        sut.addSearchQuery("query2"); // Modify internal list
        List<String> history2 = sut.getSearchHistory();
                                     // history1 should not be affected if it's a copy

        // assert
        assertThat(history1).hasSize(1).containsExactly("query1"); // Unchanged
        assertThat(history2).hasSize(2).containsExactly("query2", "query1"); // Reflects current state
    }
}
