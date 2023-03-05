package com.bytedompteur.documentfinder.settings.dagger;

import com.bytedompteur.documentfinder.settings.adapter.in.SettingsChangedCalculator;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import com.bytedompteur.documentfinder.settings.adapter.out.FilesReadWriteAdapter;
import com.bytedompteur.documentfinder.settings.adapter.out.PlatformAdapter;
import com.bytedompteur.documentfinder.settings.core.RunOnStartupSettingsservice;
import com.bytedompteur.documentfinder.settings.core.SettingsServiceImpl;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public abstract class SettingsModule {

  @Provides
  @Singleton
  static SettingsService provideSettingsService(
    @Named("applicationHomeDirectory") String applicationHome,
    FilesReadWriteAdapter adapter,
    SettingsChangedCalculator settingsChangedCalculator
  ) {
    var platformAdapter = new PlatformAdapter();
    var settingsService = new SettingsServiceImpl(applicationHome, adapter);
    return new RunOnStartupSettingsservice(settingsService, settingsChangedCalculator, platformAdapter);
  }

  @Provides
  @Singleton
  static SettingsChangedCalculator provideSettingsChangedCalculator() {
    return new SettingsChangedCalculator();
  }

  @Provides
  @Singleton
  static FilesReadWriteAdapter provideFilesReadWriteAdapter() {
    return new FilesReadWriteAdapter();
  }

  static PlatformAdapter providePlatformAdapter() {
    return new PlatformAdapter();
  }
}
