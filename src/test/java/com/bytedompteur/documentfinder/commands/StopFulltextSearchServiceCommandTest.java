package com.bytedompteur.documentfinder.commands;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StopFulltextSearchServiceCommandTest {

  @Mock
  FulltextSearchService mockedService;

  @InjectMocks
  StopFulltextSearchServiceCommand sut;

  @Test
  void run_doesNothing_whenServiceIsNotRunning() {
    // Arrange
    Mockito
      .when(mockedService.inboundFileEventProcessingRunning())
      .thenReturn(false);

    // Act
    sut.run();

    // Assert
    verify(mockedService, never()).stopInboundFileEventProcessing();
  }

  @Test
  void stopAndWait_triggersServiceStopAndWaitsUntilServiceHasStopped() {
    // Arrange
    Mockito
      .when(mockedService.inboundFileEventProcessingRunning())
      .thenReturn(true)
      .thenReturn(true)
      .thenReturn(false);

    // Act
    sut.stopAndWait();

    // Assert
    verify(mockedService, times(3)).inboundFileEventProcessingRunning();
    verify(mockedService).stopInboundFileEventProcessing();
  }
}
