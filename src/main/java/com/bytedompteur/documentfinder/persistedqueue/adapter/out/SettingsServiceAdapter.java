package com.bytedompteur.documentfinder.persistedqueue.adapter.out;

import com.bytedompteur.documentfinder.settings.adapter.in.Settings;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SettingsServiceAdapter {

  private final SettingsService settingsService;

  public Settings getSettings () {
    return settingsService.read().orElseGet(settingsService::getDefaultSettings);
  }
}
