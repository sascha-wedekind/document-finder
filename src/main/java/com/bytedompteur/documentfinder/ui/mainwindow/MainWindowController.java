package com.bytedompteur.documentfinder.ui.mainwindow;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import com.bytedompteur.documentfinder.ui.FileSystemAdapter;
import com.bytedompteur.documentfinder.ui.FxController;
import com.bytedompteur.documentfinder.ui.WindowManager;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowScope;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import lombok.RequiredArgsConstructor;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@MainWindowScope
@SuppressWarnings("java:S1172")
public class MainWindowController implements FxController {

  private final SearchResultTableController searchResultTable;
  private final AnalyzeFilesProgressBarController progressBarController;
  private final FulltextSearchService fulltextSearchService;
  private final FileSystemAdapter fileSystemAdapter;
  private final WindowManager windowManager;

  @FXML
  public TextField searchTextField;
  private Disposable disposable;

  public void searchForText(KeyEvent inputMethodEvent) {
    Optional.ofNullable(disposable).ifPresent(Disposable::dispose);
    disposable = Mono
      .delay(Duration.ofMillis(300))
      .subscribe(ignore -> handleFindButtonClick(null));
  }

  @SuppressWarnings("java:S1172")
  public void handleFindButtonClick(ActionEvent ignore) {
    searchResultTable.getSearchResults().clear();
    Mono
      .just(searchTextField.getCharacters())
      .filter(it -> !it.isEmpty())
      .flatMapMany(fulltextSearchService::findFilesWithNamesOrContentMatching)
      .filter(it -> it.getPath().toFile().exists()) // Exclude if file does not exist
      .map(it -> SearchResult.build(
        it.getPath(),
        fileSystemAdapter.getSystemIcon(it.getPath()).orElse(null),
        fileSystemAdapter.getLastModified(it.getPath()).orElse(null)
      ))
      .subscribe(it -> searchResultTable.getSearchResults().add(it));

  }

  @SuppressWarnings("java:S1172")
  public void handleOptionsButtonClick(ActionEvent ignore) {
    windowManager.showOptionsWindow();
  }

  @FXML
  public void initialize() {
    // EMPTY
  }

  @Override
  public void beforeViewHide() {
    progressBarController.beforeViewHide();
  }

  public void handleSearchTextFieldAction(ActionEvent actionEvent) {
    handleFindButtonClick(null);
  }
}
