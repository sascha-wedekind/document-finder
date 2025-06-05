package com.bytedompteur.documentfinder.searchhistory;

import com.bytedompteur.documentfinder.PathUtil; // Assuming PathUtil is in this package

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SearchHistoryManager {

    private static final int MAX_HISTORY_SIZE = 10;
    private static final String HISTORY_FILE_NAME = "search_history.json"; // Using .json as requested

    private List<String> searchQueries;
    private final Path historyFilePath;

    /**
     * Default constructor. Initializes search history from the application data folder.
     */
    public SearchHistoryManager() {
        this(PathUtil.getApplicationDataFolder().resolve(HISTORY_FILE_NAME));
    }

    /**
     * Constructor for testability, allowing a specific history file path.
     * @param historyFilePath The path to the history file.
     */
    public SearchHistoryManager(Path historyFilePath) {
        this.historyFilePath = historyFilePath;
        this.searchQueries = new LinkedList<>(); // Use LinkedList for efficient addFirst
        loadHistory();
    }

    /**
     * Loads search history from the history file.
     */
    private void loadHistory() {
        if (Files.exists(historyFilePath) && Files.isReadable(historyFilePath)) {
            try (BufferedReader reader = Files.newBufferedReader(historyFilePath, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        if (searchQueries.size() < MAX_HISTORY_SIZE) {
                            searchQueries.add(line.trim());
                        } else {
                            // If file somehow has more than MAX_HISTORY_SIZE, stop loading.
                            // Or, alternatively, load all and then trim.
                            // Current behavior: load only up to MAX_HISTORY_SIZE.
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading search history: " + e.getMessage());
                // Optionally, clear history if loading fails to prevent using corrupted data
                // searchQueries.clear();
            }
        }
    }

    /**
     * Saves the current search history to the history file.
     */
    private void saveHistory() {
        try {
            Files.createDirectories(historyFilePath.getParent()); // Ensure parent directory exists
            try (BufferedWriter writer = Files.newBufferedWriter(historyFilePath, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                for (String query : searchQueries) {
                    writer.write(query);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving search history: " + e.getMessage());
        }
    }

    /**
     * Adds a new search query to the history.
     * If the query already exists, it is moved to the top.
     * If the history exceeds MAX_HISTORY_SIZE, the oldest query is removed.
     * @param query The search query to add.
     */
    public void addSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }
        String trimmedQuery = query.trim();

        // Remove if exists to move to top later
        searchQueries.remove(trimmedQuery);

        // Add to the beginning (most recent)
        searchQueries.add(0, trimmedQuery); // LinkedList's add(0, element) is efficient

        // Enforce max history size
        while (searchQueries.size() > MAX_HISTORY_SIZE) {
            searchQueries.remove(searchQueries.size() - 1); // Remove the oldest
        }
        saveHistory();
    }

    /**
     * Returns an unmodifiable view of the search history.
     * @return A list of search queries.
     */
    public List<String> getSearchHistory() {
        return Collections.unmodifiableList(searchQueries);
    }

    /**
     * Returns the path to the history file.
     * @return The history file path.
     */
    public Path getHistoryFilePath() {
        return historyFilePath;
    }
}
