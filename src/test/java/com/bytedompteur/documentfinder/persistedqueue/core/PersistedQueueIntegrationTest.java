package com.bytedompteur.documentfinder.persistedqueue.core;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent;
import com.bytedompteur.documentfinder.persistedqueue.adapter.out.FilesReadWriteAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PersistedQueueIntegrationTest {

  private static final String TEST_APPLICATION_HOME_DIR = "TEST_APPLICATION_HOME_DIR";

  @Mock
  FilesReadWriteAdapter mockedFilesReadWriteAdapter;

  @Mock
  BufferedWriter mockedBufferedWriter;

  private PersistedUniqueFileEventQueueImpl sut;
  private QueueRepositoryImpl queueRepository;

  @BeforeEach
  void setUp() throws IOException {
    queueRepository = new QueueRepositoryImpl(
      new PersistedQueueItemCompactor(),
      TEST_APPLICATION_HOME_DIR,
      mockedFilesReadWriteAdapter,
      Clock.systemDefaultZone()
    );
    
    // When reading from file, return empty initially
    lenient().when(mockedFilesReadWriteAdapter.readAllLines(any())).thenReturn(List.of());

    sut = new PersistedUniqueFileEventQueueImpl(queueRepository);
    
    // Clear invocations that happened during constructor
    Mockito.clearInvocations(mockedFilesReadWriteAdapter);
  }

  @Test
  void queueLogFile_isDeleted_whenQueueBecomesEmptyViaPop() throws IOException {
    // Arrange
    doReturn(mockedBufferedWriter).when(mockedFilesReadWriteAdapter).newBufferedWriter(any(Path.class), any(OpenOption.class), any(OpenOption.class), any(OpenOption.class));
    FileEvent event = new FileEvent(FileEvent.Type.CREATE, Path.of("/a/b/c"));
    sut.pushOrOverwrite(event);
    
    // Act
    sut.pop();

    // Assert
    var logFilePath = Path.of(TEST_APPLICATION_HOME_DIR, QueueRepositoryImpl.REPOSITORY_FILE_NAME);
    // This confirms that it IS now deleted when empty
    verify(mockedFilesReadWriteAdapter, atLeastOnce()).deleteIfExists(logFilePath);
  }

  @Test
  void queueLogFile_isDeleted_whenQueueIsCleared() throws IOException {
    // Arrange
    doReturn(mockedBufferedWriter).when(mockedFilesReadWriteAdapter).newBufferedWriter(any(Path.class), any(OpenOption.class), any(OpenOption.class), any(OpenOption.class));
    FileEvent event = new FileEvent(FileEvent.Type.CREATE, Path.of("/a/b/c"));
    sut.pushOrOverwrite(event);
    
    // Act
    sut.clear();

    // Assert
    var logFilePath = Path.of(TEST_APPLICATION_HOME_DIR, QueueRepositoryImpl.REPOSITORY_FILE_NAME);
    verify(mockedFilesReadWriteAdapter, atLeastOnce()).deleteIfExists(logFilePath);
  }
}
