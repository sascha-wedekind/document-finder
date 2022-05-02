package com.bytedompteur.documentfinder.ui.adapter.out;

import javafx.application.Platform;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.SystemUtils;

import javax.inject.Inject;

@NoArgsConstructor(onConstructor = @__(@Inject))
public class JavaFxPlatformAdapter {

  public void runLater(Runnable runnable) {
    Platform.runLater(runnable);
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
}
