package com.bytedompteur.documentfinder.searchhistory.adapter.out;

import com.bytedompteur.documentfinder.PathUtil; // Assuming PathUtil is accessible
import com.bytedompteur.documentfinder.searchhistory.core.SearchHistoryRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections; // Not strictly needed here but often useful
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton // Dagger scope annotation
public class SearchHistoryFileRepository implements SearchHistoryRepository {

    private static final String HISTORY_FILE_NAME = "search_history.json"; // Using .json as per original design
    private final Path historyFilePath;

    @Inject
    public SearchHistoryFileRepository() {
        // Determine application data folder using PathUtil.
        // PathUtil is expected to be in com.bytedompteur.documentfinder.PathUtil
        this.historyFilePath = PathUtil.getApplicationDataFolder().resolve(HISTORY_FILE_NAME);
    }

    /**
     * Constructor for testing, allowing a specific history file path.
     * This constructor would not typically be managed by Dagger for production use.
     * @param historyFilePath The path to the history file.
     */
    public SearchHistoryFileRepository(Path historyFilePath) {
        this.historyFilePath = historyFilePath;
    }

    @Override
    public void save(List<String> queries) {
        try {
            // Ensure parent directory exists
            Path parentDir = historyFilePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            // Write the list of queries, one per line.
            // Using CREATE, TRUNCATE_EXISTING, WRITE to overwrite the file if it exists or create it.
            Files.write(historyFilePath, queries, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            // Basic error handling. In a real application, use a logging framework.
            System.err.println("Error saving search history to " + historyFilePath + ": " + e.getMessage());
            // Depending on the application's requirements, might rethrow as a custom runtime exception.
        }
    }

    @Override
    public List<String> load() {
        if (!Files.exists(historyFilePath) || !Files.isReadable(historyFilePath)) {
            // If file doesn't exist or isn't readable, return an empty list.
            return new LinkedList<>();
        }
        try {
            // Read all lines, filter out empty/blank ones, and collect into a LinkedList.
            return Files.lines(historyFilePath, StandardCharsets.UTF_8)
                        .map(String::trim) // Trim lines to handle potential leading/trailing whitespace
                        .filter(line -> !line.isEmpty()) // Ignore empty lines after trimming
                        .collect(Collectors.toCollection(LinkedList::new));
        } catch (IOException e) {
            // Basic error handling. In a real application, use a logging framework.
            System.err.println("Error loading search history from " + historyFilePath + ": " + e.getMessage());
            // Return an empty list in case of error to prevent application crash.
            // Consider clearing a potentially corrupted file or other recovery mechanisms.
            return new LinkedList<>();
        }
    }
}
