package com.bytedompteur.documentfinder.ui.optionswindow;

import com.bytedompteur.documentfinder.ui.FxController;
import com.bytedompteur.documentfinder.ui.FxmlFile;
import com.bytedompteur.documentfinder.ui.dagger.FxmlParent;
import com.bytedompteur.documentfinder.ui.optionswindow.dagger.OptionsWindowScope;
import dagger.Lazy;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import javax.inject.Inject;

@OptionsWindowScope
public class OptionsWindowController implements FxController {

  private final Lazy<Parent> fileTypeOptionsView;
  private final Lazy<Parent> folderOptionsView;

  @FXML
  public BorderPane optionsContentPane;

  @Inject
  public OptionsWindowController(
    @FxmlParent(FxmlFile.FILE_TYPE_OPTIONS) Lazy<Parent> fileTypeOptionsView,
    @FxmlParent(FxmlFile.FOLDER_OPTIONS) Lazy<Parent> folderOptionsView
  ) {
    this.fileTypeOptionsView = fileTypeOptionsView;
    this.folderOptionsView = folderOptionsView;
  }

  public void handleFileTypeOptionsSectionClick(ActionEvent actionEvent) {
//    System.out.println();
//    var pane = new Pane();
//    pane.setStyle("-fx-background-color: red;");
    optionsContentPane.setCenter(fileTypeOptionsView.get());
  }

  public void handleFolderOptionsSectionClick(ActionEvent actionEvent) {
//    System.out.println();
//    var pane = new Pane();
//    pane.setStyle("-fx-background-color: blue;");
    optionsContentPane.setCenter(folderOptionsView.get());
  }

  public void handleCommonOptionsSectionClick(ActionEvent actionEvent) {
    System.out.println();
    var pane = new Pane();
    pane.setStyle("-fx-background-color: green;");
    optionsContentPane.setCenter(pane);
  }
}
