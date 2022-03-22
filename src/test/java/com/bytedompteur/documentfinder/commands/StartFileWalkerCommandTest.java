package com.bytedompteur.documentfinder.commands;

import com.bytedompteur.documentfinder.filewalker.adapter.in.FileWalker;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
import com.bytedompteur.documentfinder.settings.adapter.in.Settings;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartFileWalkerCommandTest {

  @Mock
  FileWalker mockedFileWalker;

  @SuppressWarnings("unused")
  @Mock
  PersistedUniqueFileEventQueue mockedQueue;

  @Mock
  SettingsService mockedSettingsService;

  @InjectMocks
  StartFileWalkerCommand sut;

  @Test
  void run_callsFindFilesWithEndings_onFileWalker_withSettingsFromSettingsService() {
    // Arrange
    Mockito
      .when(mockedFileWalker.isRunning())
      .thenReturn(false);

    var fileEndings = Set.of("pdf");
    var paths = Set.of(Path.of("/a/b/c"));
    Mockito
      .when(mockedSettingsService.read())
      .thenReturn(Optional.of(
        Settings
          .builder()
          .fileTypes(fileEndings.stream().toList())
          .folders(paths.stream().map(Path::toString).toList())
          .build()
      ));

    Mockito
      .when(mockedFileWalker.findFilesWithEndings(fileEndings, paths))
      .thenReturn(Flux.empty());

    // Act
    sut.run();

    // Assert
    verify(mockedFileWalker).findFilesWithEndings(fileEndings, paths);
  }

  @Test
  void run_startsWalkerAndWaitsUntilItIsStarted() {
    // Arrange
    Mockito
      .when(mockedFileWalker.isRunning())
      .thenReturn(false)
      .thenReturn(false)
      .thenReturn(false)
      .thenReturn(true);
    Mockito
      .when(mockedSettingsService.read())
      .thenReturn(Optional.of(Settings.builder().build()));
    Mockito
      .when(mockedFileWalker.findFilesWithEndings(anySet(), anySet()))
      .thenReturn(Flux.empty());

    // Act
    sut.run();

    // Assert
    verify(mockedFileWalker, times(4)).isRunning();
  }

  @Test
  void run_doesNothing_whenWalkerIsAlreadyRunning() {
    // Arrange
    Mockito
      .when(mockedFileWalker.isRunning())
      .thenReturn(true);

    // Act
    sut.run();

    // Assert
    verify(mockedFileWalker, never()).findFilesWithEndings(anySet(), anySet());
    verify(mockedSettingsService, never()).read();
  }
}
