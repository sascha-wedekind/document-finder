package com.bytedompteur.documentfinder.ui.mainwindow;

import com.bytedompteur.documentfinder.ui.FxController;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowScope;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.InputMethodEvent;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@MainWindowScope
public class MainWindowController implements FxController {

  private final SearchResultTableController searchResultTable;
  private final AnalyzeFilesProgressBarController progressBarController;

  public void searchForText(InputMethodEvent inputMethodEvent) {
    var searchText = inputMethodEvent.getCommitted();
    System.out.println(searchText);
  }

  public void handleFindButtonClick(ActionEvent actionEvent) {
    System.out.println();
  }

  @FXML
  public void initialize() {
  }
}
