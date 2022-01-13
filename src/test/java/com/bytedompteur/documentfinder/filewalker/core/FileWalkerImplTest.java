package com.bytedompteur.documentfinder.filewalker.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import com.bytedompteur.documentfinder.filewalker.adapter.in.FileWalkerAlreadyRunningException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Sinks.Many;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class FileWalkerImplTest {

  @Mock
  WalkerFactory mockedWalkerFactory;

  FileWalkerImpl sut;

  @BeforeEach
  void setUp() {
    sut = new FileWalkerImpl(mockedWalkerFactory, Executors.newFixedThreadPool(1));
  }

  @Test
  void findFilesWithEndings_emitsPathsInFluxThatAreProvidedByWalkerRunnable() {
    // Arrange
    findFilesWithEndings_emitsPathsInFluxThatAreProvidedByWalkerRunnable_arrange();

    // Act
    var firstStep = StepVerifier
      .create(sut.findFilesWithEndings(Set.of("ingore"), Set.of(Path.of("ingore"))));

    // Assert
    assertThat(sut.isRunning()).isTrue();
    firstStep
      .expectNext(Path.of("p1"))
      .expectNext(Path.of("p2"))
      .expectNext(Path.of("p3"))
      .expectComplete()
      .verify();
    assertThat(sut.isRunning()).isFalse();
  }

  @Test
  void findFilesWithEndings_emitsOnlyComplete_whenWalkerRunnableAddsNoPathToFlux() {
    // Arrange
    findFilesWithEndings_emitsOnlyComplete_whenWalkerRunnableAddsNoPathToFlux_arrange();

    // Act
    var firstStep = StepVerifier
      .create(sut.findFilesWithEndings(Set.of("ingore"), Set.of(Path.of("ingore"))));

    // Assert
    assertThat(sut.isRunning()).isTrue();
    firstStep
      .expectComplete()
      .verify();
    assertThat(sut.isRunning()).isFalse();
  }

  @Test
  void findFilesWithEndings_throwsFileWalkerAlreadyRunningException() {
    // Arrange
    // Implementation detail: Because flux.onComplete() won't be called, FileWalker stays in the running state forever.
    Mockito
      .when(mockedWalkerFactory.create(any(), any(), any())).thenReturn(mock(WalkerRunnable.class));
    sut.findFilesWithEndings(Set.of("ingore"), Set.of(Path.of("ingore")));

    // Act
    var throwable = catchThrowable(
      () -> sut.findFilesWithEndings(Set.of("ingore"), Set.of(Path.of("ingore")))
    );

    // Assert
    assertThat(sut.isRunning()).isTrue();
    assertThat(throwable).isInstanceOf(FileWalkerAlreadyRunningException.class);
  }

  private void findFilesWithEndings_emitsOnlyComplete_whenWalkerRunnableAddsNoPathToFlux_arrange() {
    var sinkRef = new AtomicReference<Many<Path>>();
    var mockedWalkerRunnable = mock(WalkerRunnable.class);

    // Mocked WalkerFactory
    Mockito
      .when(mockedWalkerFactory.create(any(), any(), any()))
      .thenAnswer(invocation -> {
        //noinspection unchecked
        sinkRef.set((Many<Path>) invocation.getArgument(0, Many.class));
        return mockedWalkerRunnable;
      });

    // Mocked WalkerRunnable
    Mockito
      .doAnswer(invocation -> {
        sinkRef.get().tryEmitComplete();
        return Void.TYPE;
      })
      .when(mockedWalkerRunnable).run();
  }

  private void findFilesWithEndings_emitsPathsInFluxThatAreProvidedByWalkerRunnable_arrange() {
    var paths = List.of(
      Path.of("p1"),
      Path.of("p2"),
      Path.of("p3")
    );
    var sinkRef = new AtomicReference<Many<Path>>();
    var mockedWalkerRunnable = mock(WalkerRunnable.class);

    // Mocked WalkerFactory
    Mockito
      .when(mockedWalkerFactory.create(any(), any(), any()))
      .thenAnswer(invocation -> {
        //noinspection unchecked
        sinkRef.set((Many<Path>) invocation.getArgument(0, Many.class));
        return mockedWalkerRunnable;
      });

    // Mocked WalkerRunnable
    Mockito
      .doAnswer(invocation -> {
        paths.forEach(it -> sinkRef.get().tryEmitNext(it));
        sinkRef.get().tryEmitComplete();
        return Void.TYPE;
      })
      .when(mockedWalkerRunnable).run();
  }
}
