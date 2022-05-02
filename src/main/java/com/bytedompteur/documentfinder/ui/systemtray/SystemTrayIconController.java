package com.bytedompteur.documentfinder.ui.systemtray;

import com.bytedompteur.documentfinder.ui.adapter.out.JavaFxPlatformAdapter;
import com.bytedompteur.documentfinder.ui.systemtray.dagger.SystemTrayScope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;

import javax.inject.Inject;
import java.awt.*;

@Slf4j
@SystemTrayScope
public class SystemTrayIconController {

	private final TrayIcon trayIcon;
	private volatile boolean registered;
	private final SystemTrayMenuController trayMenuController;
	private final JavaFxPlatformAdapter platformAdapter;

	@Inject
  public SystemTrayIconController(
		SystemTrayImageFactory imageFactory,
		SystemTrayMenuController trayMenuController,
		JavaFxPlatformAdapter platformAdapter) {
		this.trayMenuController = trayMenuController;
		this.platformAdapter = platformAdapter;
		this.trayIcon = new TrayIcon(imageFactory.loadImage(), "Document Finder", trayMenuController.getMenu());
	}

	public synchronized void registerTrayIcon() throws IllegalStateException {
		Validate.isTrue(!isRegistered(), "System tray icon already initialized");

		trayIcon.setImageAutoSize(true);
		if (platformAdapter.isWindowsOs()) {
			trayIcon.addActionListener(trayMenuController::showMainWindowHandler);
		}

		try {
			SystemTray.getSystemTray().add(trayIcon);
			log.debug("initialized system ray icon");
		} catch (AWTException e) {
			log.error("Error showing system tray icon", e);
		}

		this.registered = true;
	}

	public synchronized void unregisterTrayIcon() {
		if (isRegistered()) {
			SystemTray.getSystemTray().remove(trayIcon);
			registered = false;
		}
	}

	public boolean isRegistered() {
		return registered;
	}
}
