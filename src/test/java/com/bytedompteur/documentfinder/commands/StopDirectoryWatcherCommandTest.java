package com.bytedompteur.documentfinder.commands;

import com.bytedompteur.documentfinder.directorywatcher.adapter.in.DirectoryWatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StopDirectoryWatcherCommandTest {

  @Mock
  DirectoryWatcher mockedDirectoryWatcher;

  @InjectMocks
  StopDirectoryWatcherCommand sut;

  @Test
  void run_doesNothing_whenWatcherIsNotRunning() {
    // Arrange
    Mockito
      .when(mockedDirectoryWatcher.isWatching())
      .thenReturn(false);

    // Act
    sut.run();

    // Assert
    verify(mockedDirectoryWatcher, never()).stopWatching();
  }

  @Test
  void stopAndWait_triggersWatcherStopAndWaitsUntilWatcherHasStopped() {
    // Arrange
    Mockito
      .when(mockedDirectoryWatcher.isWatching())
      .thenReturn(true)
      .thenReturn(true)
      .thenReturn(false);

    // Act
    sut.stopAndWait();

    // Assert
    verify(mockedDirectoryWatcher, times(3)).isWatching();
    verify(mockedDirectoryWatcher).stopWatching();
  }
}
