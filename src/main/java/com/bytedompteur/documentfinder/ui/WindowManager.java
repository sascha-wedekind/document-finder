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

import java.util.function.Function;


@RequiredArgsConstructor
@Slf4j
public class WindowManager {

  public static final Function<Parent, Scene> DEFAULT_SCENE_FACTORY = p -> new Scene(p, 640, 480);

  private final Stage stage;
  private final MainWindowComponent.Builder mainWindowComponentBuilder;
  private final OptionsWindowComponent.Builder optionsWindowComponentBuilder;
  private final SystemTrayComponent.Builder systemTrayComponentBuilder;
  private final JavaFxPlatformAdapter platformAdapter;

  private final Function<Parent, Scene> sceneFactory;

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
    platformAdapter.runLater(() -> stage.setScene(null));
    platformAdapter.runLater(stage::close);
  }

  public boolean isSystemTraySupported() {
    return platformAdapter.isSystemTraySupported();
  }

  protected void show(Parent value) {
    platformAdapter.runLater(() -> stage.setScene(null));

    Scene scene = sceneFactory.apply(value);
    platformAdapter.runLater(() -> {
      stage.setScene(scene);
      stage.show();
    });
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
