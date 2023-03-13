package com.bytedompteur.documentfinder.ui.mainwindow;

import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import static java.util.Objects.isNull;

public class TwoLineTableCell extends TableCell<SearchResult, SearchResult>{

  private static final String DEFAULT_STYLE_CLASS = "two-line-table-cell";

  public TwoLineTableCell() {
    getStyleClass().addAll(DEFAULT_STYLE_CLASS);
  }

  @Override
  protected void updateItem(SearchResult item, boolean empty) {
    super.updateItem(item, empty);
    if (isNull(item) || empty) {
      setGraphic(null);
    } else {
      var directoryName = item.getDirectoryName();
      var directoryLabel = new Label();
      directoryLabel.setText(directoryName);
      directoryLabel.setTooltip(new Tooltip(directoryName));
      directoryLabel.getStyleClass().add("directory-label");

      var filename = item.getFileOrDirectoryName();
      var filenameLabel = new Label();
      filenameLabel.setText(filename);
      filenameLabel.setTooltip(new Tooltip(filename));
      filenameLabel.getStyleClass().add("filename-label");

      var vBox = new VBox();
      vBox.getChildren().addAll(filenameLabel, directoryLabel);
      setGraphic(vBox);
    }
  }




}
