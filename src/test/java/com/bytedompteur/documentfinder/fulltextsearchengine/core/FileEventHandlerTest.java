package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.out.FilesAdapter;
import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.out.PersistedUniqueFileEventQueueAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileEventHandlerTest {

  @Mock
  ExecutorService mockedExecutorService;

  @Mock
  IndexRepository mockedIndexRepository;

  @Mock
  PersistedUniqueFileEventQueueAdapter mockedAdapter;

  @Mock
  FilesAdapter mockedFilesAdapter;

  @InjectMocks
  FileEventHandler sut;

  @Test
  void isPathValid_returnsTrue_whenPathDoesNotExists() {
    // Arrange
    when(mockedFilesAdapter.notExists(any())).thenReturn(true);

    //Act
    var result = sut.isPathInvalid(Path.of("path"));

    //Assert
    assertThat(result).isTrue();
  }

  @Test
  void isPathValid_returnsFalse_whenPathExists() {
    // Arrange
    when(mockedFilesAdapter.notExists(any())).thenReturn(false);

    //Act
    var result = sut.isPathInvalid(Path.of("path"));

    //Assert
    assertThat(result).isFalse();
  }

  @Test
  void isPathValid_returnsTrue_whenPathIsNotReadable() {
    // Arrange
    when(mockedFilesAdapter.isNotReadable(any())).thenReturn(true);

    //Act
    var result = sut.isPathInvalid(Path.of("path"));

    //Assert
    assertThat(result).isTrue();
  }

  @Test
  void isPathValid_returnsFalse_whenPathIsReadable() {
    // Arrange
    when(mockedFilesAdapter.isNotReadable(any())).thenReturn(false);

    //Act
    var result = sut.isPathInvalid(Path.of("path"));

    //Assert
    assertThat(result).isFalse();
  }

  @Test
  void isPathValid_returnsTrue_whenPathEmpty() {
    // Arrange
    when(mockedFilesAdapter.isEmpty(any())).thenReturn(true);

    //Act
    var result = sut.isPathInvalid(Path.of("path"));

    //Assert
    assertThat(result).isTrue();
  }

  @Test
  void isPathValid_returnsFalse_whenPathIsNotEmpty() {
    // Arrange
    when(mockedFilesAdapter.isEmpty(any())).thenReturn(false);

    //Act
    var result = sut.isPathInvalid(Path.of("path"));

    //Assert
    assertThat(result).isFalse();
  }
}
