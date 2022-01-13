package com.bytedompteur.documentfinder.ui.mainwindow;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import com.bytedompteur.documentfinder.ui.FxmlFile;
import com.bytedompteur.documentfinder.ui.dagger.FxmlLoaderFactory;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(ApplicationExtension.class)
@Slf4j
class AnalyzeFilesProgressBarControllerTest {

  private TestPublisher<Path> testPublisher;
  private FulltextSearchService mockedFulltextSearchService;

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

    var controller = new AnalyzeFilesProgressBarController(mockedFulltextSearchService);
    var fxmlLoaderFactory = new FxmlLoaderFactory(Map.of(AnalyzeFilesProgressBarController.class, () -> controller));
    var node = fxmlLoaderFactory.createParentNode(FxmlFile.PROGRESS_BAR);
    stage.setScene(new Scene(node, 100, 50));
    stage.show();
  }

  protected Flux<Path> createFluxAndRegisterToTestClass() {
    log.info("Creating new TestPublisher");
    testPublisher = TestPublisher.create();
    return testPublisher.flux();
  }

  @Test
  void controller_setsLabelToPathAndProgressIndicatorToVisible_whenFLuxEmits(FxRobot robot) {
    // Arrange
    var progressIndicator = robot.lookup("#progressIndicator").queryAs(ProgressIndicator.class);
    var currentFileProcessedLabel = robot.lookup("#currentFileProcessedLabel").queryAs(Label.class);
    var path = Path.of("a/b/c/d");

    //Act
    testPublisher.next(path); // do not complete stream

    // Assert
    assertThat(progressIndicator.isVisible()).isTrue();
    assertThat(currentFileProcessedLabel).extracting(Label::getText).isEqualTo(path.toString());
    verify(mockedFulltextSearchService).getCurrentPathProcessed(); // assert resubscribe
  }

  @Test
  void controller_resetsLabelToShowingNumberOfAnalyzedFilesAndProgressIndicatorToHiddenAndResubscribesFlux_whenFLuxDidNotEmitWithin5Seconds(FxRobot robot) throws InterruptedException {
    // Arrange
    var progressIndicator = robot.lookup("#progressIndicator").queryAs(ProgressIndicator.class);
    var currentFileProcessedLabel = robot.lookup("#currentFileProcessedLabel").queryAs(Label.class);
    var path = Path.of("a/b/c/d");

    //Act
    testPublisher.next(path); // do not complete stream
    Thread.sleep(6000);

    // Assert
    assertThat(progressIndicator.isVisible()).isFalse();
    assertThat(currentFileProcessedLabel.getText()).isEqualTo("The index contains 10 analyzed files"); // see mock config above
//    verify(mockedFulltextSearchService, times(2)).getCurrentPathProcessed(); // assert resubscribe
  }

  @Test
  void controller_resetsLabelToShowingNumberOfAnalyzedFilesAndProgressIndicatorToHiddenAndResubscribesFlux_whenFLuxCompletes(FxRobot robot) throws InterruptedException {
    // Arrange
    var progressIndicator = robot.lookup("#progressIndicator").queryAs(ProgressIndicator.class);
    var currentFileProcessedLabel = robot.lookup("#currentFileProcessedLabel").queryAs(Label.class);
    var path = Path.of("a/b/c/d");

    //Act
    testPublisher.next(path); // do not complete stream
    testPublisher.complete();
    Thread.sleep(500);

    // Assert
    assertThat(progressIndicator.isVisible()).isFalse();
    assertThat(currentFileProcessedLabel.getText()).isEqualTo("The index contains 10 analyzed files"); // see mock config above
//    verify(mockedFulltextSearchService, times(2)).getCurrentPathProcessed(); // assert resubscribe
  }

  @Test
  void controller_continuesShowingSettingLabelToProcessedPath_afterFluxTimeout(FxRobot robot) throws InterruptedException {
    // Arrange
    var progressIndicator = robot.lookup("#progressIndicator").queryAs(ProgressIndicator.class);
    var currentFileProcessedLabel = robot.lookup("#currentFileProcessedLabel").queryAs(Label.class);
    var path = Path.of("a/b/c/d");
    var path2 = Path.of("1/2/3/4");

    //Act
    testPublisher.next(path); // do not complete stream
    Thread.sleep(5500);
    testPublisher.next(path2);
    Thread.sleep(1000);

    // Assert
    assertThat(progressIndicator.isVisible()).isTrue();
    assertThat(currentFileProcessedLabel.getText()).isEqualTo(path2.toString()); // see mock config above
//    verify(mockedFulltextSearchService, times(2)).getCurrentPathProcessed(); // assert resubscribe
  }
}
