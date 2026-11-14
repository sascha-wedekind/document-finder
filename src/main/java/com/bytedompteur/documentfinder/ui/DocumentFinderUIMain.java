package com.bytedompteur.documentfinder.ui;

import com.bytedompteur.documentfinder.DocumentFinderMain;
import com.bytedompteur.documentfinder.commands.ExitApplicationCommand;
import com.bytedompteur.documentfinder.ui.dagger.DaggerUIComponent;
import com.bytedompteur.documentfinder.ui.dagger.UIComponent;
import com.jthemedetecor.OsThemeDetector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public class DocumentFinderUIMain
  extends Application {

  /**
   * Runs on the JavaFX launcher thread while the JavaFX toolkit is starting.
   */
  @Override
  public void init() {
    startWarmUp("os-theme-detector", OsThemeDetector::getDetector);
  }

  @Override
  public void start(Stage primaryStage) {
    Optional<ExitApplicationCommand> exitApplicationCommand = Optional.empty();
    try {
      UIComponent uiComponent = createUIComponent(primaryStage);
      startWarmUp("fulltext-search-service", uiComponent::startFulltextSearchServiceCommand);
      exitApplicationCommand = Optional.of(uiComponent.exitApplicationCommand());
      configureStage(primaryStage, uiComponent, exitApplicationCommand.get());

      uiComponent.ipcService().startIPCServer();
      uiComponent.startFulltextSearchServiceCommand().run();
      uiComponent.startDirectoryWatcherCommand().run();
      uiComponent.windowManager().showMainWindow();
      Platform.runLater(() -> showSystemTrayIcon(uiComponent));
      registerJVMShutdownHook(uiComponent);

      Screen.getScreens().addListener((ListChangeListener<Screen>) c -> {
        Platform.runLater(() -> uiComponent.windowManager().hideSystemTrayIcon());
        Platform.runLater(() -> uiComponent.windowManager().showSystemTrayIcon());
      });

      log.info("Started DocumentFinder");
    } catch (Exception e) {
      log.error("Could not start DocumentFinder, shutting down", e);
      exitApplicationCommand.ifPresentOrElse(ExitApplicationCommand::run, Platform::exit);
    }
  }

  /**
   * Creates objects that are expensive to build and needed later on a separate thread, while the JavaFX thread does
   * other work. Dagger singletons are thread safe, so a later request for the same object waits for the result. A failure
   * is only logged here, because the later request on the JavaFX thread fails the same way and is handled there.
   */
  private static void startWarmUp(String name, Runnable task) {
    Thread.ofPlatform().daemon().name("warm-up-" + name).start(() -> {
      try {
        task.run();
      } catch (Exception e) {
        log.warn("Warm-up of '{}' failed", name, e);
      }
    });
  }

  private void showSystemTrayIcon(UIComponent uiComponent) {
    try {
      uiComponent.windowManager().showSystemTrayIcon();
    } catch (Exception e) {
      log.error("Could not show system tray icon", e);
    }
  }

  private void registerJVMShutdownHook(UIComponent uiComponent) {
    log.info("Registering JVM shutdown hook");
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("Shutdown hook called");
      uiComponent.exitApplicationCommand().run();
    }));
    log.info("Registered JVM shutdown hook");
  }

  private UIComponent createUIComponent(Stage primaryStage) {
    int cores = Runtime.getRuntime().availableProcessors();
    return DaggerUIComponent
      .builder()
      .numberOfVirtualThreads(cores * 12)
      .applicationHomeDirectory(DocumentFinderMain.determineApplicationHomeDirectory())
      .primaryStage(primaryStage)
      .hostServices(getHostServices())
      .build();
  }

  private void configureStage(Stage primaryStage, UIComponent uiComponent, ExitApplicationCommand exitApplicationCommand) {
    var platformAdapter = uiComponent.platformAdapter();
    platformAdapter.disableImplicitExit();

    //noinspection DataFlowIssue
    List<Image> windowIcons = platformAdapter.isMacOs() ? List.of() : List.of(
      new Image(getClass().getResource("/images/DocumentFinderIcon_32.png").toString()),
      new Image(getClass().getResource("/images/DocumentFinderIcon_512.png").toString())
    );
    primaryStage.getIcons().setAll(windowIcons);
    primaryStage.setTitle("Document Finder");
    primaryStage.setOnCloseRequest(it -> handleStageCloseEvent(uiComponent, exitApplicationCommand));
  }

  private void handleStageCloseEvent(UIComponent uiComponent, ExitApplicationCommand exitApplicationCommand) {
    var windowManager = uiComponent.windowManager();
    if (windowManager.isSystemTraySupported()) {
      log.debug("Close event. Hiding window because system tray is supported");
      windowManager.hideApplicationWindow();
    } else {
      log.debug("Close event. Exiting application because system tray is not supported");
      exitApplicationCommand.run();
    }
  }
}
