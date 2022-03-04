package com.bytedompteur.documentfinder.ui.optionswindow.dagger;

import com.bytedompteur.documentfinder.PathUtil;
import com.bytedompteur.documentfinder.settings.dagger.SettingsModule;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module(includes = {BaseOptionsWindowModule.class, SettingsModule.class})
public abstract class OptionsWindowModule {

  @Provides
  @Singleton
  public static PathUtil providePathUtil() {
    return new PathUtil();
  }

}
