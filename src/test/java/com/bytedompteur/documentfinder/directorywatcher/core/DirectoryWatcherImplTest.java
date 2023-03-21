package com.bytedompteur.documentfinder.directorywatcher.core;

import com.bytedompteur.documentfinder.directorywatcher.adapter.out.FilesAdapter;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirectoryWatcherImplTest {

  @Mock
  WatchServicePollHandler mockedPollHandler;

  @Mock
  FilesAdapter mockedFilesAdapter;

  @Mock
  WatchService mockedWatchService;

  private DirectoryWatcherImpl sut;

  @BeforeEach
  void setUp() throws IOException {
    sut = new DirectoryWatcherImpl(
      Executors.newFixedThreadPool(1),
      mockedPollHandler,
      mockedFilesAdapter
    );

    Mockito.when(mockedFilesAdapter.createWatchService()).thenReturn(mockedWatchService);
    startWatcherWaitUntilStarted();
  }

  @AfterEach
  void tearDown() {
    stopWatcherWaitUntilStopped();
  }

  @Test
  void watchIncludingSubdirectories_registersWatchServiceForAllEventsOnGivenPath_whenNotAlreadyRegistered() throws IOException {
    // Arrange
    Path mockedPath = mock(Path.class);
    WatchKey mockedWatchKey = mock(WatchKey.class);
    Mockito
      .when(
        mockedPath.register(
          mockedWatchService,
          StandardWatchEventKinds.ENTRY_CREATE,
          StandardWatchEventKinds.ENTRY_DELETE,
          StandardWatchEventKinds.ENTRY_MODIFY
        )
      )
      .thenReturn(mockedWatchKey);

    // Act
    sut.watchIncludingSubdirectories(mockedPath);
    callPreVisitDirectoryOnFileVisitor(mockedPath);

    // Assert
    verify(mockedPath).register(
      eq(mockedWatchService),
      eq(StandardWatchEventKinds.ENTRY_CREATE),
      eq(StandardWatchEventKinds.ENTRY_DELETE),
      eq(StandardWatchEventKinds.ENTRY_MODIFY)
    );
  }

  @Test
  void watchIncludingSubdirectories_reRegistersPathAlreadyRegistered_whenWatchIsRestarted() throws IOException {
    // Arrange
    Path mockedPath = mock(Path.class);
    WatchKey mockedWatchKey = mock(WatchKey.class);
    Mockito
      .when(
        mockedPath.register(
          mockedWatchService,
          StandardWatchEventKinds.ENTRY_CREATE,
          StandardWatchEventKinds.ENTRY_DELETE,
          StandardWatchEventKinds.ENTRY_MODIFY
        )
      )
      .thenReturn(mockedWatchKey);
    sut.watchIncludingSubdirectories(mockedPath);
    callPreVisitDirectoryOnFileVisitor(mockedPath);

    // Act
    stopWatcherWaitUntilStopped();
    startWatcherWaitUntilStarted();
    sut.watchIncludingSubdirectories(mockedPath);
    callPreVisitDirectoryOnFileVisitor(mockedPath);

    // Assert
    verify(mockedPath, times(2)).register(
      eq(mockedWatchService),
      eq(StandardWatchEventKinds.ENTRY_CREATE),
      eq(StandardWatchEventKinds.ENTRY_DELETE),
      eq(StandardWatchEventKinds.ENTRY_MODIFY)
    );
  }


  @Test
  void unwatchIncludingSubdirectories_callsCancelOnWatchKey_whenPathIsWatched() throws IOException {
    // Arrange
    Path mockedPath = mock(Path.class);
    WatchKey mockedWatchKey = mock(WatchKey.class);
    Mockito
      .when(
        mockedPath.register(
          mockedWatchService,
          StandardWatchEventKinds.ENTRY_CREATE,
          StandardWatchEventKinds.ENTRY_DELETE,
          StandardWatchEventKinds.ENTRY_MODIFY
        )
      )
      .thenReturn(mockedWatchKey);
    sut.watchIncludingSubdirectories(mockedPath);
    callPreVisitDirectoryOnFileVisitor(mockedPath);

    // Act
    sut.unwatchIncludingSubdirectories(mockedPath);
    callPreVisitDirectoryOnFileVisitor(mockedPath);

    // Assert
    verify(mockedWatchKey).cancel();
  }

  @SuppressWarnings("unchecked")
  private void callPreVisitDirectoryOnFileVisitor(Path mockedPath) throws IOException {
    ArgumentCaptor<FileVisitor<Path>> argumentCaptor = ArgumentCaptor.forClass(FileVisitor.class);
    verify(mockedFilesAdapter, atLeastOnce()).walkFileTree(eq(mockedPath), argumentCaptor.capture());
    argumentCaptor.getValue().preVisitDirectory(mockedPath, null);
  }

  private void startWatcherWaitUntilStarted() {
    sut.startWatching();
    var policy = RetryPolicy
      .builder()
      .handleResult(false) // retry as long as watcher returns given result ('true')
      .withMaxRetries(20)
      .withDelay(Duration.ofMillis(100))
      .build();
    Failsafe
      .with(policy)
      .get(sut::isWatching);
  }

  private void stopWatcherWaitUntilStopped() {
    sut.stopWatching();
    var policy = RetryPolicy
      .builder()
      .handleResult(true) // retry as long as watcher returns given result ('false')
      .withMaxRetries(20)
      .withDelay(Duration.ofMillis(100))
      .build();
    Failsafe
      .with(policy)
      .get(sut::isWatching);
  }
}
