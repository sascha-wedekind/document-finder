package com.bytedompteur.documentfinder.ui.systemtray;

import com.bytedompteur.documentfinder.ui.adapter.out.JavaFxPlatformAdapter;
import com.bytedompteur.documentfinder.ui.systemtray.dagger.SystemTrayScope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.Validate;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;

@Slf4j
@SystemTrayScope
public class SystemTrayIconController {

  private TrayIcon trayIcon;
  private volatile boolean registered;
  private final SystemTrayImageFactory imageFactory;
  private final SystemTrayMenuController trayMenuController;
  private final JavaFxPlatformAdapter platformAdapter;

  @Inject
  public SystemTrayIconController(
    SystemTrayImageFactory imageFactory,
    SystemTrayMenuController trayMenuController,
    JavaFxPlatformAdapter platformAdapter
  ) {
    this.imageFactory = imageFactory;
    this.trayMenuController = trayMenuController;
    this.platformAdapter = platformAdapter;
  }

  public synchronized void registerTrayIcon() throws IllegalStateException {
    if (platformAdapter.isSystemTraySupported()) {
        SwingUtilities.invokeLater(() -> {
          Validate.isTrue(!isRegistered(), "System tray icon already initialized");
          log.info("Showing tray icon");
          this.trayIcon = new TrayIcon(imageFactory.loadImage(), "Document Finder", trayMenuController.getMenu());
          trayIcon.setImageAutoSize(true);
          // On windows the main window will be displayes with a single left click. On MacOs with a right click.
          if (platformAdapter.isWindowsOs() || platformAdapter.isMacOs()) {
            trayIcon.addActionListener(trayMenuController::showMainWindowHandler);
          }

          try {
            SystemTray.getSystemTray().add(trayIcon);
            log.debug("initialized system ray icon");
          } catch (Exception e) {
            log.error("Error showing system tray icon", e);
          }

          this.registered = true;
        });
    } else {
      log.warn("System tray not supported on operating system '{} version {} ({})', won't register", SystemUtils.OS_NAME, SystemUtils.OS_VERSION, SystemUtils.OS_ARCH);
    }
  }

  public synchronized void unregisterTrayIcon() {
    if (isRegistered() && platformAdapter.isSystemTraySupported()) {
      SwingUtilities.invokeLater(() -> {
        log.info("Removeing tray icon");
        SystemTray.getSystemTray().remove(trayIcon);
        registered = false;
        this.trayIcon = null;
      });
    }
  }

  public boolean isRegistered() {
    return registered;
  }
}
