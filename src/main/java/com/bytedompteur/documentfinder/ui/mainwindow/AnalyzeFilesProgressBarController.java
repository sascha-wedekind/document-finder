package com.bytedompteur.documentfinder.ui.mainwindow;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import com.bytedompteur.documentfinder.ui.FxController;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowScope;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import javax.inject.Inject;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@MainWindowScope
@Slf4j
public class AnalyzeFilesProgressBarController implements FxController {

  public static final Path RESET_PROGRESS_INDICATOR_PATH = Path.of("DEFAULT");
  private final FulltextSearchService fulltextSearchService;

  @FXML
  public ProgressIndicator progressIndicator;

  @FXML
  public Label currentFileProcessedLabel;


  @FXML
  protected void initialize() {
    showNumberOfScannedFiles();
    subscribeFlux(fulltextSearchService.getCurrentPathProcessed());
  }

  private void subscribeFlux(Flux<Path> pathProcessedFlux) {
    log.debug("Subscribing to 'fulltextSearchService'");
    AtomicReference<Disposable> ref = new AtomicReference<>();

    var processTimeoutFlux = Flux
      .fromArray(new Path[]{RESET_PROGRESS_INDICATOR_PATH})
      .doFinally(signalType -> {
        log.debug("Resubscribing to 'fulltextSearchService' ... {}", signalType);
        ref.get().dispose();
        subscribeFlux(fulltextSearchService.getCurrentPathProcessed());
        log.debug("... resubscribed to 'fulltextSearchService'");
      });

    var disposable = pathProcessedFlux
      .timeout(Duration.ofSeconds(5), processTimeoutFlux)
      .doOnComplete(this::showNumberOfScannedFiles)
      .subscribe(this::delegatePathEvent);

    ref.set(disposable);
    log.debug("Subscribed to 'fulltextSearchService'");
  }

  protected void delegatePathEvent(Path path) {
    if (path == RESET_PROGRESS_INDICATOR_PATH) {
      showNumberOfScannedFiles();
    } else {
      showProgress(path.toString());
    }
  }

  public void showProgress(String value) {
    log.debug("Show progress '{}'", value);
    Platform.runLater(() -> {
      progressIndicator.setVisible(true);
      currentFileProcessedLabel.setText(value);
    });
  }


  public void showNumberOfScannedFiles() {
    log.debug("Show number of scanned files");
    Platform.runLater(() -> {
      progressIndicator.setVisible(false);
      var numberOfFilesInIndex = fulltextSearchService.getScannedFiles();
      var text = String.format("The index contains %s analyzed files", numberOfFilesInIndex);
      currentFileProcessedLabel.setText(text);
    });
  }

}
