package com.bytedompteur.documentfinder.persistedqueue.core;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent;
import com.bytedompteur.documentfinder.persistedqueue.adapter.out.FilesReadWriteAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QueueRepositoryImplTest {

  private static final String TEST_APPLICATION_HOME_DIR = "TEST_APPLICATION_HOME_DIR";

  @Mock
  PersistedQueueItemCompactor mockedCompactor;

  @Mock
  FilesReadWriteAdapter mockedFilesReadWriteAdapter;

  @Mock
  BufferedWriter mockedBufferedWriter;

  private QueueRepositoryImpl sut;

  @BeforeEach
  void setUp() {
    sut = new QueueRepositoryImpl(
      mockedCompactor,
      TEST_APPLICATION_HOME_DIR,
      mockedFilesReadWriteAdapter,
      Clock.systemDefaultZone()
    );
  }

  @Test
  void save_initsBufferedWriterIfWriterDoesNotExist() throws IOException {
    // Arrange
    Mockito
      .when(mockedFilesReadWriteAdapter.newBufferedWriter(any(), any()))
      .thenReturn(mockedBufferedWriter);
    FileEvent event = new FileEvent(FileEvent.Type.CREATE, Path.of("/a/b/c"));

    // Act
    sut.save(event, QueueModificationType.ADDED);

    // Assert
    var expectedPath = Path.of(TEST_APPLICATION_HOME_DIR, QueueRepositoryImpl.REPOSITORY_FILE_NAME);
    var expectedOptions = new OpenOption[]{
      StandardOpenOption.WRITE,
      StandardOpenOption.APPEND,
      StandardOpenOption.CREATE
    };
    verify(mockedFilesReadWriteAdapter).newBufferedWriter(expectedPath, expectedOptions);
  }

  @Test
  void sut_writerWillBeClosed_whenTryWithClauseIsTriggered() throws IOException {
    // Arrange
    Mockito
      .when(mockedFilesReadWriteAdapter.newBufferedWriter(any(), any()))
      .thenReturn(mockedBufferedWriter);
    Mockito
      .doThrow(new IOException("TEST_EXCEPTION"))
      .when(mockedBufferedWriter)
      .write(anyString());
    FileEvent event = new FileEvent(FileEvent.Type.CREATE, Path.of("/a/b/c"));

    // Act
    Exception exception = null;
    try (QueueRepositoryImpl r = sut) {
      r.save(event, QueueModificationType.ADDED);
    } catch (Exception e) {
      exception = e;
    }

    // Assert
    assertThat(exception).isInstanceOf(IOException.class);
    verify(mockedBufferedWriter).close();
  }

  @Test
  void readCompactedQueueLog_deletesTheLogFileAfterReading() throws IOException {
    // Act
    var result = sut.readCompactedQueueLog();

    // Assert
    var expectedPath = Path.of(TEST_APPLICATION_HOME_DIR, QueueRepositoryImpl.REPOSITORY_FILE_NAME);
    verify(mockedFilesReadWriteAdapter).deleteIfExists(expectedPath);
  }

  @Test
  void readCompactedQueueLog_returnsListOfPersistedQueueItems() throws IOException {
    // Arrange
    var persistedQueueItems = List.of(
      new PersistedQueueItem(Clock.systemUTC().millis(), QueueModificationType.ADDED, FileEvent.Type.CREATE, "/a/b/c"),
      new PersistedQueueItem(Clock.systemUTC().millis(), QueueModificationType.REMOVED, FileEvent.Type.UPDATE, "/d/e/f")
    );
    Mockito
      .when(mockedFilesReadWriteAdapter.readAllLines(any()))
      .thenReturn(
        persistedQueueItems
          .stream()
          .map(PersistedQueueItem::toFileLine)
          .map(Optional::get)
          .toList()
      );
    Mockito
      .when(mockedCompactor.compact(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    var result = sut.readCompactedQueueLog();

    // Assert
    var expectedEvents = new FileEvent[]{
      new FileEvent(FileEvent.Type.CREATE, Path.of("/a/b/c")),
      new FileEvent(FileEvent.Type.UPDATE, Path.of("/d/e/f"))
    };
    assertThat(result).containsExactlyInAnyOrder(expectedEvents);
  }
}
