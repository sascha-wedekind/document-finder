package com.bytedompteur.documentfinder.settings.dagger;

import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import com.bytedompteur.documentfinder.settings.core.FilesReadWriteAdapter;
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
    FilesReadWriteAdapter adapter
  ) {
    return new SettingsServiceImpl(applicationHome, adapter);
  }

  @Provides
  @Singleton
  static FilesReadWriteAdapter provideFilesReadWriteAdapter() {
    return new FilesReadWriteAdapter();
  }

}
