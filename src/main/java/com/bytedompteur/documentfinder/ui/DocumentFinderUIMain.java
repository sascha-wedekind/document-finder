package com.bytedompteur.documentfinder.ui;


import com.bytedompteur.documentfinder.DocumentFinderMain;
import com.bytedompteur.documentfinder.commands.StopAllGracefulCommand;
import com.bytedompteur.documentfinder.ui.dagger.DaggerUIComponent;
import com.bytedompteur.documentfinder.ui.dagger.UIComponent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public class DocumentFinderUIMain
  extends Application {

  @Override
  public void start(Stage primaryStage) {
    Optional<StopAllGracefulCommand> stopAllGracefulCommand = Optional.empty();
    try {
      UIComponent uiComponent = createUIComponent(primaryStage);
      stopAllGracefulCommand = Optional.of(uiComponent.stopAllGracefulCommand());
      configureStage(primaryStage, uiComponent, stopAllGracefulCommand.get());

      uiComponent.ipcService().startIPCServer();
      uiComponent.startFulltextSearchServiceCommand().run();
      uiComponent.startDirectoryWatcherCommand().run();
      uiComponent.windowManager().showMainWindow();
      uiComponent.windowManager().showSystemTrayIcon();
      log.info("Started DocumentFinder");
    } catch (Exception e) {
      log.error("Could not start DocumentFinder, shutting down", e);
      stopAllGracefulCommand.ifPresentOrElse(StopAllGracefulCommand::run, Platform::exit);
    }
  }

  private UIComponent createUIComponent(Stage primaryStage) {
    return DaggerUIComponent
      .builder()
      .numberOfThreads(4)
      .applicationHomeDirectory(DocumentFinderMain.determineApplicationHomeDirectory())
      .primaryStage(primaryStage)
      .build();
  }

  private void configureStage(Stage primaryStage, UIComponent uiComponent, StopAllGracefulCommand stopAllGracefulCommand) {
    var platformAdapter = uiComponent.platformAdapter();
    platformAdapter.disableImplicitExit();
    List<Image> windowIcons = platformAdapter.isMacOs() ? List.of() : List.of(
      new Image(getClass().getResource("/images/DocumentFinderIcon_32.png").toString()),
      new Image(getClass().getResource("/images/DocumentFinderIcon_512.png").toString())
    );
    primaryStage.getIcons().setAll(windowIcons);
    primaryStage.setOnCloseRequest(it -> handleStageCloseEvent(uiComponent, stopAllGracefulCommand));
  }

  private void handleStageCloseEvent(UIComponent uiComponent, StopAllGracefulCommand stopAllGracefulCommand) {
    var windowManager = uiComponent.windowManager();
    if (windowManager.isSystemTraySupported()) {
      windowManager.hideApplicationWindow();
    } else {
      stopAllGracefulCommand.run();
    }
  }
}
