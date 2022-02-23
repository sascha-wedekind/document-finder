package com.bytedompteur.documentfinder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.util.Objects.nonNull;

public class PathUtil {

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
