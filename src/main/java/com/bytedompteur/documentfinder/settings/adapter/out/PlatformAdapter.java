package com.bytedompteur.documentfinder.settings.adapter.out;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.cryptomator.integrations.autostart.AutoStartProvider;
import org.cryptomator.integrations.autostart.ToggleAutoStartFailedException;

import jakarta.inject.Inject;
import java.util.Optional;

@Slf4j
public class PlatformAdapter {

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private Optional<AutoStartProvider> autoStartProvider = Optional.empty();

  @Inject
  public PlatformAdapter() {
    if (isMacOs() || isWindowsOs()) {
      try {
        autoStartProvider = AutoStartProvider.get();
      } catch (Exception e) {
        log.error("Error while getting AutoStartProvider", e);
      }
    }
  }

  public boolean isMacOs() {
    return SystemUtils.IS_OS_MAC;
  }

  public boolean isWindowsOs() {
    return SystemUtils.IS_OS_WINDOWS;
  }

  public void enableRunOnStartup() throws ToggleAutoStartFailedException {
    if (autoStartProvider.isPresent()) {
      try {
        autoStartProvider.get().enable();
      } catch (ToggleAutoStartFailedException e) {
        log.error("Error while enabling run on startup", e);
        throw e;
      }
    }
  }

  public void disableRunOnStartup() throws ToggleAutoStartFailedException {
    if (autoStartProvider.isPresent()) {
      try {
        autoStartProvider.get().disable();
      } catch (ToggleAutoStartFailedException e) {
        log.error("Error while disabling run on startup", e);
        throw e;
      }
    }
  }

  public boolean isRunOnStartupEnabled() {
    return autoStartProvider.map(AutoStartProvider::isEnabled).orElse(false);
  }
}
