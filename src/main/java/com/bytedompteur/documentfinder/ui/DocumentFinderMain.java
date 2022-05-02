package com.bytedompteur.documentfinder.ui;


import com.bytedompteur.documentfinder.ui.dagger.DaggerUIComponent;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DocumentFinderMain extends Application {

  public static void main(String[] args) {
    System.out.println("STARTING");
    try {
      launch(args);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void start(Stage primaryStage) {
    Logger log = null;

    try {
      var applicationHomeDirectory = com.bytedompteur.documentfinder.Application.determineApplicationHomeDirectory();
      System.setProperty("APPLICATION_HOME_DIRECTORY", applicationHomeDirectory);
      log = LoggerFactory.getLogger(com.bytedompteur.documentfinder.Application.class);
      log.info("Starting Document Finder");
      log.info("Determined index directory '{}'", applicationHomeDirectory);
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      var uiComponent = DaggerUIComponent
        .builder()
        .numberOfThreads(4)
        .applicationHomeDirectory(com.bytedompteur.documentfinder.Application.determineApplicationHomeDirectory())
        .primaryStage(primaryStage)
        .build();

      var platformAdapter = uiComponent.platformAdapter();
      List<Image> windowIcons = platformAdapter.isMacOs() ? List.of() : List.of(
        new Image(getClass().getResource("/images/DocumentFinderIcon_32.png").toString()),
        new Image(getClass().getResource("/images/DocumentFinderIcon_512.png").toString())
      );
      primaryStage.getIcons().setAll(windowIcons);


      // execute exit application command
      primaryStage.setOnCloseRequest(it -> {
        uiComponent.windowManager().hideApplicationWindow();
      });
      uiComponent.startFulltextSearchServiceCommand().run();
      uiComponent.startDirectoryWatcherCommand().run();
      uiComponent.windowManager().showMainWindow();
      uiComponent.windowManager().showSystemTrayIcon();
      platformAdapter.disableImplicitExit();
      if (null != log) {
        log.info("Started DocumentFinder");
      }
    } catch (Exception e) {
      e.printStackTrace();
      if (null != log) {
        log.error("Could not start DocumentFinder", e);
      }
    }
  }
}
