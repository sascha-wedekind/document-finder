package com.bytedompteur.documentfinder.commands;

import com.bytedompteur.documentfinder.filewalker.adapter.in.FileWalker;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class StopFileWalkerCommandTest {

  @Mock
  FileWalker mockedFileWalker;

  @InjectMocks
  StopFileWalkerCommand sut;

  @Test
  void run_doesNothing_whenFileWalkerIsNotRunning() {
    // Arrange
    Mockito
      .when(mockedFileWalker.isRunning())
      .thenReturn(false);

    // Act
    sut.run();

    // Assert
    verify(mockedFileWalker, never()).stop();
  }

  @Test
  void stopAndWait_triggersWalkerStopAndWaitsUntilWalkerHasStopped() {
    // Arrange
    Mockito
      .when(mockedFileWalker.isRunning())
      .thenReturn(true)
      .thenReturn(true)
      .thenReturn(false);

    // Act
    sut.stopAndWait();

    // Assert
    verify(mockedFileWalker, times(3)).isRunning();
    verify(mockedFileWalker).stop();
  }
}
