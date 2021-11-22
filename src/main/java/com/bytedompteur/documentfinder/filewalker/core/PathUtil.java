package com.bytedompteur.documentfinder.filewalker.core;

import static java.util.Objects.nonNull;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
  static Set<Path> removeChildPaths(Collection<Path> list) {
    Set<Path> result;
    if (nonNull(list) && !list.isEmpty()) {
      List<Path> sortedByNumberOfPathElementsList = list.stream()
        .sorted(Comparator.comparingInt(Path::getNameCount))
        .collect(Collectors.toList());

      List<Path> copyOfSortedByNumberOfPathElementsList = new LinkedList<>(sortedByNumberOfPathElementsList);

      sortedByNumberOfPathElementsList.forEach(it -> {
        copyOfSortedByNumberOfPathElementsList.removeIf(next -> !next.equals(it) && next.startsWith(it));
      });
      result = new HashSet<>(copyOfSortedByNumberOfPathElementsList);
    } else {
      result = Set.of();
    }
    return result;
  }
}
