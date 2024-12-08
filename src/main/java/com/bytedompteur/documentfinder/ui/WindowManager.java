package com.bytedompteur.documentfinder.ui;

import com.bytedompteur.documentfinder.ui.adapter.out.JavaFxPlatformAdapter;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowComponent;
import com.bytedompteur.documentfinder.ui.optionswindow.dagger.OptionsWindowComponent;
import com.bytedompteur.documentfinder.ui.systemtray.SystemTrayIconController;
import com.bytedompteur.documentfinder.ui.systemtray.dagger.SystemTrayComponent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cryptomator.integrations.tray.TrayIntegrationProvider;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;


@RequiredArgsConstructor
@Slf4j
public class WindowManager {

  public static final Function<Parent, Scene> DEFAULT_SCENE_FACTORY = p -> {
    var result = new Scene(p, 640, 480);
      try {
          result.getStylesheets()
              .add(
                  Objects
                      .requireNonNull(WindowManager.class.getResource("/css/default.css"))
                      .toExternalForm()
              );
      } catch (Exception e) {
          log.error("Could not load default.css", e);
      }
      return result;
  };

  private final Stage stage;
  private final MainWindowComponent.Builder mainWindowComponentBuilder;
  private final OptionsWindowComponent.Builder optionsWindowComponentBuilder;
  private final SystemTrayComponent.Builder systemTrayComponentBuilder;
  private final JavaFxPlatformAdapter platformAdapter;

  private final Function<Parent, Scene> sceneFactory;

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private final Optional<TrayIntegrationProvider> trayIntegrationProvider;

  private MainWindowComponent mainWindowComponent;
  private OptionsWindowComponent optionsWindowComponent;
  private SystemTrayIconController systemTrayIconController;
  private FxController currentController;

  public void showMainWindow() {
    if (mainWindowComponent == null) {
      mainWindowComponent = mainWindowComponentBuilder.build();
    }
    notifyCurrentControllerBeforeViewHide();
    currentController = mainWindowComponent.mainWindowController();
    show(mainWindowComponent.mainViewNode().get());
    notifyCurrentControllerAfterViewShown();
  }

  public void showOptionsWindow() {
    if (optionsWindowComponent == null) {
      optionsWindowComponent = optionsWindowComponentBuilder.build();
    }
    notifyCurrentControllerBeforeViewHide();
    currentController = optionsWindowComponent.optionsWindowController();
    show(optionsWindowComponent.optionsViewNode().get());
    notifyCurrentControllerAfterViewShown();
  }

  public void notifyCurrentControllerBeforeViewHide() {
    if (currentController != null) {
      platformAdapter.runLater(() -> currentController.beforeViewHide());
    }
  }

  public void hideSystemTrayIcon() {
    obtainSystemTrayIconController();
    systemTrayIconController.unregisterTrayIcon();
  }

  public void showSystemTrayIcon() {
    obtainSystemTrayIconController();
    systemTrayIconController.registerTrayIcon();
  }

  public void hideApplicationWindow() {
    platformAdapter.runLater(stage::close);
    platformAdapter.runLater(() -> trayIntegrationProvider.ifPresent(TrayIntegrationProvider::minimizedToTray));
  }

  public boolean isSystemTraySupported() {
    return platformAdapter.isSystemTraySupported();
  }

  protected void show(Parent value) {
    Scene scene = sceneFactory.apply(value);
    platformAdapter.runLater(() -> {
      trayIntegrationProvider.ifPresent(TrayIntegrationProvider::restoredFromTray);
      setDockOrTaskbarIcon();
      stage.setScene(scene);
      stage.show();
      stage.toFront();
    });
  }

  protected void setDockOrTaskbarIcon() {
    if (Taskbar.isTaskbarSupported()) {
        try {
            Taskbar taskbar = Taskbar.getTaskbar();
            if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
              var defaultToolkit = Toolkit.getDefaultToolkit();
              var image = defaultToolkit.getImage(getClass().getResource("/images/DocumentFinderIcon_512.png"));
              taskbar.setIconImage(image);
            }
        } catch (Exception e) {
            log.error("Could not set taskbar icon", e);
        }
    }
  }

  protected FxController getCurrentController() {
    return currentController;
  }

  private void notifyCurrentControllerAfterViewShown() {
    if (currentController != null) {
      platformAdapter.runLater(() -> currentController.afterViewShown());
    }
  }

  private void obtainSystemTrayIconController() {
    if (systemTrayIconController == null) {
      systemTrayIconController = systemTrayComponentBuilder.build().systemTrayIconController().get();
    }
  }
}
