package com.bytedompteur.documentfinder.settings.core;

import com.bytedompteur.documentfinder.settings.adapter.in.Settings;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsChangedCalculator;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import com.bytedompteur.documentfinder.settings.adapter.out.PlatformAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
@Slf4j
public class RunOnStartupSettingsservice implements SettingsService {

  private final SettingsService wrappedSettingsService;

  private final SettingsChangedCalculator settingsChangedCalculator;

  private final PlatformAdapter platformAdapter;

  private Settings lastSettingRead;

  @Override
  public void save(Settings value) {
    if (nonNull(value) && nonNull(lastSettingRead) && settingsChangedCalculator.calculateChanges(lastSettingRead, value).contains(SettingsChangedCalculator.ChangeType.RUN_ON_STARTUP)) {
      applyRunOnStartup(value);
    }
    wrappedSettingsService.save(value);
  }

  @Override
  public Optional<Settings> read() {
    var result = wrappedSettingsService.read();
    if (result.isPresent() && isRunOnStartupSupported()) {
      lastSettingRead = result.get().toBuilder().runOnStartup(platformAdapter.isRunOnStartupEnabled()).build();
      result = Optional.of(lastSettingRead);
    }
    return result;
  }

  private void applyRunOnStartup(Settings value) {
    if (isRunOnStartupSupported()) {
      try {
        if (value.isRunOnStartup() && !platformAdapter.isRunOnStartupEnabled()) {
          platformAdapter.enableRunOnStartup();
        } else if (!value.isRunOnStartup() && platformAdapter.isRunOnStartupEnabled()) {
          platformAdapter.disableRunOnStartup();
        }
      } catch (Exception e) {
        log.error("Error while toggling run on startup", e);
      }
    }
  }

  boolean isRunOnStartupSupported() {
    var result = platformAdapter.isMacOs() || platformAdapter.isWindowsOs();
    log.debug("Run on startup supported: {}", result);
    return result;
  }
}
