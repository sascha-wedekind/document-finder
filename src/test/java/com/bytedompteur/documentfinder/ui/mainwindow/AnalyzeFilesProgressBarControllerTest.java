package com.bytedompteur.documentfinder.ui.mainwindow;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import com.bytedompteur.documentfinder.ui.FxmlFile;
import com.bytedompteur.documentfinder.ui.adapter.out.JavaFxPlatformAdapter;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.nio.file.Path;
import java.time.Duration;

import static com.bytedompteur.documentfinder.ui.UITestInitHelper.addNodeUnderTestToStage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(ApplicationExtension.class)
@Slf4j
class AnalyzeFilesProgressBarControllerTest {

  private TestPublisher<Path> testPublisher;
  private FulltextSearchService mockedFulltextSearchService;
  private final VirtualTimeScheduler virtualTimeScheduler = VirtualTimeScheduler.getOrSet();

  @Start
  void start(Stage stage) {
    mockedFulltextSearchService = Mockito.mock(FulltextSearchService.class);
    Mockito
      .when(mockedFulltextSearchService.getCurrentPathProcessed())
      .then((Answer<Flux<Path>>) invocation -> createFluxAndRegisterToTestClass())
      .then((Answer<Flux<Path>>) invocation -> createFluxAndRegisterToTestClass());

    Mockito
      .when(mockedFulltextSearchService.getScannedFiles())
      .thenReturn(10);

    addNodeUnderTestToStage(
      FxmlFile.PROGRESS_BAR,
      new AnalyzeFilesProgressBarController(mockedFulltextSearchService, new JavaFxPlatformAdapter()),
      stage
    );
    stage.show();
  }


  protected Flux<Path> createFluxAndRegisterToTestClass() {
    log.info("Creating new TestPublisher");
    testPublisher = TestPublisher.create();
    return testPublisher.flux().publishOn(virtualTimeScheduler);
  }

  @Test
  void controller_setsLabelToPathAndProgressIndicatorToVisible_whenFLuxEmits(FxRobot robot) {
    // Arrange
    var progressIndicator = robot.lookup("#progressIndicator").queryAs(ProgressIndicator.class);
    var currentFileProcessedLabel = robot.lookup("#currentFileProcessedLabel").queryAs(Label.class);
    var path = Path.of("a/b/c/d");

    //Act
    testPublisher.next(path); // do not complete stream
    robot.interrupt(); // Wait until all events on the FX event thread are completed

    // Assert
    assertThat(progressIndicator.isVisible()).isTrue();
    assertThat(currentFileProcessedLabel).extracting(Label::getText).isEqualTo(path.toString());
    verify(mockedFulltextSearchService).getCurrentPathProcessed(); // assert resubscribe
  }

  @Test
  void controller_resetsLabelToShowingNumberOfAnalyzedFilesAndProgressIndicatorToHiddenAndResubscribesFlux_whenFLuxDidNotEmitWithin5Seconds(FxRobot robot) {
    // Arrange
    var progressIndicator = robot.lookup("#progressIndicator").queryAs(ProgressIndicator.class);
    var currentFileProcessedLabel = robot.lookup("#currentFileProcessedLabel").queryAs(Label.class);
    var path = Path.of("a/b/c/d");

    //Act
    testPublisher.next(path); // do not complete stream
    virtualTimeScheduler.advanceTimeBy(Duration.ofSeconds(6)); // Forward time by 6 seconds in virtual spring reactor scheduler
    robot.interrupt(); // Wait until all events on the FX event thread are completed

    // Assert
    assertThat(progressIndicator.isVisible()).isFalse();
    assertThat(currentFileProcessedLabel.getText()).isEqualTo("The index contains 10 analyzed files"); // see mock config above
    verify(mockedFulltextSearchService, times(2)).getCurrentPathProcessed(); // assert resubscribe
  }

  @Test
  void controller_resetsLabelToShowingNumberOfAnalyzedFilesAndProgressIndicatorToHidden_whenFLuxCompletes(FxRobot robot) {
    // Arrange
    var progressIndicator = robot.lookup("#progressIndicator").queryAs(ProgressIndicator.class);
    var currentFileProcessedLabel = robot.lookup("#currentFileProcessedLabel").queryAs(Label.class);
    var path = Path.of("a/b/c/d");

    //Act
    testPublisher.next(path); // do not complete stream
    testPublisher.complete();
    robot.interrupt(); // Wait until all events on the FX event thread are completed

    // Assert
    assertThat(progressIndicator.isVisible()).isFalse();
    assertThat(currentFileProcessedLabel.getText()).isEqualTo("The index contains 10 analyzed files"); // see mock config above
    verify(mockedFulltextSearchService).getCurrentPathProcessed(); // assert no resubscribe
  }

  @Test
  void controller_continuesShowingSettingLabelToProcessedPath_afterFluxTimeout(FxRobot robot) {
    // Arrange
    var progressIndicator = robot.lookup("#progressIndicator").queryAs(ProgressIndicator.class);
    var currentFileProcessedLabel = robot.lookup("#currentFileProcessedLabel").queryAs(Label.class);
    var path = Path.of("a/b/c/d");
    var path2 = Path.of("1/2/3/4");

    //Act
    testPublisher.next(path); // do not complete stream
    virtualTimeScheduler.advanceTimeBy(Duration.ofSeconds(6)); // Forward time by 6 seconds in virtual spring reactor scheduler
    testPublisher.next(path2);
    robot.interrupt(); // Wait until all events on the FX event thread are completed

    // Assert
    assertThat(progressIndicator.isVisible()).isTrue();
    assertThat(currentFileProcessedLabel.getText()).isEqualTo(path2.toString()); // see mock config above
    verify(mockedFulltextSearchService, times(2)).getCurrentPathProcessed(); // assert resubscribe
  }
}
