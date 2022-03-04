package com.bytedompteur.documentfinder.ui;

import com.bytedompteur.documentfinder.ui.mainwindow.dagger.DaggerMainWindowComponent;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DocumentFinderMain extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {

    var uiComponent = DaggerMainWindowComponent
      .builder()
      .numberOfThreads(1)
      .applicationHomeDirectory(com.bytedompteur.documentfinder.Application.determineApplicationHomeDirectory())
      .build();
    var lazyNode = uiComponent.mainViewNode();
    var node = lazyNode.get();


    Scene scene = new Scene(node, 640, 480);
    primaryStage.setScene(scene);
    primaryStage.show();
  }
}
