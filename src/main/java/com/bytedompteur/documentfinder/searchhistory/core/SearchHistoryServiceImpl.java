package com.bytedompteur.documentfinder.searchhistory.core;

import com.bytedompteur.documentfinder.searchhistory.adapter.in.SearchHistoryService;
import jakarta.inject.Inject; // Using Jakarta inject
import jakarta.inject.Singleton; // Assuming Singleton scope is desired

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Singleton // Add Singleton scope annotation
public class SearchHistoryServiceImpl implements SearchHistoryService {

    private static final int MAX_HISTORY_SIZE = 10;
    private final LinkedList<String> searchQueries; // Use LinkedList for efficient addFirst/removeLast
    private final SearchHistoryRepository historyRepository;

    @Inject
    public SearchHistoryServiceImpl(SearchHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
        // Load queries and ensure it's mutable for internal operations
        List<String> loadedQueries = this.historyRepository.load();
        if (loadedQueries instanceof LinkedList) {
            this.searchQueries = (LinkedList<String>) loadedQueries;
        } else {
            this.searchQueries = new LinkedList<>(loadedQueries);
        }

        // Ensure history doesn't exceed max size after loading, trimming oldest if necessary
        while (this.searchQueries.size() > MAX_HISTORY_SIZE) {
            this.searchQueries.removeLast(); // removeLast is efficient for LinkedList
        }
    }

    @Override
    public void addSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }
        String trimmedQuery = query.trim();

        // Remove if exists to avoid duplicates and move to top
        // This operation can be slow on LinkedList if the list is large,
        // but for MAX_HISTORY_SIZE=10, it's acceptable.
        searchQueries.remove(trimmedQuery);

        searchQueries.addFirst(trimmedQuery); // Add to the beginning (most recent)

        // Enforce max history size by removing the oldest query (from the end)
        if (searchQueries.size() > MAX_HISTORY_SIZE) {
            searchQueries.removeLast();
        }

        // Save a copy to the repository; pass a new list to avoid issues if repository modifies it.
        historyRepository.save(new LinkedList<>(searchQueries));
    }

    @Override
    public List<String> getSearchHistory() {
        // Return an unmodifiable copy to prevent external modification
        return Collections.unmodifiableList(new LinkedList<>(searchQueries));
    }
}
