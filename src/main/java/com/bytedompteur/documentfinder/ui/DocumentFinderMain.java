package com.bytedompteur.documentfinder.ui;


import com.bytedompteur.documentfinder.ui.dagger.DaggerUIComponent;
import javafx.application.Application;
import javafx.stage.Stage;

public class DocumentFinderMain extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {

    var uiComponent = DaggerUIComponent
      .builder()
      .numberOfThreads(4)
      .applicationHomeDirectory(com.bytedompteur.documentfinder.Application.determineApplicationHomeDirectory())
      .primaryStage(primaryStage)
      .build();

    uiComponent.windowManager().showMainWindow();
  }
}
