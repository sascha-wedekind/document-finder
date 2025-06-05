package com.bytedompteur.documentfinder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths; // Added for Paths.get()
import java.io.IOException; // Added for Files.createDirectories()
import java.util.*;

import static java.util.Objects.nonNull;

public class PathUtil {

  private static final String APPLICATION_NAME = "DocumentFinder";

  /**
   * Returns the application-specific data folder based on the operating system.
   * It creates the directory if it doesn't exist.
   *
   * @return The path to the application data folder.
   */
  public static Path getApplicationDataFolder() {
    String osName = System.getProperty("os.name").toLowerCase();
    Path appDataPath;

    if (osName.contains("win")) {
      String appDataEnv = System.getenv("APPDATA");
      if (appDataEnv != null && !appDataEnv.isEmpty()) {
        appDataPath = Paths.get(appDataEnv, APPLICATION_NAME);
      } else {
        // Fallback for Windows if APPDATA is not set
        appDataPath = Paths.get(System.getProperty("user.home"), "AppData", "Roaming", APPLICATION_NAME);
      }
    } else if (osName.contains("mac")) {
      appDataPath = Paths.get(System.getProperty("user.home"), "Library", "Application Support", APPLICATION_NAME);
    } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
      appDataPath = Paths.get(System.getProperty("user.home"), ".local", "share", APPLICATION_NAME);
    } else {
      // Generic fallback
      appDataPath = Paths.get(System.getProperty("user.home"), "." + APPLICATION_NAME);
    }

    try {
      Files.createDirectories(appDataPath);
    } catch (IOException e) {
      System.err.println("Failed to create application data directory: " + appDataPath + " - " + e.getMessage());
      // Depending on the application's needs, this might be a critical failure.
      // For now, we print an error and return the path, trusting the caller to handle usability.
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
