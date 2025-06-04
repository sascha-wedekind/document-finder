package com.bytedompteur.documentfinder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths; // Added for Paths.get()
import java.io.IOException; // Added for Files.createDirectories()
import java.util.*;

import static java.util.Objects.nonNull;

public class PathUtil {

  private static final String APPLICATION_NAME = "DocumentFinder";

  public static Path getApplicationDataFolder() {
    String osName = System.getProperty("os.name").toLowerCase();
    Path appDataPath;

    if (osName.contains("win")) {
      String appData = System.getenv("APPDATA");
      if (appData != null && !appData.isEmpty()) {
        appDataPath = Paths.get(appData, APPLICATION_NAME);
      } else {
        // Fallback if APPDATA is not set (unlikely for modern Windows)
        appDataPath = Paths.get(System.getProperty("user.home"), "AppData", "Roaming", APPLICATION_NAME);
      }
    } else if (osName.contains("mac")) {
      appDataPath = Paths.get(System.getProperty("user.home"), "Library", "Application Support", APPLICATION_NAME);
    } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
      // Using .local/share as a common standard for user-specific application data
      appDataPath = Paths.get(System.getProperty("user.home"), ".local", "share", APPLICATION_NAME);
    } else {
      // Fallback for other OSes (e.g., Solaris) or if detection fails
      appDataPath = Paths.get(System.getProperty("user.home"), "." + APPLICATION_NAME);
    }

    try {
      Files.createDirectories(appDataPath);
    } catch (IOException e) {
      // Handle the exception, e.g., log it or throw a runtime exception
      // For now, print to stderr, but a more robust solution would be better
      System.err.println("Failed to create application data directory: " + appDataPath + " - " + e.getMessage());
      // Depending on the application's needs, it might be critical to stop,
      // or it might be possible to continue (e.g., by using a temporary directory or in-memory storage).
      // For this example, we'll proceed, and the application might fail later if the path is unusable.
    }
    return appDataPath;
  }

  /**
   * Given a list of
   * <pre>
   *   [
   *    'a/b/c',
   *    'a/b/c/c1',
   *    'a/b/c/c2',
   *    'a/b/c/c2/c3',
   *    'a/b/d',
   *    'a/b/d/d1/d2'
   *   ]
   * </pre>
   * returns
   * <pre>
   *   [
   *    'a/b/c',
   *    'a/b/d'
   *   ]
   * </pre>
   */
  public Set<Path> removeChildPaths(Collection<Path> list) {
    Set<Path> result;
    if (nonNull(list) && !list.isEmpty()) {
      List<Path> sortedByNumberOfPathElementsList = list.stream()
        .sorted(Comparator.comparingInt(Path::getNameCount))
        .toList();

      List<Path> copyOfSortedByNumberOfPathElementsList = new LinkedList<>(sortedByNumberOfPathElementsList);

      sortedByNumberOfPathElementsList.forEach(it ->
        copyOfSortedByNumberOfPathElementsList.removeIf(next -> !next.equals(it) && next.startsWith(it))
      );
      result = new HashSet<>(copyOfSortedByNumberOfPathElementsList);
    } else {
      result = Set.of();
    }
    return result;
  }

  /**
   * Tests if the give  string represents an existing directory in the system.
   *
   * @return true if the given string is na existing directory, otherwise false.
   */
  public boolean isDirectory(String s) {
    return Optional
      .ofNullable(s)
      .filter(it -> !it.isBlank())
      .map(Path::of)
      .filter(Files::isDirectory)
      .isPresent();
  }
}
