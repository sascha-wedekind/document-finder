package com.bytedompteur.documentfinder.ui;

import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowComponent;
import com.bytedompteur.documentfinder.ui.optionswindow.dagger.OptionsWindowComponent;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class WindowManager {

  private final Stage stage;
  private final MainWindowComponent.Builder mainWindowComponentBuilder;
  private final OptionsWindowComponent.Builder optionsWindowComponentBuilder;
  private MainWindowComponent mainWindowComponent;
  private OptionsWindowComponent optionsWindowComponent;
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

  private void notifyCurrentControllerAfterViewShown() {
    if (currentController != null) {
      Platform.runLater(() -> currentController.afterViewShown());
    }
  }

  private void notifyCurrentControllerBeforeViewHide() {
    if (currentController != null) {
      Platform.runLater(() -> currentController.beforeViewHide());
    }
  }

  protected void show(Parent value) {
    Scene scene = new Scene(value, 640, 480);
    stage.setScene(scene);
    stage.show();
  }
}
