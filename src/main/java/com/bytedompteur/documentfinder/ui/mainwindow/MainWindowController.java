package com.bytedompteur.documentfinder.ui.mainwindow;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import com.bytedompteur.documentfinder.ui.FileSystemAdapter;
import com.bytedompteur.documentfinder.ui.FxController;
import com.bytedompteur.documentfinder.ui.WindowManager;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowScope;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import lombok.RequiredArgsConstructor;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Optional;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@MainWindowScope
@SuppressWarnings("java:S1172")
public class MainWindowController implements FxController {

  public static final String SEARCH_RESULTS_TEXT = "search results";
  private final SearchResultTableController searchResultTable;
  private final AnalyzeFilesProgressBarController progressBarController;
  private final FulltextSearchService fulltextSearchService;
  private final FileSystemAdapter fileSystemAdapter;
  private final WindowManager windowManager;

  @FXML
  public TextField searchTextField;

  @FXML
  public Label numberOfSearchResultsLabel;

  private Disposable disposable;

  public void searchAsYouType(KeyEvent ignore) {
    Optional.ofNullable(disposable).ifPresent(Disposable::dispose);
    disposable = Mono
      .delay(Duration.ofMillis(300))
      .subscribe(it -> {
        if (searchTextField.getCharacters().isEmpty()) {
          clearSearchResults();
        } else {
          searchForFilesMatchingSearchText();
        }
      });
  }

  @SuppressWarnings("java:S1172")
  public void handleClearSearchTextButtonClick(ActionEvent ignore) {
    searchTextField.clear();
    clearSearchResults();
  }

  public void addToSearchResults(SearchResult it) {
    Platform.runLater(() -> {
      searchResultTable.getSearchResults().add(it);
      numberOfSearchResultsLabel.setText(searchResultTable.getSearchResults().size() + " " + SEARCH_RESULTS_TEXT);
    });
  }

  public void clearSearchResults() {
    Platform.runLater(() -> {
      searchResultTable.getSearchResults().clear();
      numberOfSearchResultsLabel.setText("0 " + SEARCH_RESULTS_TEXT);
    });
  }

  public void searchForFilesMatchingSearchText() {
    Mono
      .just(searchTextField.getCharacters())
      .doOnEach(it -> clearSearchResults())
      .filter(it -> !it.isEmpty())
      .flatMapMany(fulltextSearchService::findFilesWithNamesOrContentMatching)
      .filter(it -> it.getPath().toFile().exists()) // Exclude if file does not exist
      .map(it -> SearchResult.build(
        it.getPath(),
        fileSystemAdapter.getSystemIcon(it.getPath()).orElse(null),
        it.getFileLastUpdated().atZone(ZoneId.systemDefault()).toInstant()
      ))
      .subscribe(this::addToSearchResults);
  }

  public void handleFindLastUpdatedButtonClick(ActionEvent ignore) {
    clearSearchResults();
    fulltextSearchService
      .findLastUpdated()
      .map(it -> SearchResult.build(
        it.getPath(),
        fileSystemAdapter.getSystemIcon(it.getPath()).orElse(null),
        it.getFileLastUpdated().atZone(ZoneId.systemDefault()).toInstant()
      ))
      .subscribe(this::addToSearchResults);
  }

  @SuppressWarnings("java:S1172")
  public void handleOptionsButtonClick(ActionEvent ignore) {
    windowManager.showOptionsWindow();
  }

  @FXML
  public void initialize() {
    clearSearchResults();
  }

  @Override
  public void beforeViewHide() {
    progressBarController.beforeViewHide();
  }

  public void handleSearchTextFieldAction(ActionEvent ignore) {
    searchForFilesMatchingSearchText();
  }
}
