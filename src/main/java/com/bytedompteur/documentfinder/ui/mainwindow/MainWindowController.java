package com.bytedompteur.documentfinder.ui.mainwindow;

import com.bytedompteur.documentfinder.ui.FxController;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowScope;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.input.InputMethodEvent;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@MainWindowScope
public class MainWindowController implements Initializable, FxController {

  private final SearchResultTableController searchResultTable;

  public void searchForText(InputMethodEvent inputMethodEvent) {
    var searchText = inputMethodEvent.getCommitted();
    System.out.println(searchText);
  }

  public void handleFindButtonClick(ActionEvent actionEvent) {
    System.out.println();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
  }
}
