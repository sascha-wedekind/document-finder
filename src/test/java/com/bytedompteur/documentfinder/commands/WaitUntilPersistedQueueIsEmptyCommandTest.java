package com.bytedompteur.documentfinder.commands;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WaitUntilPersistedQueueIsEmptyCommandTest {

  @Mock
  PersistedUniqueFileEventQueue mockedQueue;

  @InjectMocks
  WaitUntilPersistedQueueIsEmptyCommand sut;

  @Test
  void run_waitsUntilQueueReturnsIsEmptyTrue() throws InterruptedException {
    // Arrange
    Mockito
      .when(mockedQueue.isEmpty())
      .thenReturn(false)
      .thenReturn(false)
      .thenReturn(true);

    // Act
    sut.run();

    // Assert
    verify(mockedQueue, times(3)).isEmpty();
  }
}
