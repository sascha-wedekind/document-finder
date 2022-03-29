package com.bytedompteur.documentfinder.ui.mainwindow;

import com.bytedompteur.documentfinder.ui.FxController;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowScope;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import java.util.Optional;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@MainWindowScope
public class SearchResultTableController implements FxController {

  private final SearchResultTableContextMenu contextMenu;

  private final ObservableList<SearchResult> searchResults = FXCollections.observableArrayList();

  @FXML
  public TableView<SearchResult> resultTable;

  public ObservableList<SearchResult> getSearchResults() {
    return searchResults;
  }

  @FXML
  public void initialize() {
    resultTable.setContextMenu(contextMenu);
    resultTable.setOnContextMenuRequested(event -> Optional
      .ofNullable(resultTable.getSelectionModel().getSelectedItem())
      .ifPresent(it -> {
        contextMenu.setSelectedSearchResult(it);
        contextMenu.show(event.getPickResult().getIntersectedNode(), event.getScreenX(), event.getScreenY());
      }));
  }
}
