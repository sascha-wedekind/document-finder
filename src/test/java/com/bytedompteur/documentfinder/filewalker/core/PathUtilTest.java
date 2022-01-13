package com.bytedompteur.documentfinder.filewalker.core;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PathUtilTest {

  @Test
  void removeChildPaths_returnsAListContainingOnlyParentPaths() {
    // Arrange
    List<Path> pathList = Arrays.asList(
      Path.of("a/b/c"),
      Path.of("a/b/c/c1"),
      Path.of("a/b/c/c2"),
      Path.of("a/b/c/c2/c3"),
      Path.of("a/b/d"),
      Path.of("a/b/d/d1/d2")
    );
    Collections.shuffle(pathList);

    // Act
    Set<Path> result = PathUtil.removeChildPaths(pathList);

    // Assert
    assertThat(System.identityHashCode(result) == System.identityHashCode(pathList)).isFalse();
    assertThat(result).containsExactlyInAnyOrder(
      Path.of("a/b/c"),
      Path.of("a/b/d")
    );
  }

  @Test
  void removeChildPaths_returnsEmptyList_whenGivenListIsEmpty() {
    // Arrange
    List<Path> emptyList = List.of();

    // Act
    Set<Path> result = PathUtil.removeChildPaths(emptyList);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void removeChildPaths_returnsEmptyList_whenGivenListIsNull() {
    // Act
    Set<Path> result = PathUtil.removeChildPaths(null);

    // Assert
    assertThat(result).isEmpty();
  }
}
