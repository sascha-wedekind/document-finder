package com.bytedompteur.documentfinder.ui.adapter.out;

import javafx.application.Platform;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.SystemUtils;

import javax.inject.Inject;
import java.awt.*;

@NoArgsConstructor(onConstructor = @__(@Inject))
public class JavaFxPlatformAdapter {

  public void runLater(Runnable runnable) {
    Platform.runLater(runnable);
  }

  public boolean isFxApplicationThread() {
    return Platform.isFxApplicationThread();
  }

  public void disableImplicitExit() {
    Platform.setImplicitExit(false);
  }

  public boolean isMacOs() {
    return SystemUtils.IS_OS_MAC;
  }

  public boolean isWindowsOs() {
    return SystemUtils.IS_OS_WINDOWS;
  }

  public boolean isLinuxOs() {
    return SystemUtils.IS_OS_LINUX;
  }

  public boolean isSystemTraySupported() {
    return !isLinuxOs() && SystemTray.isSupported();
  }
}
