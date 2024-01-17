package com.bytedompteur.documentfinder.persistedqueue.core;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
import com.bytedompteur.documentfinder.persistedqueue.adapter.out.SettingsServiceAdapter;
import com.bytedompteur.documentfinder.settings.adapter.in.Settings;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilteringFileEventQueueDelegateTest {

  @Mock
  PersistedUniqueFileEventQueue delegate;

  @Mock
  SettingsServiceAdapter settingsService;
  private FilteringFileEventQueueDelegate sut;

  @BeforeEach
  void setUp() {
    var settings = Settings
      .builder()
      .fileTypes(List.of(
        "CONFIGURED_FILE_TYPE"
      ))
      .build();
    when(settingsService.getSettings()).thenReturn(settings);
    sut = new FilteringFileEventQueueDelegate(delegate, settingsService);
  }

  @Test
  void pushOrOverwrite_doesntCallDelegate_whenParameterIsNull() {
    // Act
    sut.pushOrOverwrite((FileEvent) null);

    // Assert
    verify(delegate, never()).pushOrOverwrite((FileEvent) null);
  }

  @Test
  void pushOrOverwrite_doesntCallDelegate_whenPathOfParameterDoesntMatchAnyOfTheConfiguredFileTypes() {
    // Arrange
    var event = new FileEvent(FileEvent.Type.CREATE, Path.of("test.NOT_CONFIGURED_FILE_TYPE"));

    // Act
    sut.pushOrOverwrite(event);

    // Assert
    verify(delegate, never()).pushOrOverwrite(event);
  }

  @Test
  void pushOrOverwrite_callsDelegate_whenPathOfParameterMatchesAnyOfTheConfiguredFileTypes() {
    // Arrange
    var event = new FileEvent(FileEvent.Type.CREATE, Path.of("test.CONFIGURED_FILE_TYPE"));

    // Act
    sut.pushOrOverwrite(event);

    // Assert
    verify(delegate).pushOrOverwrite(event);
  }

  @Test
  void pushOrOverwrite_list_doesntCallDelegate_whenParameterIsNull() {
    // Act
    sut.pushOrOverwrite((List<FileEvent>) null);

    // Assert
    verify(delegate, never()).pushOrOverwrite((List<FileEvent>) null);
  }

  @Test
  void pushOrOverwrite_list_doesntCallDelegate_whenPathOfParameterDoesntMatchAnyOfTheConfiguredFileTypes() {
    // Arrange
    var events = List.of(new FileEvent(FileEvent.Type.CREATE, Path.of("test.NOT_CONFIGURED_FILE_TYPE")));

    // Act
    sut.pushOrOverwrite(events);

    // Assert
    verify(delegate, never()).pushOrOverwrite(events);
  }

  @Test
  void pushOrOverwrite_list_callsDelegate_whenPathOfParameterMatchesAnyOfTheConfiguredFileTypes() {
    // Arrange
    var events = List.of(new FileEvent(FileEvent.Type.CREATE, Path.of("test.CONFIGURED_FILE_TYPE")));

    // Act
    sut.pushOrOverwrite(events);

    // Assert
    verify(delegate).pushOrOverwrite(events);
  }
}