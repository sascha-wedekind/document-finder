package com.bytedompteur.documentfinder.filewalker.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Sinks.Many;

@ExtendWith(MockitoExtension.class)
class WalkerRunnableTest {

  @Mock
  FileEndingMatcher mockedFileEndingMatcher;

  @Mock
  WalkFileTreeAdapter mockedWalkFileTreeAdapter;

  @Mock
  Many<Path> mockedSink;


  @Test
  void preVisitDirectory_returnsContinue_whenWalkerNotStopped() {
    // Arrange
    var sut = new WalkerRunnable(mockedSink, mockedFileEndingMatcher, Set.of(), mockedWalkFileTreeAdapter);

    // Act
    var result = sut.preVisitDirectory(Path.of(""), mock(BasicFileAttributes.class));

    // Assert
    assertThat(sut.isStopped()).isFalse();
    assertThat(result).isEqualTo(FileVisitResult.CONTINUE);
  }

  @Test
  void preVisitDirectory_returnsTerminate_whenWalkerStopped() {
    // Arrange
    var sut = new WalkerRunnable(mockedSink, mockedFileEndingMatcher, Set.of(), mockedWalkFileTreeAdapter);
    sut.stop();

    // Act
    var result = sut.preVisitDirectory(Path.of(""), mock(BasicFileAttributes.class));

    // Assert
    assertThat(sut.isStopped()).isTrue();
    assertThat(result).isEqualTo(FileVisitResult.TERMINATE);
  }

  @Test
  void visitFile_returnsContinue_whenWalkerNotStopped() {
    // Arrange
    when(mockedFileEndingMatcher.matches(any())).thenReturn(false);
    var sut = new WalkerRunnable(mockedSink, mockedFileEndingMatcher, Set.of(), mockedWalkFileTreeAdapter);

    // Act
    var result = sut.visitFile(Path.of(""), mock(BasicFileAttributes.class));

    // Assert
    assertThat(sut.isStopped()).isFalse();
    assertThat(result).isEqualTo(FileVisitResult.CONTINUE);
  }

  @Test
  void visitFile_returnsTerminate_whenWalkerStopped() {
    // Arrange
    when(mockedFileEndingMatcher.matches(any())).thenReturn(false);
    var sut = new WalkerRunnable(mockedSink, mockedFileEndingMatcher, Set.of(), mockedWalkFileTreeAdapter);
    sut.stop();

    // Act
    var result = sut.visitFile(Path.of(""), mock(BasicFileAttributes.class));

    // Assert
    assertThat(sut.isStopped()).isTrue();
    assertThat(result).isEqualTo(FileVisitResult.TERMINATE);
  }

  @Test
  void visitFile_doNotCallSink_whenFileDoesNotMatch() {
    // Arrange
    when(mockedFileEndingMatcher.matches(any())).thenReturn(false);
    var sut = new WalkerRunnable(mockedSink, mockedFileEndingMatcher, Set.of(), mockedWalkFileTreeAdapter);

    // Act
    sut.visitFile(Path.of(""), mock(BasicFileAttributes.class));

    // Assert
    verify(mockedSink, never()).tryEmitNext(any());
  }

  @Test
  void visitFile_callSink_whenFileMatches() {
    // Arrange
    when(mockedFileEndingMatcher.matches(any())).thenReturn(true);
    var sut = new WalkerRunnable(mockedSink, mockedFileEndingMatcher, Set.of(), mockedWalkFileTreeAdapter);
    var path = Path.of("someFile");

    // Act
    sut.visitFile(path, mock(BasicFileAttributes.class));

    // Assert
    verify(mockedSink).tryEmitNext(path);
  }

  @Test
  void run_callsAdapterWithAllPassedPaths_andSignalsCompleteAtTheEnd() throws IOException {
    // Arrange
    Set<Path> paths = Set.of(
      Path.of("path/p1"),
      Path.of("path/p2")
    );
    var sut = new WalkerRunnable(mockedSink, mockedFileEndingMatcher, paths, mockedWalkFileTreeAdapter);

    // Act
    sut.run();

    // Assert,
    verify(mockedWalkFileTreeAdapter).walkFileTree(Path.of("path/p1"), sut);
    verify(mockedWalkFileTreeAdapter).walkFileTree(Path.of("path/p2"), sut);
    verify(mockedSink).tryEmitComplete();
  }

  @Test
  void run_dontCallAdapterAndSignalsCompleteAtTheEnd_whenPathIsNull() throws IOException {
    // Arrange
    var sut = new WalkerRunnable(mockedSink, mockedFileEndingMatcher, null, mockedWalkFileTreeAdapter);

    // Act
    sut.run();

    // Assert,
    verify(mockedWalkFileTreeAdapter,never()).walkFileTree(any(), any());
    verify(mockedSink).tryEmitComplete();
  }

}
