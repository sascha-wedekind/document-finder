package com.bytedompteur.documentfinder.directorywatcher.core;

import com.bytedompteur.documentfinder.directorywatcher.adapter.in.DirectoryWatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bytedompteur.documentfinder.directorywatcher.core.WatchEventBuilder.eventBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WatchServicePollHandlerTest {

  private Map<WatchKey, Path> pathByWatchKey;

  private WatchServicePollHandler sut;

  @SuppressWarnings("unused")
  @Mock
  DirectoryWatcher mockedDirectoryWatcher;

  @BeforeEach
  void setUp() {
    pathByWatchKey = new HashMap<>();
    sut = new WatchServicePollHandler(mockedDirectoryWatcher);
  }

  @Test
  void deduplicateAndOrderFileEvents_ordersEventsByKind() {
    // Arrange
    List<AbsolutePathWatchEvent> events = List.of(
      eventBuilder()
        .path(Paths.get("/dir_2"))
        .kind(StandardWatchEventKinds.ENTRY_DELETE)
        .buildFileEvent(),
      eventBuilder()
        .path(Paths.get("/dir_1"))
        .kind(StandardWatchEventKinds.ENTRY_CREATE)
        .buildFileEvent()
    );

    // Act
    List<AbsolutePathWatchEvent> result = sut.deduplicateAndOrderFileEvents(events);

    // Assert
    assertThat(result).containsExactly(
      eventBuilder()
        .path(Paths.get("/dir_1"))
        .kind(StandardWatchEventKinds.ENTRY_CREATE)
        .buildFileEvent(),
      eventBuilder()
        .path(Paths.get("/dir_2"))
        .kind(StandardWatchEventKinds.ENTRY_DELETE)
        .buildFileEvent()
    );
  }

  @Test
  void deduplicateAndOrderFileEvents_deduplicatesEvents() {
    // Arrange
    List<AbsolutePathWatchEvent> events = List.of(
      eventBuilder()
        .path(Paths.get("/dirAll"))
        .kind(StandardWatchEventKinds.ENTRY_DELETE)
        .buildFileEvent(),
      eventBuilder()
        .path(Paths.get("/dirAll"))
        .kind(StandardWatchEventKinds.ENTRY_CREATE)
        .buildFileEvent(),
      eventBuilder()
        .path(Paths.get("/dirAll"))
        .kind(StandardWatchEventKinds.ENTRY_MODIFY)
        .buildFileEvent(),
      eventBuilder()
        .path(Paths.get("/dirCreateModify"))
        .kind(StandardWatchEventKinds.ENTRY_CREATE)
        .buildFileEvent(),
      eventBuilder()
        .path(Paths.get("/dirCreateModify"))
        .kind(StandardWatchEventKinds.ENTRY_MODIFY)
        .buildFileEvent(),
      eventBuilder()
        .path(Paths.get("/dirCreateDelete"))
        .kind(StandardWatchEventKinds.ENTRY_CREATE)
        .buildFileEvent(),
      eventBuilder()
        .path(Paths.get("/dirCreateDelete"))
        .kind(StandardWatchEventKinds.ENTRY_DELETE)
        .buildFileEvent(),
      eventBuilder()
        .path(Paths.get("/dirModifyDelete"))
        .kind(StandardWatchEventKinds.ENTRY_MODIFY)
        .buildFileEvent(),
      eventBuilder()
        .path(Paths.get("/dirModifyDelete"))
        .kind(StandardWatchEventKinds.ENTRY_DELETE)
        .buildFileEvent()
    );

    // Act
    List<AbsolutePathWatchEvent> result = sut.deduplicateAndOrderFileEvents(events);

    // Assert
    assertThat(result)
      .hasSize(4)
      .contains(
        eventBuilder()
          .path(Paths.get("/dirAll"))
          .kind(StandardWatchEventKinds.ENTRY_DELETE)
          .buildFileEvent(),
        eventBuilder()
          .path(Paths.get("/dirCreateModify"))
          .kind(StandardWatchEventKinds.ENTRY_MODIFY)
          .buildFileEvent(),
        eventBuilder()
          .path(Paths.get("/dirCreateDelete"))
          .kind(StandardWatchEventKinds.ENTRY_DELETE)
          .buildFileEvent(),
        eventBuilder()
          .path(Paths.get("/dirModifyDelete"))
          .kind(StandardWatchEventKinds.ENTRY_DELETE)
          .buildFileEvent()
      );
  }

  @Test
  void notifyWatcherAboutDirectoryEvents_shouldRegisterNewDirectoriesForWatchingOrRemoveDeletedDirectoriesFromWatcher() throws IOException {
    // Arrange
    List<AbsolutePathWatchEvent> events = List.of(
      eventBuilder().path(Paths.get("/created")).kind(StandardWatchEventKinds.ENTRY_CREATE).buildDirectoryEvent(),
      eventBuilder().path(Paths.get("/deleted")).kind(StandardWatchEventKinds.ENTRY_DELETE).buildDirectoryEvent()
    );

    // Act
    sut.notifyWatcherAboutDirectoryEvents(events);

    // Assert
    verify(mockedDirectoryWatcher).watchIncludingSubdirectories(Paths.get("/created"));
    verify(mockedDirectoryWatcher).unwatchIncludingSubdirectories(Paths.get("/deleted"));
  }

  @Test
  void mapToAbsolutePathEvents_joinsFilePathWithBasePath() {
    // Arrange
    WatchKeyForTests watchKeyBasePath = WatchKeyForTests
      .builder()
      .event(WatchEventBuilder
        .eventBuilder()
        .path(Paths.get("fileA.txt"))
        .kind(StandardWatchEventKinds.ENTRY_CREATE)
        .buildFileEvent()
      )
      .build();
    WatchKeyForTests watchKeyAnotherBasePath = WatchKeyForTests
      .builder()
      .event(WatchEventBuilder
        .eventBuilder()
        .path(Paths.get("fileB.txt"))
        .kind(StandardWatchEventKinds.ENTRY_CREATE)
        .buildFileEvent()
      )
      .build();
    Path basePath = Paths.get("/a/base/path");
    Path anotherBasePath = Paths.get("/another/base/path");
    pathByWatchKey.put(watchKeyBasePath, basePath);
    pathByWatchKey.put(watchKeyAnotherBasePath, anotherBasePath);

    when(mockedDirectoryWatcher.getPathByWatchKey()).thenReturn(pathByWatchKey);

    // Act
    List<AbsolutePathWatchEvent> events1 = sut.mapToAbsolutePathEvents(watchKeyBasePath);
    List<AbsolutePathWatchEvent> events2 = sut.mapToAbsolutePathEvents(watchKeyAnotherBasePath);

    // Assert
    assertThat(events1)
      .map(AbsolutePathWatchEvent::context)
      .containsExactly(Path.of("\\a\\base\\path\\fileA.txt"));
    assertThat(events2)
      .map(AbsolutePathWatchEvent::context)
      .containsExactly(Path.of("\\another\\base\\path\\fileB.txt"));
  }
}
