package com.bytedompteur.documentfinder.commands;

import com.bytedompteur.documentfinder.directorywatcher.adapter.in.DirectoryWatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartDirectoryWatcherCommandTest {

  @Mock
  DirectoryWatcher mockedDirectoryWatcher;

  private StartDirectoryWatcherCommand sut;

  @BeforeEach
  void setUp() {
    sut = new StartDirectoryWatcherCommand(mockedDirectoryWatcher);
  }

  @Test
  void run_doesNothing_whenWatcherIsAlreadyWatching() {
    // Arrange
    Mockito
      .when(mockedDirectoryWatcher.isWatching())
      .thenReturn(true);

    // Act
    sut.run();

    // Assert
    verify(mockedDirectoryWatcher, never()).startWatching();
  }

  @Test
  void run_startsWatcherAndWaitsUntilItIsStarted() {
    // Arrange
    when(mockedDirectoryWatcher.isWatching())
      .thenReturn(false)
      .thenReturn(false)
      .thenReturn(false)
      .thenReturn(true);

    // Act
    sut.run();

    // Assert
    verify(mockedDirectoryWatcher, times(4)).isWatching();
    verify(mockedDirectoryWatcher).startWatching();
  }
}
