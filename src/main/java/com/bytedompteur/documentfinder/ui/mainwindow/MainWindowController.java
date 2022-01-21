package com.bytedompteur.documentfinder.ui.mainwindow;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import com.bytedompteur.documentfinder.ui.FileSystemAdapter;
import com.bytedompteur.documentfinder.ui.FxController;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowScope;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.InputMethodEvent;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@MainWindowScope
public class MainWindowController implements FxController {

  private final SearchResultTableController searchResultTable;
  private final AnalyzeFilesProgressBarController progressBarController;
  private final FulltextSearchService fulltextSearchService;
  private final FileSystemAdapter fileSystemAdapter;

  @FXML
  public TextField searchTextField;

  public void searchForText(InputMethodEvent inputMethodEvent) {
    // EMPTY
  }

  @SuppressWarnings("java:S1172")
  public void handleFindButtonClick(ActionEvent ignore) {
    searchResultTable.getSearchResults().clear();
    Mono
      .just(searchTextField.getCharacters())
      .filter(it -> !it.isEmpty())
      .flatMapMany(fulltextSearchService::findFilesWithNamesOrContentMatching)
      .map(it -> SearchResult.build(
        it,
        fileSystemAdapter.getSystemIcon(it).orElse(null),
        fileSystemAdapter.getLastModified(it).orElse(null)
      ))
      .subscribe(it -> searchResultTable.getSearchResults().add(it));

  }

  @FXML
  public void initialize() {
    // EMPTY
  }
}
