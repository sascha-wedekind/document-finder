package com.bytedompteur.documentfinder.ui;

import com.bytedompteur.documentfinder.ui.mainwindow.dagger.DaggerMainWindowComponent;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class DocumentFinderMain extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws IOException {

    var uiComponent = DaggerMainWindowComponent.builder().numberOfThreads(1).build();
    var lazyNode = uiComponent.mainViewNode();
    var node = lazyNode.get();


    Scene scene = new Scene(node, 640, 480);
    primaryStage.setScene(scene);
    primaryStage.show();
  }
}
