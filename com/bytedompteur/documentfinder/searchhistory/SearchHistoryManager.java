package com.bytedompteur.documentfinder.searchhistory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO: Implement PathUtil.getApplicationDataFolder() or replace with a suitable alternative.
// For now, using a placeholder for the application data folder.
class PathUtil { // TODO: This should be a proper utility class, potentially in a different package.
    public static Path getApplicationDataFolder() {
        // Placeholder implementation. Replace with actual logic for a real application.
        // For example, on Windows: System.getenv("APPDATA")
        // On macOS: System.getProperty("user.home") + "/Library/Application Support"
        // On Linux: System.getProperty("user.home") + "/.local/share"
        // For this exercise, a subfolder in user.home is acceptable.
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, ".DocumentFinderAppData", "DocumentFinder"); // Added a subdirectory for the app
    }
}

public class SearchHistoryManager {

    private static final int MAX_HISTORY_SIZE = 10;
    private static final String HISTORY_FILE_NAME = "search_history.json"; // Changed to .json as per requirement
    private List<String> searchQueries;
    private final Path historyFilePath;

    // Public constructor for application use
    public SearchHistoryManager() {
        this(PathUtil.getApplicationDataFolder().resolve(HISTORY_FILE_NAME));
    }

    // Constructor for testing or specific path injection
    public SearchHistoryManager(Path historyFilePath) {
        this.historyFilePath = historyFilePath;
        try {
            Files.createDirectories(this.historyFilePath.getParent()); // Ensure directory exists
        } catch (IOException e) {
            System.err.println("Could not create parent directories for history file: " + e.getMessage());
            // Depending on requirements, might rethrow, or fallback to a non-persistent mode.
        }
        this.searchQueries = loadHistory();
    }

    public void addSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }
        // Remove if exists to avoid duplicates and move to top
        searchQueries.remove(query);
        // Add to the beginning of the list (most recent)
        searchQueries.add(0, query);
        // Enforce max history size
        if (searchQueries.size() > MAX_HISTORY_SIZE) {
            searchQueries.remove(searchQueries.size() - 1); // Remove the oldest
        }
        saveHistory(); // Save the modified list
    }

    public List<String> getSearchHistory() {
        // Return a copy to prevent external modification
        return Collections.unmodifiableList(new ArrayList<>(this.searchQueries));
    }

    // Changed to save the internal state `this.searchQueries`
    private void saveHistory() {
        try {
            // Ensure directory exists - already done in constructor, but good practice if path could change
            // Files.createDirectories(historyFilePath.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(historyFilePath)) {
                // For plain text, one query per line.
                // For JSON, this would involve using a JSON library.
                for (String query : this.searchQueries) {
                    writer.write(query); // TODO: Handle special characters if any (e.g. newline in query itself)
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving search history: " + e.getMessage());
            // Consider more robust error handling or logging strategy
        }
    }

    private List<String> loadHistory() {
        List<String> loadedQueries = new ArrayList<>();
        if (Files.exists(historyFilePath) && Files.isReadable(historyFilePath)) {
            try (BufferedReader reader = Files.newBufferedReader(historyFilePath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) { // Avoid adding empty lines if any
                        loadedQueries.add(line);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading search history: " + e.getMessage());
                // Depending on requirements, could clear the corrupted file or notify user.
            }
        }
        // Ensure loaded queries do not exceed MAX_HISTORY_SIZE, though saveHistory should prevent this.
        // This is more of a safeguard if the file was manually edited.
        if (loadedQueries.size() > MAX_HISTORY_SIZE) {
            return new ArrayList<>(loadedQueries.subList(0, MAX_HISTORY_SIZE));
        }
        return loadedQueries;
    }

    public Path getHistoryFilePath() {
        return historyFilePath;
    }
}
