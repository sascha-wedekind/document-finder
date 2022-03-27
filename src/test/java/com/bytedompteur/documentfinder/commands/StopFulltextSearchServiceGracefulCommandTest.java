package com.bytedompteur.documentfinder.commands;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StopFulltextSearchServiceGracefulCommandTest {

  @Mock
  FulltextSearchService mockedFulltextSearchService;

  @InjectMocks
  WaitUntilFulltextSearchServiceProcessedAllEventsCommand sut;

  @Test
  void waitUntilAllEventsAreProcessed_waitsAsLongAsZeroIsReturnedFromSearchServiceNumberOfEventsNotYetProcessed() {
    // Arrange
    Mockito
      .when(mockedFulltextSearchService.getNumberOfEventsNotYetProcessed())
      .thenReturn(1L)
      .thenReturn(1L)
      .thenReturn(1L)
      .thenReturn(0L);

    // Act
    sut.waitUntilAllEventsAreProcessed();

    // Assert
    verify(mockedFulltextSearchService, times(4)).getNumberOfEventsNotYetProcessed();
  }
}
