package com.bytedompteur.documentfinder.filewalker.core;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class FileEndingMatcherTest {

  @Test
  void constructor_throwsIllegalArgumentException_whenParameterIsNull() {
    // Act
    Throwable throwable = catchThrowable(() -> new FileEndingMatcher(null));

    // Assert
    assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void constructor_throwsIllegalArgumentException_whenParameterIsEmpty() {
    // Act
    Throwable throwable = catchThrowable(() -> new FileEndingMatcher(Set.of()));

    // Assert
    assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void matches_returnsTrue_whenGivenPathEndsWithConfiguredFileEnding() {
    // Arrange
    Set<String> fileNameEndings = Set.of("pdf");
    FileEndingMatcher sut = new FileEndingMatcher(fileNameEndings);
    Path path = Path.of("/a/b/someFile.PdF");

    // Act
    boolean result = sut.matches(path);

    // Assert
    assertThat(result).isTrue();
  }

  @Test
  void matches_returnsFalse_whenGivenPathDoesNotEndWithConfiguredFileEnding() {
    // Arrange
    Set<String> fileNameEndings = Set.of("pdf");
    FileEndingMatcher sut = new FileEndingMatcher(fileNameEndings);
    Path path = Path.of("/a/b/someFile.txt");

    // Act
    boolean result = sut.matches(path);

    // Assert
    assertThat(result).isFalse();
  }
}
