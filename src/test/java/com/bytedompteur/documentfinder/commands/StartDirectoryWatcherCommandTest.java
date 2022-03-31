package com.bytedompteur.documentfinder.commands;

import com.bytedompteur.documentfinder.directorywatcher.adapter.in.DirectoryWatcher;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
import com.bytedompteur.documentfinder.settings.adapter.in.Settings;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartDirectoryWatcherCommandTest {

  @Mock
  DirectoryWatcher mockedDirectoryWatcher;

  @Spy
  SettingsService mockedSettingsService;

  @Mock
  PersistedUniqueFileEventQueue mockedQueue;

  private StartDirectoryWatcherCommand sut;

  @BeforeEach
  void setUp() {
    sut = new StartDirectoryWatcherCommand(mockedDirectoryWatcher, mockedSettingsService, mockedQueue);
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
    when(mockedDirectoryWatcher.fileEvents())
      .thenReturn(Flux.empty());
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

  @Test
  void configureWatcherWithDirectoriesFromSettings_callWatcherWithAllFoldersFromSettings() throws IOException {
    // Arrange
    var settings = Settings
      .builder()
      .folders(List.of("/a/b", "/c/d"))
      .build();
    Mockito
      .when(mockedSettingsService.read())
      .thenReturn(Optional.of(settings));

    // Act
    sut.configureWatcherWithDirectoriesFromSettings();

    // Assert
    verify(mockedDirectoryWatcher).watchIncludingSubdirectories(Path.of("/a/b"));
    verify(mockedDirectoryWatcher).watchIncludingSubdirectories(Path.of("/c/d"));
  }

  @Test
  void configureWatcherWithDirectoriesFromSettings_continuesEvenWhenACallToWatchThrows() throws IOException {
    // Arrange
    var settings = Settings
      .builder()
      .folders(List.of("/a/b", "/c/d"))
      .build();
    Mockito
      .when(mockedSettingsService.read())
      .thenReturn(Optional.of(settings));
    Mockito
      .doThrow(new IOException("TEST EXCEPTION"))
      .when(mockedDirectoryWatcher)
      .watchIncludingSubdirectories(Path.of("/a/b"));

    // Act
    sut.configureWatcherWithDirectoriesFromSettings();

    // Assert
    verify(mockedDirectoryWatcher).watchIncludingSubdirectories(Path.of("/a/b"));
    verify(mockedDirectoryWatcher).watchIncludingSubdirectories(Path.of("/c/d"));
  }
}
