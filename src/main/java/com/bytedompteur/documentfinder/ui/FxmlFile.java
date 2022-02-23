package com.bytedompteur.documentfinder.ui;

public enum FxmlFile {

  SEARCH_RESULT_TABLE("/fxml/SearchResultTable.fxml"),
  MAIN_VIEW("/fxml/MainWindow.fxml"),
  PROGRESS_BAR("/fxml/ProgressBar.fxml"),
  OPTIONS_VIEW("/fxml/OptionsWindow.fxml"),
  FILE_TYPE_OPTIONS("/fxml/FileTypeOptionsView.fxml"),
  FOLDER_OPTIONS("/fxml/FolderOptionsView.fxml");

  private final String resourcePathString;

  FxmlFile(String resourcePathString) {
    this.resourcePathString = resourcePathString;
  }

  public String getResourcePathString() {
    return resourcePathString;
  }
}
