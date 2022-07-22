package com.bytedompteur.documentfinder.ui.systemtray;

import com.bytedompteur.documentfinder.commands.ExitApplicationCommand;
import com.bytedompteur.documentfinder.ui.WindowManager;
import com.bytedompteur.documentfinder.ui.adapter.out.JavaFxPlatformAdapter;
import com.bytedompteur.documentfinder.ui.systemtray.dagger.SystemTrayScope;
import dagger.Lazy;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.ActionEvent;

@Slf4j
@SystemTrayScope
public class SystemTrayMenuController {

  private final Lazy<WindowManager> windowManager;
  private final ExitApplicationCommand exitApplicationCommand;
  private final JavaFxPlatformAdapter platformAdapter;

  @Inject
  public SystemTrayMenuController(
    Lazy<WindowManager> windowManager,
    ExitApplicationCommand exitApplicationCommand,
    JavaFxPlatformAdapter platformAdapter
  ) {
    this.windowManager = windowManager;
    this.exitApplicationCommand = exitApplicationCommand;
    this.platformAdapter = platformAdapter;
  }

  public PopupMenu getMenu() {
    return createMenu();
  }

  protected PopupMenu createMenu() {
    PopupMenu menu = new PopupMenu("Document Finder");

    MenuItem showMainWindowItem = new MenuItem("Show");
    showMainWindowItem.addActionListener(this::showMainWindowHandler);
    showMainWindowItem.setFont(Font.decode(null).deriveFont(Font.BOLD));
    menu.add(showMainWindowItem);

    MenuItem exitApplicationItem = new MenuItem("Exit");
    exitApplicationItem.addActionListener(this::exitApplicationHandler);
    exitApplicationItem.setFont(Font.decode(null).deriveFont(Font.BOLD).deriveFont(Font.PLAIN));
    menu.add(exitApplicationItem);

    return menu;
  }

  private void exitApplicationHandler(ActionEvent actionEvent) {
    platformAdapter.runLater(exitApplicationCommand);
  }

  void showMainWindowHandler(ActionEvent actionEvent) {
    windowManager.get().showMainWindow();
  }
}
